package com.ontotext.ehri.tests;

import com.ontotext.acceptance.ClasspathUtils;
import com.ontotext.ehri.TypoFixerPR;
import com.ontotext.ehri.TypoLearnerPR;
import com.ontotext.ehri.tybus.Index;
import com.ontotext.ehri.tybus.Model;
import gate.Annotation;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.creole.tokeniser.SimpleTokeniser;
import gate.util.GateException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TypoBusterTests {

	@Test
	public void test() {

		try {
			// initialize GATE
			Gate.runInSandbox(true);
			Gate.init();

			File modelFile = new File("model.gz");

			// tokenize document
			Document document = Factory.newDocument(ClasspathUtils.loadFileAsString("supernova.txt"));
			SimpleTokeniser tokeniser = new SimpleTokeniser();
			tokeniser.setRulesURL(ClasspathUtils.getFileURL("rules/DefaultTokeniser.rules"));
			tokeniser.setEncoding(StandardCharsets.UTF_8.toString());
			tokeniser.setDocument(document);
			tokeniser.init();
			tokeniser.execute();

			// learn typos
			TypoLearnerPR learner = new TypoLearnerPR();
			learner.setAnnotationSet("");
			learner.setAnnotationType("Token");
			learner.setAnnotationFeature("string");
			learner.setModelFilePath(modelFile.toURI().toURL());
			learner.setDocument(document);
			learner.init();
			learner.execute();

			// check model
			Model model = Model.deserialize(modelFile);
			assertNotNull(model);
			assertEquals("unexpected number of distinct tokens", 893, model.numDistinctTokens());
			assertEquals("unexpected maximum token length", 19, model.maxTokenLength());
			assertEquals("unexpected number of tokens with length 9", 71, model.getTokens(9).size());

			// fix typos
			TypoFixerPR fixer = new TypoFixerPR();
			fixer.setAnnotationSet("");
			fixer.setAnnotationType("Token");
			fixer.setAnnotationFeature("string");
			fixer.setModelFilePath(modelFile.toURI().toURL());
			fixer.setNumCorrections(Index.DEFAULT_NUM_CORRECTIONS);
			fixer.setMinLength(Index.MIN_LENGTH);
			fixer.setMinCorrectionFrequency(Index.MIN_CORRECTION_FREQUENCY);
			fixer.setMaxTypoFrequency(Index.MAX_TYPO_FREQUENCY);
			fixer.setTypoFrequencyRatio(Index.TYPO_FREQUENCY_RATIO);
			fixer.setDocument(document);
			fixer.init();
			fixer.execute();

			// check annotation features
			Annotation supernova = getFeatureForFeatureName(document, "supernova");
			assertNotNull(supernova);
			Object string = supernova.getFeatures().get("string");
			assertNotNull(string);
			assertEquals("supernova", string);
			Object correction = supernova.getFeatures().get("correction");
			assertNotNull(correction);
			assertEquals("supernova", correction);

			Annotation suprenova = getFeatureForFeatureName(document, "suprenova");
			assertNotNull(suprenova);
			string = suprenova.getFeatures().get("string");
			assertNotNull(string);
			assertEquals("suprenova", string);
			correction = suprenova.getFeatures().get("correction");
			assertNotNull(correction);
			assertEquals("supernova", correction);

			Annotation gastronomers = getFeatureForFeatureName(document, "gastronomers");
			assertNotNull(gastronomers);
			string = gastronomers.getFeatures().get("string");
			assertNotNull(string);
			assertEquals("gastronomers", string);
			correction = gastronomers.getFeatures().get("correction");
			assertNotNull(correction);
			assertEquals("astronomers", correction);

			// clean up
			modelFile.delete();

		} catch (GateException | ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	private Annotation getFeatureForFeatureName(Document document, String featureName) {
		Annotation result = null;
		for (Annotation annotation : document.getAnnotations()) {
			String currFeatureName = (String) annotation.getFeatures().get("string");
			if (StringUtils.equals(currFeatureName, featureName)) {
				result = annotation;
				break;
			}
		}
		return result;
	}
}
