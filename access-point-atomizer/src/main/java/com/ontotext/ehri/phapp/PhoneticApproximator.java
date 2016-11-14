package com.ontotext.ehri.phapp;

import name.fraser.neil.plaintext.diff_match_patch;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;

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
        //s = approximate(s, SUBSTITUTIONS);
        s = squeeze(s);
        return s;
    }

    @Override
    public Object encode(Object o) throws EncoderException {
        if (o instanceof String) return encode((String) o);
        throw new EncoderException("this encoder can only encode objects of class java.lang.String");
    }

    public static void main(String[] args) throws EncoderException {
        String[] strings = { "Zhytomyr", "지토미르", "ジトームィル", "Jitomir", "Ĵitomir", "Jîtomîr", "Jıtomır", "Jytomyr", "Jytómyr", "Schytomyr", "Shitomir", "Zhitomir", "Zhytomyr", "Zhytomyr", "Zhytomyr", "Žitomir", "Žitomir", "Zitomiria", "Zjytomyr", "Zjytomyr", "Zjytomyr", "Zjytomyr", "Zjytomyr", "Žõtomõr", "Zsitomir", "Żytomierz", "Żytomierz", "Žytomyr", "Žytomyr", "Žytomyr", "Žytomyr", "Žytomyr", "Žytomyras", "جيتومير", "ژیتومیر", "ژیتومیر", "ז'יטומיר", "זשיטאמיר", "Горад Жытомір", "Житомир", "Житомир", "Житомир", "Житомир", "Житомир", "Житомир", "Житомиръ", "Жытомир", "Ժիտոմիր", "ჟიტომირი", "日托米尔" };
        PhoneticApproximator phapp = new PhoneticApproximator();
        diff_match_patch dmp = new diff_match_patch();

        for (int i = 1; i < strings.length; i++) {
            String a = phapp.encode(strings[i - 1]);
            String b = phapp.encode(strings[i]);
            System.out.println("\"" + a + "\" => \"" + b + "\"");
            for (diff_match_patch.Diff diff : dmp.diff_main(a, b)) System.out.println(diff.operation + " \"" + diff.text + "\"");
            System.out.println();
        }
    }
}
