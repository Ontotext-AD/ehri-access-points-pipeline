package com.ontotext.ehri.tests;

import com.ontotext.ehri.AccessPointAtomizerPR;
import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.util.GateException;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AtomizerTests {
    private static final String DOCUMENT_PATH = "src/test/resources/random-sample.txt";
    private static final String DOCUMENT_ENCODING = "UTF-8";

    @Test
    public void test() {

        try {
            URL documentURL = new File(DOCUMENT_PATH).toURI().toURL();

            // initialize GATE
            Gate.runInSandbox(true);
            Gate.init();

            // process document
            Document document = Factory.newDocument(documentURL, DOCUMENT_ENCODING);
            AccessPointAtomizerPR atomizer = new AccessPointAtomizerPR();
            atomizer.setDocument(document);
            atomizer.execute();

            // check annotation counts
            assertEquals("unexpected number of access-point annotations", 666, document.getAnnotations().get("Access Point").size());
            assertEquals("unexpected number of atom annotations", 1229, document.getAnnotations().get("Atom").size());
            assertEquals("unexpected number of token annotations", 1969, document.getAnnotations().get("Token").size());

            // check a specific access-point annotation
            Annotation accessPoint = document.getAnnotations().get(2000);
            assertNotNull("no annotation with ID 2000", accessPoint);
            assertEquals("unexpected start offset", 8001L, accessPoint.getStartNode().getOffset().longValue());
            assertEquals("unexpected end offset", 8016L, accessPoint.getEndNode().getOffset().longValue());

            // check a specific atom annotation
            Annotation atom = document.getAnnotations().get(2278);
            assertNotNull("no annotation with ID 2278", atom);
            assertEquals("unexpected start offset", 9075L, atom.getStartNode().getOffset().longValue());
            assertEquals("unexpected end offset", 9110L, atom.getEndNode().getOffset().longValue());
            assertNotNull("no fingerprint feature", atom.getFeatures().get("fingerprint"));
            assertEquals("unexpected fingerprint", "dt staatsbuerger juedischen glaubens", atom.getFeatures().get("fingerprint"));

            // check a specific token annotation
            Annotation token = document.getAnnotations().get(348);
            assertNotNull("no annotation with ID 348", token);
            assertEquals("unexpected start offset", 1334L, token.getStartNode().getOffset().longValue());
            assertEquals("unexpected end offset", 1341L, token.getEndNode().getOffset().longValue());
            assertNotNull("no fingerprint feature", token.getFeatures().get("fingerprint"));
            assertEquals("unexpected fingerprint", "bohumin", token.getFeatures().get("fingerprint"));

        } catch (MalformedURLException | GateException e) {
            e.printStackTrace();
        }
    }
}
