package com.ontotext.ehri;

import com.ontotext.ehri.tybus.Model;
import gate.Annotation;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@CreoleResource(name = "Typo Learner", comment = "Learns typos and creates or updates a model.")
public class TypoLearnerPR extends AbstractLanguageAnalyser {
    private String annotationSet;
    private String annotationType;
    private String annotationFeature;
    private URL modelFilePath;

    public String getAnnotationSet() {
        return annotationSet;
    }

    @Optional
    @RunTime
    @CreoleParameter(defaultValue = "", comment = "Name of the annotation set to learn typos from.")
    public void setAnnotationSet(String annotationSet) {
        this.annotationSet = annotationSet;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    @RunTime
    @CreoleParameter(defaultValue = "Token", comment = "Name of the annotation type to learn typos from.")
    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getAnnotationFeature() {
        return annotationFeature;
    }

    @RunTime
    @CreoleParameter(defaultValue = "fingerprint", comment = "Name of the annotation feature to learn typos from.")
    public void setAnnotationFeature(String annotationFeature) {
        this.annotationFeature = annotationFeature;
    }

    public URL getModelFilePath() {
        return modelFilePath;
    }

    @RunTime
    @CreoleParameter(defaultValue = "model.gz", comment = "Path to the model file to create or update.")
    public void setModelFilePath(URL modelFilePath) {
        this.modelFilePath = modelFilePath;
    }

    @Override
    public void execute() throws ExecutionException {

        try {
            File modelFile = new File(modelFilePath.toURI());
            Model model;

            if (modelFile.isFile()) {
                System.out.println("deserializing model from file: " + modelFile.getAbsolutePath());
                model = Model.deserialize(modelFile);
            } else {
                System.out.println("initializing new model");
                model = new Model();
            }

            System.out.println("adding tokens to model");
            for (Annotation annotation : document.getAnnotations(annotationSet).get(annotationType)) {
                Object featureValue = annotation.getFeatures().get(annotationFeature);
                if (featureValue == null) continue;

                model.addToken(featureValue.toString());
            }

            System.out.println("number of distinct tokens in model: " + model.numDistinctTokens());
            System.out.println("serializing model to file: " + modelFile.getAbsolutePath());
            Model.serialize(model, modelFile);
            System.out.println("serialization finished");

        } catch (URISyntaxException | IOException | ClassNotFoundException e) {
            throw new ExecutionException(e.getMessage(), e);
        }
    }
}
