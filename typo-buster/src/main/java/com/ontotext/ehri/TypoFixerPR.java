package com.ontotext.ehri;

import com.ontotext.ehri.tybus.Index;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.net.URL;

@CreoleResource(name = "Typo Fixer", comment = "Fixes typos.")
public class TypoFixerPR extends AbstractLanguageAnalyser {
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
    @CreoleParameter(defaultValue = "1", comment = "Number of corrections to make (set to 0 for full correction).")
    public void setNumCorrections(Integer numCorrections) {
        this.numCorrections = numCorrections;
    }

    public Integer getMinLength() {
        return minLength;
    }

    @CreoleParameter(defaultValue = "5", comment = "Minimum length of correction or typo.")
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMinCorrectionFrequency() {
        return minCorrectionFrequency;
    }

    @CreoleParameter(defaultValue = "10", comment = "Minimum frequency of correction.")
    public void setMinCorrectionFrequency(Integer minCorrectionFrequency) {
        this.minCorrectionFrequency = minCorrectionFrequency;
    }

    public Integer getMaxTypoFrequency() {
        return maxTypoFrequency;
    }

    @CreoleParameter(defaultValue = "10", comment = "Absolute maximum frequency of typo.")
    public void setMaxTypoFrequency(Integer maxTypoFrequency) {
        this.maxTypoFrequency = maxTypoFrequency;
    }

    public Float getTypoFrequencyRatio() {
        return typoFrequencyRatio;
    }

    @CreoleParameter(defaultValue = "0.1f", comment = "Typo-frequency to correction-frequency ratio.")
    public void setTypoFrequencyRatio(Float typoFrequencyRatio) {
        this.typoFrequencyRatio = typoFrequencyRatio;
    }

    @Override
    public Resource init() throws ResourceInstantiationException {
        // TODO: create index according to user preferences
        return this;
    }

    @Override
    public void execute() throws ExecutionException {
        // TODO: bust typos with index
    }
}
