package com.ontotext.ehri;

import gate.*;
import gate.annotation.AnnotationFactory;
import gate.annotation.DefaultAnnotationFactory;
import gate.annotation.NodeImpl;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;
import gate.util.SimpleFeatureMapImpl;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.language.DaitchMokotoffSoundex;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.sf.junidecode.Junidecode.unidecode;

@CreoleResource(name = "Access Point Atomizer", comment = "Atomizes EHRI access points.")
public class AccessPointAtomizerPR extends AbstractLanguageAnalyser {

    // names of annotation types
    private static final String ACCESS_POINT_TYPE = "AccessPoint";
    private static final String ATOM_TYPE = "Atom";
    private static final String TOKEN_TYPE = "Token";

    // match entire access points
    private static final Pattern ACCESS_POINT_MATCH = Pattern.compile("[^\\n]+");

    // match atom boundaries and any number of surrounding spaces
    private static final Pattern ATOM_SPLIT = Pattern.compile("(\\s*(" +
            "[,;:*/\"\\(\\)\\[\\]\\{\\}]|" +    // single-character boundaries
            "\\.\\.+|-->?|>>|<>|" +             // multi-character boundaries
            "\\s-\\s" +                         // boundaries requiring spaces
            ")\\s*)+");

    // match pattern on entire string to filter out some non-atoms
    private static final Pattern ATOM_FILTER = Pattern.compile("^(" +
            "[^\\p{IsWord}]+|" +                    // punctuation and other crap
            "\\p{IsDigit}{1,3}|\\p{IsDigit}{5,}|" + // whole numbers excluding years
            "[IVX]+|" +                             // roman numbers
            "\\p{IsL}|" +                           // single letters
            "etc\\.|Zie ook" +                      // meaningless keywords
            ")$");


    // match tokens within atom
    private static final Pattern TOKEN_MATCH = Pattern.compile("['\\p{IsWord}]+");

    // name of fingerprint feature
    private static final String FINGERPRINT_FEATURE = "fingerprint";

    private static final char L_POLISH = 322;
    private static final char[] A_UMLAUT = {97, 776};
    private static final char[] O_UMLAUT = {111, 776};
    private static final char[] U_UMLAUT = {117, 776};
    private static final String A_UMLAUT_STRING = String.valueOf(A_UMLAUT);
    private static final String O_UMLAUT_STRING = String.valueOf(O_UMLAUT);
    private static final String U_UMLAUT_STRING = String.valueOf(U_UMLAUT);

    // combining characters to be deleted from fingerprint
    private static final Pattern COMBINING_CHARACTER = Pattern.compile("[" +
            "'’`" +                                         // apostrophes
            "\\p{InSpacingModifierLetters}" +               // http://www.unicode.org/charts/PDF/U02B0.pdf
            "\\p{InCombiningDiacriticalMarks}" +            // http://www.unicode.org/charts/PDF/U0300.pdf
            "\\p{InCombiningDiacriticalMarksSupplement}" +  // http://www.unicode.org/charts/PDF/U1DC0.pdf
            "\\p{InCombiningMarksforSymbols}" +             // http://www.unicode.org/charts/PDF/U20D0.pdf
            "\\p{InCombiningHalfMarks}" +                   // http://www.unicode.org/charts/PDF/UFE20.pdf
            "]");

    // punctuation characters to be replaces with space in fingerprint
    private static final Pattern PUNCTUATION_CHARACTER = Pattern.compile("\\p{IsPunct}");

    // sequence of spaces to be squashed in fingerprint
    private static final Pattern SPACE_SEQUENCE = Pattern.compile("\\s+");

    // phonetic encoder
    private static final StringEncoder ENCODER = new DaitchMokotoffSoundex();

    // name of the phonetic encoding feature
    private static final String ENCODING_FEATURE = "phoneticEncoding";

    private int annotationID;
    private int nodeID;

    private String outputASName;

    public String getOutputASName() {
        return outputASName;
    }

    @CreoleParameter(defaultValue = "", comment = "Name of the output annotation set.")
    @Optional
    @RunTime
    public void setOutputASName(String outputASName) {
        this.outputASName = outputASName;
    }

    @Override
    public Resource init() throws ResourceInstantiationException {
        annotationID = 0;
        nodeID = 0;
        return this;
    }

