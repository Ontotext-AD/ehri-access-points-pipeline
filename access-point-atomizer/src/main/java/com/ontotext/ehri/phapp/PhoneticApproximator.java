package com.ontotext.ehri.phapp;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class PhoneticApproximator implements StringEncoder {
    private static final Comparator<String> LENGTH_COMPARATOR = new LengthComparator();
    private static final Pattern TAB_SEPARATOR = Pattern.compile("\\t");

    private SortedMap<String, String> substitutions;

    public PhoneticApproximator(File substitutionsTSV) {
        substitutions = new TreeMap<String, String>(LENGTH_COMPARATOR);

        try (BufferedReader reader = new BufferedReader(new FileReader(substitutionsTSV))) {
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

    private String substituteFully(String s) {
        String result = substitute(s);
        if (result.equals(s)) return result;

        return substituteFully(result);
    }

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
        PhoneticApproximator approximator = new PhoneticApproximator(new File(args[0]));

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
