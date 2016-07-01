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

@CreoleResource(name = "Typo Learner", comment = "Learns typos.")
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
    @CreoleParameter(defaultValue = "", comment = "Name of the annotation set.")
    public void setAnnotationSet(String annotationSet) {
        this.annotationSet = annotationSet;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    @RunTime
    @CreoleParameter(comment = "Name of the annotation type.")
    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getAnnotationFeature() {
        return annotationFeature;
    }

    @RunTime
    @CreoleParameter(comment = "Name of the annotation feature.")
    public void setAnnotationFeature(String annotationFeature) {
        this.annotationFeature = annotationFeature;
    }

    public URL getModelFilePath() {
        return modelFilePath;
    }

    @RunTime
    @CreoleParameter(comment = "Path to the model file.")
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
                model.addToken(annotation.getFeatures().get(annotationFeature).toString());
            }

            System.out.println("number of distinct tokens in model: " + model.numTokens());
            System.out.println("serializing model to file: " + modelFile.getAbsolutePath());
            Model.serialize(model, modelFile);
            System.out.println("done");
            System.out.println(model.toString());

        } catch (URISyntaxException | IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
