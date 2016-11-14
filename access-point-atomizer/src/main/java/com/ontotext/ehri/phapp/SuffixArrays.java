package com.ontotext.ehri.phapp;

import java.util.*;

public class SuffixArrays {

    private static Suffix[] suffixes(char[] chars) {
        Suffix[] result = new Suffix[chars.length];
        for (int i = 0; i < chars.length; i++) result[i] = new Suffix(chars, i);
        return result;
    }

    private static Suffix[] suffixes(String string) {
        return suffixes(string.toCharArray());
    }

    private static Suffix[] sort(Suffix[] suffixes) {
        Arrays.sort(suffixes);
        return suffixes;
    }

    private static Suffix[] concatenate(Suffix[] a, Suffix[] b) {
        Suffix[] result = new Suffix[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static NavigableSet<CommonPrefix> overlaps(String a, String b) {
        if (a == null || b == null || a.length() == 0 || b.length() == 0) return null;

        NavigableSet<CommonPrefix> result = new TreeSet<CommonPrefix>();
        Suffix[] sortedSuffixes = sort(concatenate(suffixes(a), suffixes(b)));

        for (int i = 1; i < sortedSuffixes.length; i++) {
            CommonPrefix lcp = sortedSuffixes[i - 1].longestCommonPrefix(sortedSuffixes[i]);
            if (lcp == null || lcp.length == 0) continue;
            result.add(lcp);
        }

        Set<CommonPrefix> nonMaximal = new HashSet<CommonPrefix>();
        for (CommonPrefix cp : result) {
            if (nonMaximal.contains(cp)) continue;

            for (CommonPrefix otherCP : result) {
                if (cp == otherCP || nonMaximal.contains(otherCP) || ! cp.contains(otherCP)) continue;
                nonMaximal.add(otherCP);
            }
        }

        result.removeAll(nonMaximal);
        return result;
    }

    /*
    public static List<String[]> diffPairs(String a, String b) {
        String marker = "_";

        for (String overlap : overlaps(a, b)) {
            a = a.replace(overlap, marker);
            b = b.replace(overlap, marker);
        }

        List<String[]> result = new LinkedList<String[]>();
        int aFrom = 0;
        int bFrom = 0;
        int aTo, bTo;
        while ((aTo = a.indexOf(marker, aFrom)) != -1 && (bTo = b.indexOf(marker, bFrom)) != -1) {
            String[] diffPair = new String[2];
            diffPair[0] = a.substring(aFrom, aTo);
            diffPair[1] = b.substring(bFrom, bTo);
            result.add(diffPair);
            aFrom = aTo + marker.length();
            bFrom = bTo + marker.length();
        }

        String[] diffPair = new String[2];
        diffPair[0] = a.substring(aFrom);
        diffPair[1] = b.substring(bFrom);
        result.add(diffPair);

        return result;
    }
    */
}
