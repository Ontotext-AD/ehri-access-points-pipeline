package com.ontotext.ehri;

import com.ontotext.ehri.tybus.Index;
import com.ontotext.ehri.tybus.Model;
import gate.Annotation;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@CreoleResource(name = "Typo Fixer", comment = "Fixes typos using existing model.")
public class TypoFixerPR extends AbstractLanguageAnalyser {
    private static final String CORRECTION_FEATURE_NAME = "correction";

    private Index index;

    private String annotationSet;
    private String annotationType;
    private String annotationFeature;
    private URL modelFilePath;

    // index parameters
    Integer numCorrections;
    Integer minLength;
    Integer minCorrectionFrequency;
    Integer maxTypoFrequency;
    Float typoFrequencyRatio;
    Boolean checkPhonetics;

    public String getAnnotationSet() {
        return annotationSet;
    }

    @Optional
    @RunTime
    @CreoleParameter(defaultValue = "", comment = "Name of the annotation set to fix typos in.")
    public void setAnnotationSet(String annotationSet) {
        this.annotationSet = annotationSet;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    @RunTime
    @CreoleParameter(defaultValue = "Token", comment = "Name of the annotation type to fix typos in.")
    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getAnnotationFeature() {
        return annotationFeature;
    }

    @RunTime
    @CreoleParameter(defaultValue = "fingerprint", comment = "Name of the annotation feature to fix typos in.")
    public void setAnnotationFeature(String annotationFeature) {
        this.annotationFeature = annotationFeature;
    }

    public URL getModelFilePath() {
        return modelFilePath;
    }

    @CreoleParameter(defaultValue = "model.gz", comment = "Path to the model file to build index from.")
    public void setModelFilePath(URL modelFilePath) {
        this.modelFilePath = modelFilePath;
    }

    public Integer getNumCorrections() {
        return numCorrections;
    }

    @RunTime
    @CreoleParameter(defaultValue = "" + Index.DEFAULT_NUM_CORRECTIONS, comment = "Number of corrections to make (set to 0 for full correction).")
    public void setNumCorrections(Integer numCorrections) {
        this.numCorrections = numCorrections;
    }

    public Integer getMinLength() {
        return minLength;
    }

    @CreoleParameter(defaultValue = "" + Index.MIN_LENGTH, comment = "Minimum length of correction or typo.")
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMinCorrectionFrequency() {
        return minCorrectionFrequency;
    }

    @CreoleParameter(defaultValue = "" + Index.MIN_CORRECTION_FREQUENCY, comment = "Minimum frequency of correction.")
    public void setMinCorrectionFrequency(Integer minCorrectionFrequency) {
        this.minCorrectionFrequency = minCorrectionFrequency;
    }

    public Integer getMaxTypoFrequency() {
        return maxTypoFrequency;
    }

    @CreoleParameter(defaultValue = "" + Index.MAX_TYPO_FREQUENCY, comment = "Absolute maximum frequency of typo.")
    public void setMaxTypoFrequency(Integer maxTypoFrequency) {
        this.maxTypoFrequency = maxTypoFrequency;
    }

    public Float getTypoFrequencyRatio() {
        return typoFrequencyRatio;
    }

    @CreoleParameter(defaultValue = "" + Index.TYPO_FREQUENCY_RATIO, comment = "Typo-frequency to correction-frequency ratio.")
    public void setTypoFrequencyRatio(Float typoFrequencyRatio) {
        this.typoFrequencyRatio = typoFrequencyRatio;
    }

    public Boolean getCheckPhonetics() {
        return checkPhonetics;
    }

    @CreoleParameter(defaultValue = "" + Index.CHECK_PHONETICS, comment = "Toggle phonetic check.")
    public void setCheckPhonetics(Boolean checkPhonetics) {
        this.checkPhonetics = checkPhonetics;
    }

    @Override
    public Resource init() throws ResourceInstantiationException {

        try {
            File modelFile = new File(modelFilePath.toURI());

            System.out.println("deserializing model from file: " + modelFile.getAbsolutePath());
            Model model = Model.deserialize(modelFile);

            System.out.println("building index from model");
            long start = System.currentTimeMillis();
            index = new Index(model, minLength, minCorrectionFrequency, maxTypoFrequency, typoFrequencyRatio, checkPhonetics);
            long time = System.currentTimeMillis() - start;
            System.out.println("index built in " + time + " ms");
            System.out.println("number of corrections in index: " + index.numCorrections());

        } catch (URISyntaxException | ClassNotFoundException | IOException e) {
            throw new ResourceInstantiationException(e.getMessage(), e);
        }

        return this;
    }

    @Override
    public void execute() throws ExecutionException {

        for (Annotation annotation : document.getAnnotations(annotationSet).get(annotationType)) {
            Object featureValue = annotation.getFeatures().get(annotationFeature);
            if (featureValue == null) continue;

            String correction;
            if (numCorrections < 1) {
                correction = index.correctFully(featureValue.toString());
            } else {
                correction = index.correct(featureValue.toString(), numCorrections);
            }

            annotation.getFeatures().put(CORRECTION_FEATURE_NAME, correction);
        }
    }
}
