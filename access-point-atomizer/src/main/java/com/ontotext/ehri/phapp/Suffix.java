package com.ontotext.ehri.phapp;

import java.util.Arrays;

public class Suffix implements Comparable<Suffix> {
    protected char[] chars;
    protected int from;

    public Suffix(char[] chars, int from) {
        this.chars = chars;
        this.from = from;
    }

    protected int length() {
        return chars.length - from;
    }

    public CommonPrefix longestCommonPrefix(Suffix other) {
        if (chars == other.chars) return null;

        int cpl = 0;
        while (cpl < length() && cpl < other.length() && chars[from + cpl] == other.chars[other.from + cpl]) cpl++;
        return new CommonPrefix(this, other, cpl);
    }

    @Override
    public String toString() {
        return new String(chars, from, length());
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
}
