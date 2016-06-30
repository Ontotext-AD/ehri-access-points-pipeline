package com.ontotext.ehri.tests;

import org.junit.Test;

import static com.ontotext.ehri.AccessPointAtomizerPR.extractFingerprint;
import static org.junit.Assert.assertEquals;

public class FingerprintTests {

    @Test
    public void testLowercasing() {
        assertEquals("text", extractFingerprint("text"));
        assertEquals("text", extractFingerprint("TEXT"));
        assertEquals("tex", extractFingerprint("TExT"));
    }

    @Test
    public void testUmlautExpansion() {
        assertEquals("fuer", extractFingerprint("für"));
        assertEquals("oesterreich", extractFingerprint("Österreich"));
        assertEquals("rumaenien", extractFingerprint("Rumänien"));
        assertEquals("aeaeoeoeueue", extractFingerprint("ÄäÖöÜü"));
    }

    @Test
    public void testPolishL() {
        assertEquals("lodz", extractFingerprint("Łódź"));
        assertEquals("lalala", extractFingerprint("Łałała"));
    }

    @Test
    public void testSpaceNormalization() {
        assertEquals("text with spaces", extractFingerprint(" text with  spaces    "));
        assertEquals("text with tabs", extractFingerprint("\ttext\twith \t\t tabs\t\t\t\t"));
        assertEquals("text with newlines", extractFingerprint("\ntext\nwith \n\n newlines\t\n\n\t"));
    }
}
