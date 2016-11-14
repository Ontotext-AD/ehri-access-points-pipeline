package com.ontotext.ehri.phapp;

public class CommonPrefix implements Comparable<CommonPrefix> {
    protected Suffix a, b;
    protected int length;

    public CommonPrefix(Suffix a, Suffix b, int length) {
        this.a = a;
        this.b = b;
        this.length = length;
    }

    protected int[] froms() {
        return new int[] { a.from, b.from };
    }

    protected int[] tos() {
        return new int[] { a.from + length, b.from + length };
    }

    public boolean contains(CommonPrefix other) {
        return length >= other.length
                && a.from <= other.a.from
                && b.from <= other.b.from
                && a.from + length >= other.a.from + other.length
                && b.from + length >= other.b.from + other.length;
    }

    @Override
    public String toString() {
        return new String(a.chars, a.from, length);
    }

    @Override
    public int hashCode() {
        int result = a.hashCode();
        result = 31 * result + b.hashCode();
        result = 31 * result + length;
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        CommonPrefix otherCP = (CommonPrefix) other;
        if (length != otherCP.length) return false;
        if (!a.equals(otherCP.a)) return false;
        return b.equals(otherCP.b);
    }

    @Override
    public int compareTo(CommonPrefix other) {
        int aFromComparison = Integer.compare(a.from, other.a.from);
        if (aFromComparison != 0) return aFromComparison;

        int bFromComparison = Integer.compare(b.from, other.b.from);
        if (bFromComparison != 0) return bFromComparison;

        int lengthComparison = Integer.compare(other.length, length);
        if (lengthComparison != 0) return lengthComparison;

        int aComparison = a.compareTo(other.a);
        if (aComparison != 0) return aComparison;

        int bComparison = b.compareTo(other.b);
        if (bComparison != 0) return bComparison;

        return 0;
    }
}
