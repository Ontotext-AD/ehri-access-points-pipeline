package com.ontotext.ehri.phapp;

import java.util.Comparator;

/**
 * Compare strings first by length (longer first) and then using their natural ordering (alphabetically).
 */
public class LengthComparator implements Comparator<String> {

    @Override
    public int compare(String a, String b) {
        int lengthComparison = Integer.compare(b.length(), a.length());
        if (lengthComparison != 0) return lengthComparison;

        return a.compareTo(b);
    }
}