    @Override
    public void execute() throws ExecutionException {

        try {
            AnnotationFactory factory = new DefaultAnnotationFactory();
            AnnotationSet outputAS = document.getAnnotations(outputASName);
            DocumentContent content = document.getContent();
            Matcher accessPointMatcher = ACCESS_POINT_MATCH.matcher(content.toString());

            // iterate through access-point matches
            while (accessPointMatcher.find()) {
                String accessPointText = accessPointMatcher.group();
                FeatureMap accessPointFeatures = new SimpleFeatureMapImpl();
                Node accessPointStart = new NodeImpl(nodeID++, (long) accessPointMatcher.start());
                Node accessPointEnd = new NodeImpl(nodeID++, (long) accessPointMatcher.end());
                factory.createAnnotationInSet(outputAS, annotationID++, accessPointStart, accessPointEnd, ACCESS_POINT_TYPE, accessPointFeatures);

                // the start of the first atom is the start of the entire access point
                Matcher atomSplitter = ATOM_SPLIT.matcher(accessPointText);
                Node atomStart = new NodeImpl(nodeID++, accessPointStart.getOffset());

                // iterate through atom-boundary matches
                while (atomSplitter.find()) {
                    Node atomEnd = new NodeImpl(nodeID++, accessPointStart.getOffset() + (long) atomSplitter.start());
                    annotateAtom(factory, outputAS, content, atomStart, atomEnd);

                    // update the start of the next atom
                    atomStart = new NodeImpl(nodeID++, accessPointStart.getOffset() + (long) atomSplitter.end());
                }

                // the end of the last atom is the end of the entire access point
                Node atomEnd = new NodeImpl(nodeID++, accessPointEnd.getOffset());
                annotateAtom(factory, outputAS, content, atomStart, atomEnd);
            }

        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Add an atom annotation.
     *
     * @param factory   The annotation factory.
     * @param as        The annotation set.
     * @param content   The document content.
     * @param atomStart The start node of the atom.
     * @param atomEnd   The end node of the atom.
     */
    private void annotateAtom(AnnotationFactory factory, AnnotationSet as, DocumentContent content, Node atomStart, Node atomEnd) throws EncoderException {
        String atomText = extractText(content, atomStart, atomEnd);

        // add annotation only if the atom is valid
        if (atomText != null && atomText.length() > 0 && !ATOM_FILTER.matcher(atomText).matches()) {
            FeatureMap atomFeatures = new SimpleFeatureMapImpl();
            atomFeatures.put(FINGERPRINT_FEATURE, extractFingerprint(atomText));
            atomFeatures.put(ENCODING_FEATURE, encodePhonetics(atomText));
            factory.createAnnotationInSet(as, annotationID++, atomStart, atomEnd, ATOM_TYPE, atomFeatures);

            Matcher tokenMatcher = TOKEN_MATCH.matcher(atomText);

            // iterate through token matches
            while (tokenMatcher.find()) {
                Node tokenStart = new NodeImpl(nodeID++, atomStart.getOffset() + (long) tokenMatcher.start());
                Node tokenEnd = new NodeImpl(nodeID++, atomStart.getOffset() + (long) tokenMatcher.end());
                String tokenText = extractText(content, tokenStart, tokenEnd);
                FeatureMap tokenFeatures = new SimpleFeatureMapImpl();
                tokenFeatures.put(FINGERPRINT_FEATURE, extractFingerprint(tokenText));
                tokenFeatures.put(ENCODING_FEATURE, encodePhonetics(tokenText));
                factory.createAnnotationInSet(as, annotationID++, tokenStart, tokenEnd, TOKEN_TYPE, tokenFeatures);
            }
        }
    }

    /**
     * Extract the text of a document part.
     *
     * @param content The document content.
     * @param start   The start node of the part.
     * @param end     The end node of the part.
     * @return The text of the document part or null if the start or end node is invalid.
     */
    private static String extractText(DocumentContent content, Node start, Node end) {

        try {
            return content.getContent(start.getOffset(), end.getOffset()).toString();
        } catch (InvalidOffsetException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extract the fingerprint of some text.
     *
     * @param text The text.
     * @return The fingerprint of the text.
     */
    public static String extractFingerprint(String text) {
        text = text.toLowerCase();
        text = Normalizer.normalize(text, Normalizer.Form.NFKD); // http://www.unicode.org/reports/tr15/#Norm_Forms
        text = text.replace(L_POLISH, 'l')
                .replace(A_UMLAUT_STRING, "ae")
                .replace(O_UMLAUT_STRING, "oe")
                .replace(U_UMLAUT_STRING, "ue");
        text = COMBINING_CHARACTER.matcher(text).replaceAll("");
        text = PUNCTUATION_CHARACTER.matcher(text).replaceAll(" ");
        text = SPACE_SEQUENCE.matcher(text).replaceAll(" ");
        text = text.trim();
        return text;
    }

    public static String encodePhonetics(String text) throws EncoderException {
        text = unidecode(text);
        text = ENCODER.encode(text);
        return text;
    }
}
