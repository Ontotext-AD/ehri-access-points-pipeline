package com.ontotext.ehri.phapp;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

import java.io.*;
import java.util.*;

import static net.sf.junidecode.Junidecode.unidecode;

/**
 * Extensible phonetic model.
 */
public class PhoneticModel {
    private static final Comparator<String> LENGTH_COMPARATOR = new LengthComparator();
    private static final String TAB_SEPARATOR = "\t";
    private static final String LINE_SEPARATOR = "\n";
    private static final Set<Character.UnicodeScript> ALLOWED_SCRIPTS = new HashSet<Character.UnicodeScript>();
    static {
        ALLOWED_SCRIPTS.add(Character.UnicodeScript.COMMON);
        ALLOWED_SCRIPTS.add(Character.UnicodeScript.LATIN);
    }

    // default parameters
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

    /**
     * Create an empty phonetic model with default parameters.
     */
    public PhoneticModel() {
        this(MIN_WORD_LENGTH, MAX_DIFF_LENGTH, MAX_DIFF_TO_WORD_RATIO);
    }

    /**
     * Create an empty phonetic model with custom parameters.
     * @param minWordLength Minimum allowed word length in characters after normalization (ignore short words).
     * @param maxDiffLength Maximum allowed diff length in characters (ignore long diffs).
     * @param maxDiffToWordRatio Maximum allowed ratio of diff length to word length (ignore relatively long diffs).
     */
    public PhoneticModel(int minWordLength, int maxDiffLength, double maxDiffToWordRatio) {
        this.minWordLength = minWordLength;
        this.maxDiffLength = maxDiffLength;
        this.maxDiffToWordRatio = maxDiffToWordRatio;
        dmp = new diff_match_patch();
        sourceToTargets = new HashMap<String, Map<String, Integer>>();
        diffToFrequency = new HashMap<String, Integer>();
        maxFrequency = 0;
    }

    /**
     * Add a bunch of words that may be phonetically related to the model.
     */
    public void add(String[] words) {
        for (int i = 0; i < words.length; i++) for (int j = i + 1; j < words.length; j++) add(words[i], words[j]);
    }

    /**
     * Add a pair of words to the model.
     */
    private void add(String wordA, String wordB) {
        if (wordA == null || wordB == null) return;

        // normalize words and ensure they are still different
        String a = normalize(wordA);
        String b = normalize(wordB);
        if (a.equals(b)) return;

        // ensure none of the words is too short
        int wordLength = Math.min(a.length(), b.length());
        if (wordLength < minWordLength) return;

        // go through diffs between the two words
        Diff previousDiff = null;
        for (Diff diff : dmp.diff_main(a, b)) {

            // add diff pair if the previous operation was DELETE or INSERT and was different than the current one
            if (previousDiff != null && (previousDiff.operation == Operation.DELETE || previousDiff.operation == Operation.INSERT) && previousDiff.operation != diff.operation) {
                String diffA = previousDiff.text;
                String diffB;

                // if this operation is DELETE or INSERT then we have an alteration, otherwise we have a deletion
                if (diff.operation == Operation.DELETE || diff.operation == Operation.INSERT) diffB = diff.text;
                else diffB = "";

                // add diff pair and ignore this diff in the next iteration
                add(diffA, diffB, wordLength);
                previousDiff = null;
                continue;
            }

            // update previous diff
            previousDiff = diff;
        }
    }

    /**
     * Add a diff pair to the model.
     */
    private void add(String diffA, String diffB, int wordLength) {
        if (diffA.equals(diffB)) return;

        // ensure none of the diffs is too long
        int diffLength = Math.max(diffA.length(), diffB.length());
        if (diffLength > maxDiffLength) return;

        // ensure none of the diffs is too long relative to the word
        double diffToWordRatio = (double) diffLength / (double) wordLength;
        if (diffToWordRatio > maxDiffToWordRatio) return;

        // map diffs to each other
        map(diffA, diffB);
        map(diffB, diffA);

        // add diffs to frequency index
        add(diffA);
        add(diffB);
    }

