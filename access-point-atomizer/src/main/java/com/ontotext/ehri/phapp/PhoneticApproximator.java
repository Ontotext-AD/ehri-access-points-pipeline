package com.ontotext.ehri.phapp;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

import java.util.SortedSet;

import static net.sf.junidecode.Junidecode.unidecode;

public class PhoneticApproximator implements StringEncoder {
    private static final String[][] SUBSTITUTIONS = {
            { "tsch", "s" },
            { "sch", "s" },
            { "tch", "s" },
            { "ph", "f" },
            { "gh", "g" },
            { "kh", "h" },
            { "wh", "h" },
            { "ch", "s" },
            { "cz", "s" },
            { "sh", "s" },
            { "sz", "s" },
            { "th", "t" },
            { "ts", "t" },
            { "tz", "t" },
            { "ks", "x" },
            { "j", "i" },
            { "y", "i" },
            { "c", "k" },
            { "q", "k" },
            { "w", "v" },
    };

    private static String approximate(String s, String[][] substitutions) {
        for (String[] substitution : substitutions) s = s.replace(substitution[0], substitution[1]);
        return s;
    }

    private static String squeeze(String s) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (! Character.isLetter(c)) continue;
            if (i != 0 && c == s.charAt(i - 1)) continue;
            output.append(c);
        }

        return output.toString();
    }

    @Override
    public String encode(String s) throws EncoderException {
        s = unidecode(s);
        s = s.toLowerCase();
        s = approximate(s, SUBSTITUTIONS);
        s = squeeze(s);
        return s;
    }

    @Override
    public Object encode(Object o) throws EncoderException {
        if (o instanceof String) return encode((String) o);
        throw new EncoderException("this encoder can only encode objects of class java.lang.String");
    }

    public static void main(String[] args) {
        String[] strings = { "shitomir", "zhitomir", "zhytomyr", "zitomir", "jitomireu", "zytomyr" };

        for (int i = 1; i < strings.length; i++) {
            String a = strings[i - 1];
            String b = strings[i];
            System.out.println("\"" + a + "\" => \"" + b + "\"");

            for (String[] diffPair : SuffixArrays.diffPairs(a, b)) {
                System.out.println("\t\"" + diffPair[0] + "\" => \"" + diffPair[1] + "\"");
            }

            System.out.println();
        }
    }
}
