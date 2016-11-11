package com.ontotext.ehri.phapp;

import java.util.Arrays;

public class Suffix implements Comparable<Suffix> {
    private char[] chars;
    private int from;

    public Suffix(char[] chars, int from) {
        this.chars = chars;
        this.from = from;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = from; i < chars.length; i++) result.append(chars[i]);
        return result.toString();
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(chars);
        result = 31 * result + from;
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Suffix otherSuffix = (Suffix) other;
        if (from != otherSuffix.from) return false;
        return Arrays.equals(chars, otherSuffix.chars);
    }

    @Override
    public int compareTo(Suffix other) {
        int length = length();
        int oLength = other.length();

        for (int i = 0; i < Math.min(length, oLength); i++) {
            int charComparison = Character.compare(chars[from + i], other.chars[other.from + i]);
            if (charComparison != 0) return charComparison;
        }

        int lengthComparison = Integer.compare(length, oLength);
        if (lengthComparison != 0) return lengthComparison;

        int fromComparison = Integer.compare(from, other.from);
        if (fromComparison != 0) return fromComparison;

        for (int i = 0; i < from; i++) {
            int charComparison = Character.compare(chars[i], other.chars[i]);
            if (charComparison != 0) return charComparison;
        }

        return 0;
    }

    private int length() {
        return chars.length - from;
    }

    public String longestCommonPrefix(Suffix other) {
        if (chars == other.chars) return null;

        StringBuilder sb = new StringBuilder();
        int length = length();
        int oLength = other.length();

        for (int i = 0; i < Math.min(length, oLength); i++) {
            char c = chars[from + i];
            if (c == other.chars[other.from + i]) sb.append(c);
            else break;
        }

        return sb.toString();
    }
}
