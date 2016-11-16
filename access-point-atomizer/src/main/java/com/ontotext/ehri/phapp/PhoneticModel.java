package com.ontotext.ehri.phapp;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

import java.io.*;
import java.util.*;

import static net.sf.junidecode.Junidecode.unidecode;

public class PhoneticModel {
    private static final Comparator<String> LENGTH_COMPARATOR = new LengthComparator();
    private static final String TAB_SEPARATOR = "\t";
    private static final String LINE_SEPARATOR = "\n";
    private static final Set<Character.UnicodeScript> ALLOWED_SCRIPTS = new HashSet<Character.UnicodeScript>();
    static {
        ALLOWED_SCRIPTS.add(Character.UnicodeScript.COMMON);
        ALLOWED_SCRIPTS.add(Character.UnicodeScript.LATIN);
    }

    private static final int MIN_WORD_LENGTH = 4;
    private static final int MAX_DIFF_LENGTH = 4;
    private static final double MAX_DIFF_TO_WORD_RATIO = 0.5;
    private static final double MIN_FREQUENCY_RATIO = 0.05;
    private static final double DELETION_WEIGHT = 0.2;

    private int minWordLength, maxDiffLength;
    private double maxDiffToWordRatio;

    private diff_match_patch dmp;
    private Map<String, Map<String, Integer>> sourceToTargets;
    private Map<String, Integer> diffToFrequency;
    private int maxFrequency;

    public PhoneticModel() {
        this(MIN_WORD_LENGTH, MAX_DIFF_LENGTH, MAX_DIFF_TO_WORD_RATIO);
    }

    public PhoneticModel(int minWordLength, int maxDiffLength, double maxDiffToWordRatio) {
        this.minWordLength = minWordLength;
        this.maxDiffLength = maxDiffLength;
        this.maxDiffToWordRatio = maxDiffToWordRatio;
        dmp = new diff_match_patch();
        sourceToTargets = new HashMap<String, Map<String, Integer>>();
        diffToFrequency = new HashMap<String, Integer>();
        maxFrequency = 0;
    }

    public void add(String[] words) {
        for (int i = 0; i < words.length; i++) for (int j = i + 1; j < words.length; j++) add(words[i], words[j]);
    }

    private void add(String wordA, String wordB) {
        if (wordA == null || wordB == null) return;

        String a = normalize(wordA);
        String b = normalize(wordB);
        if (a.equals(b)) return;

        int wordLength = Math.min(a.length(), b.length());
        if (wordLength < minWordLength) return;

        Diff previousDiff = null;
        for (Diff diff : dmp.diff_main(a, b)) {

            if (previousDiff != null && (previousDiff.operation == Operation.DELETE || previousDiff.operation == Operation.INSERT) && previousDiff.operation != diff.operation) {
                String diffA = previousDiff.text;
                String diffB;

                if (diff.operation == Operation.DELETE || diff.operation == Operation.INSERT) diffB = diff.text;
                else diffB = "";

                add(diffA, diffB, wordLength);
                previousDiff = null;
                continue;
            }

            previousDiff = diff;
        }
    }

    private void add(String diffA, String diffB, int wordLength) {
        if (diffA.equals(diffB)) return;

        int diffLength = Math.max(diffA.length(), diffB.length());
        if (diffLength > maxDiffLength) return;

        double diffToWordRatio = (double) diffLength / (double) wordLength;
        if (diffToWordRatio > maxDiffToWordRatio) return;

        map(diffA, diffB);
        map(diffB, diffA);

        add(diffA);
        add(diffB);
    }

    private void add(String diff) {
        Integer frequency = diffToFrequency.get(diff);
        if (frequency == null) frequency = 1;
        else frequency++;
        diffToFrequency.put(diff, frequency);
    }

    private void map(String source, String target) {
        if (source.length() == 0) return;

        Map<String, Integer> targets = sourceToTargets.get(source);
        if (targets == null) targets = new HashMap<String, Integer>();

        Integer frequency = targets.get(target);
        if (frequency == null) frequency = 1;
        else frequency++;
        targets.put(target, frequency);

        sourceToTargets.put(source, targets);
        if (maxFrequency < frequency) maxFrequency = frequency;
    }

    protected static String normalize(String word) {
        if (! scriptIsAllowed(word)) return "";

        String result = unidecode(word);
        result = result.toLowerCase();
        result = squeeze(result);
        return result;
    }

    private static boolean scriptIsAllowed(String word) {
        for (int i = 0; i < word.length(); i++) if (! ALLOWED_SCRIPTS.contains(Character.UnicodeScript.of(word.codePointAt(i)))) return false;
        return true;
    }

    private static String squeeze(String s) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (! Character.isLetter(c)) continue;
            if (i != 0 && c == s.charAt(i - 1)) continue;

            result.append(c);
        }

        return result.toString();
    }

    public void writeSubstitutions(File substitutionsTSV) {
        writeSubstitutions(substitutionsTSV, MIN_FREQUENCY_RATIO, DELETION_WEIGHT);
    }

    public void writeSubstitutions(File substitutionsTSV, double minFrequencyRatio, double deletionWeight) {
        SortedMap<String, String> substitutions = substitutions(minFrequencyRatio, deletionWeight);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(substitutionsTSV))) {
            for (String source : substitutions.keySet()) writer.write(source + TAB_SEPARATOR + substitutions.get(source) + LINE_SEPARATOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SortedMap<String, String> substitutions() {
        return substitutions(MIN_FREQUENCY_RATIO, DELETION_WEIGHT);
    }

    public SortedMap<String, String> substitutions(double minFrequencyRatio, double deletionWeight) {
        if (minFrequencyRatio < 0 || minFrequencyRatio > 1) {
            System.err.println("minimum frequency ration must be between 0 and 1: " + minFrequencyRatio);
            return null;
        }

        SortedMap<String, String> substitutions = new TreeMap<String, String>(LENGTH_COMPARATOR);

        for (String source : sourceToTargets.keySet()) {
            Map<String, Integer> targets = sourceToTargets.get(source);
            String bestTarget = "";
            int bestFrequency = 0;

            for (String target : targets.keySet()) {
                int frequency = targets.get(target);

                if (bestFrequency < frequency) {
                    bestFrequency = frequency;
                    bestTarget = target;
                }
            }

            double frequencyRatio = (double) bestFrequency / (double) maxFrequency;
            if (bestTarget.length() == 0) frequencyRatio *= deletionWeight;
            if (frequencyRatio < minFrequencyRatio) continue;

            if (source.equals(substitutions.get(bestTarget))) {
                if (diffToFrequency.get(source) >= diffToFrequency.get(bestTarget)) continue;
                substitutions.remove(bestTarget);
            }

            substitutions.put(source, bestTarget);
        }

        return substitutions;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("USAGE: " + PhoneticModel.class.getName() + " <train TSV> <substitutions TSV>");
            System.exit(1);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
            PhoneticModel model = new PhoneticModel();
            String line;

            System.out.println("building model");
            while ((line = reader.readLine()) != null) model.add(line.split("\\t"));

            System.out.println("writing model");
            model.writeSubstitutions(new File(args[1]));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