    /**
     * Add a diff to the frequency index.
     */
    private void add(String diff) {
        Integer frequency = diffToFrequency.get(diff);
        if (frequency == null) frequency = 1;
        else frequency++;
        diffToFrequency.put(diff, frequency);
    }

    /**
     * Map a diff to another diff.
     */
    private void map(String source, String target) {
        if (source.length() == 0) return;

        // get the current target diffs mapped to this source diff
        Map<String, Integer> targets = sourceToTargets.get(source);
        if (targets == null) targets = new HashMap<String, Integer>();

        // add this target diff
        Integer frequency = targets.get(target);
        if (frequency == null) frequency = 1;
        else frequency++;
        targets.put(target, frequency);

        // update the target diffs of this source diff and the maximum frequency
        sourceToTargets.put(source, targets);
        if (maxFrequency < frequency) maxFrequency = frequency;
    }

    /**
     * Normalize a word or return empty string if it contains weird letters.
     */
    protected static String normalize(String word) {
        if (! scriptIsAllowed(word)) return "";
        String result = unidecode(word);
        result = result.toLowerCase();
        result = squeeze(result);
        return result;
    }

    /**
     * Return true if the word contains only allowed characters.
     */
    private static boolean scriptIsAllowed(String word) {
        for (int i = 0; i < word.length(); i++) if (! ALLOWED_SCRIPTS.contains(Character.UnicodeScript.of(word.codePointAt(i)))) return false;
        return true;
    }

    /**
     * Remove repeated letters and non-letters from a string.
     */
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

    /**
     * Extract substitutions from model with default parameters and write them to file.
     * @param substitutionsTSV TSV output file.
     */
    public void writeSubstitutions(File substitutionsTSV) {
        writeSubstitutions(substitutionsTSV, MIN_FREQUENCY_RATIO, DELETION_WEIGHT);
    }

    /**
     * Extract substitutions from model with custom parameters and write them to file.
     * @param substitutionsTSV TSV output file.
     * @param minFrequencyRatio Minimum allowed ratio of substitution frequency to maximum substitution frequency (ignore relatively rare substitutions).
     * @param deletionWeight Modifier for the substitution frequency of deletions (penalize deletions).
     */
    public void writeSubstitutions(File substitutionsTSV, double minFrequencyRatio, double deletionWeight) {
        SortedMap<String, String> substitutions = substitutions(minFrequencyRatio, deletionWeight);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(substitutionsTSV))) {
            for (String source : substitutions.keySet()) writer.write(source + TAB_SEPARATOR + substitutions.get(source) + LINE_SEPARATOR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extract substitutions from model with default parameters.
     * @return Map from source string to target string sorted from longest source to shortest source.
     */
    public SortedMap<String, String> substitutions() {
        return substitutions(MIN_FREQUENCY_RATIO, DELETION_WEIGHT);
    }

    /**
     * Extract substitutions from model with custom parameters.
     * @param minFrequencyRatio Minimum allowed ratio of substitution frequency to maximum substitution frequency (ignore relatively rare substitutions).
     * @param deletionWeight Modifier for the substitution frequency of deletions (penalize deletions).
     * @return Map from source string to target string sorted from longest source to shortest source.
     */
    public SortedMap<String, String> substitutions(double minFrequencyRatio, double deletionWeight) {
        SortedMap<String, String> substitutions = new TreeMap<String, String>(LENGTH_COMPARATOR);

        // get the possible targets of each source
        for (String source : sourceToTargets.keySet()) {
            Map<String, Integer> targets = sourceToTargets.get(source);
            String bestTarget = "";
            int bestFrequency = 0;

            // find a target with maximum frequency
            for (String target : targets.keySet()) {
                int frequency = targets.get(target);

                if (bestFrequency < frequency) {
                    bestFrequency = frequency;
                    bestTarget = target;
                }
            }

            // ensure that the substitution is frequent enough relative to the most frequent substitution
            double frequencyRatio = (double) bestFrequency / (double) maxFrequency;
            if (bestTarget.length() == 0) frequencyRatio *= deletionWeight;
            if (frequencyRatio < minFrequencyRatio) continue;

            // if there is already a substitution with the same source and target but swapped, prefer the one with the more frequent target
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
