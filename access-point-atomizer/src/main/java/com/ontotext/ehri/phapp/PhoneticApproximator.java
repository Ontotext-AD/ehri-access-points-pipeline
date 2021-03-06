package com.ontotext.ehri.phapp;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * String encoder using a trained phonetic model.
 */
public class PhoneticApproximator implements StringEncoder {
    private static final Comparator<String> LENGTH_COMPARATOR = new LengthComparator();
    private static final Pattern TAB_SEPARATOR = Pattern.compile("\\t");

    private SortedMap<String, String> substitutions;

    /**
     * Initialize encoder from a TSV file with extracted substitutions.
     */
    public PhoneticApproximator(File substitutionsTSV) throws FileNotFoundException {
        this(new FileReader(substitutionsTSV));
    }

    /**
     * Initialize encoder from a TSV file with extracted substitutions.
     */
    public PhoneticApproximator(InputStream substitutionsTSV) {
        this(new InputStreamReader(substitutionsTSV, StandardCharsets.UTF_8));
    }

    private PhoneticApproximator(Reader substitutionsTSV) {
        substitutions = new TreeMap<String, String>(LENGTH_COMPARATOR);

        try (BufferedReader reader = new BufferedReader(substitutionsTSV)) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] values = TAB_SEPARATOR.split(line);
                if (values.length > 2) {
                    System.err.println("unexpected line: " + line);
                    continue;
                }

                if (values.length == 2) substitutions.put(values[0], values[1]);
                else substitutions.put(values[0], "");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize encoder from a sorted map with extracted substitutions.
     */
    public PhoneticApproximator(SortedMap<String, String> substitutions) {
        this.substitutions = substitutions;
    }

    @Override
    public Object encode(Object o) throws EncoderException {
        if (o instanceof String) return encode((String) o);
        throw new EncoderException("this encoder can only encode objects of class java.lang.String");
    }

    @Override
    public String encode(String s) throws EncoderException {
        String result = PhoneticModel.normalize(s);
        if (substitutions == null) return result;

        result = substituteFully(result);
        return result;
    }

    /**
     * Repeatedly perform substitutions on string until convergence.
     */
    private String substituteFully(String s) {
        String result = substitute(s);
        if (result.equals(s)) return result;
        return substituteFully(result);
    }

    /**
     * Perform substitutions on string.
     */
    private String substitute(String s) {
        for (String source : substitutions.keySet()) s = s.replace(source, substitutions.get(source));
        return s;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("USAGE: " + PhoneticApproximator.class.getName() + " <substitutions TSV>");
            System.exit(1);
        }

        System.out.println("creating encoder");
        PhoneticApproximator approximator = null;
        try {
            approximator = new PhoneticApproximator(new File(args[0]));
        } catch (FileNotFoundException e) {
            System.err.println("cannot find substitutions TSV file: " + args[0]);
            System.exit(1);
        }

        String safeWord = "stop";
        System.out.println("enter word to encode or \"" + safeWord + "\" to stop");

        Scanner scanner = new Scanner(System.in);
        String line;

        while (! safeWord.equals((line = scanner.nextLine()))) {

            try {
                System.out.println(approximator.encode(line));
            } catch (EncoderException e) {
                e.printStackTrace();
            }
        }
    }
}
