package com.ontotext.ehri;

import com.ontotext.ehri.lodis.Disambiguator;
import com.ontotext.ehri.lodis.Location;
import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;

import java.util.*;

@CreoleResource(name = "Location Disambiguator", comment = "Finds the most probable location from multiple candidates.")
public class LocationDisambiguatorPR extends AbstractLanguageAnalyser {
    private static final String GEONAMES_ID_PREFIX = "http://sws.geonames.org/";
    private static final String GEONAMES_ID_SUFFIX = "/";
    private static final String GEONAMES_TYPE_PREFIX = "http://www.geonames.org/ontology#";

    private static final String ID_FEATURE_NAME = "inst";
    private static final String TYPE_FEATURE_NAME = "class";
    private static final String LATITUDE_FEATURE_NAME = "latitude";
    private static final String LONGITUDE_FEATURE_NAME = "longitude";
    private static final String POPULATION_FEATURE_NAME = "population";
    private static final String LINKS_FEATURE_NAME = "wikipediaLinks";
    private static final String ANCESTORS_FEATURE_NAME = "ancestors";

    private static final Set<String> REQUIRED_CANDIDATE_FEATURES = new HashSet<String>();
    static {
        Collections.addAll(REQUIRED_CANDIDATE_FEATURES, "inst", "class", "ancestors", "latitude", "longitude");
    }

    private String annotationSetName;
    private String contextAnnotationType;
    private String candidateAnnotationType;

    public String getAnnotationSetName() {
        return annotationSetName;
    }

    @Optional
    @RunTime
    @CreoleParameter(defaultValue = "", comment = "Name of the annotation set to disambiguate locations in.")
    public void setAnnotationSetName(String annotationSetName) {
        this.annotationSetName = annotationSetName;
    }

    public String getContextAnnotationType() {
        return contextAnnotationType;
    }

    @RunTime
    @CreoleParameter(defaultValue = "Access Point", comment = "Name of the annotation type to use as context for disambiguation.")
    public void setContextAnnotationType(String contextAnnotationType) {
        this.contextAnnotationType = contextAnnotationType;
    }

    public String getCandidateAnnotationType() {
        return candidateAnnotationType;
    }

    @RunTime
    @CreoleParameter(defaultValue = "Lookup", comment = "Name of the annotation type of location candidates.")
    public void setCandidateAnnotationType(String candidateAnnotationType) {
        this.candidateAnnotationType = candidateAnnotationType;
    }

    @Override
    public void execute() throws ExecutionException {
        AnnotationSet annotationSet = document.getAnnotations(annotationSetName);
        AnnotationSet candidateAnnotationSet = annotationSet.get(candidateAnnotationType, REQUIRED_CANDIDATE_FEATURES);

        for (Annotation context : annotationSet.get(contextAnnotationType)) {
            Set<SortedSet<Location>> lookups = new HashSet<SortedSet<Location>>();

            // collect candidate locations at each lookup
            Set<Integer> addedAnnotationIDs = new HashSet<Integer>();
            AnnotationSet candidatesInContext = candidateAnnotationSet.get(context.getStartNode().getOffset(), context.getEndNode().getOffset());
            for (Annotation lookup : candidatesInContext) {
                if (addedAnnotationIDs.contains(lookup.getId())) continue;

                // collect candidate locations at this lookup
                SortedSet<Location> candidates = new TreeSet<Location>();
                AnnotationSet candidatesInLookup = candidatesInContext.get(lookup.getStartNode().getOffset(), lookup.getEndNode().getOffset());
                for (Annotation candidate : candidatesInLookup) {
                    FeatureMap features = candidate.getFeatures();
                    Object instFeature = features.get("inst");
                    Object classFeature = features.get("class");
                    Object ancestorsFeature = features.get("ancestors");
                    Object latitudeFeature = features.get("latitude");
                    Object longitudeFeature = features.get("longitude");
                    Object populationFeature = features.get("population");
                    Object wikipediaLinksFeature = features.get("wikipediaLinks");

                    // should not happen according to GATE documentation
                    if (instFeature == null || classFeature == null || ancestorsFeature == null || latitudeFeature == null || longitudeFeature == null) {
                        throw new ExecutionException("GATE sucks");
                    }

                    String url = (String) instFeature;
                    int id = Integer.parseInt(url.substring(GEONAMES_ID_PREFIX.length(), url.length() - GEONAMES_ID_SUFFIX.length()));

                    String type = (String) classFeature;
                    type = type.substring(GEONAMES_TYPE_PREFIX.length());

                    double latitude = Double.parseDouble((String) ((Collection) latitudeFeature).iterator().next());
                    double longitude = Double.parseDouble((String) ((Collection) longitudeFeature).iterator().next());

                    int population = 0;
                    if (populationFeature != null) population = Integer.parseInt((String) ((Collection) populationFeature).iterator().next());

                    int numLinks = 0;
                    if (wikipediaLinksFeature != null) numLinks = ((Collection) wikipediaLinksFeature).size();

                    Set<Integer> ancestors = new HashSet<Integer>();
                    for (Object ancestorsFeatureItem : (Collection) ancestorsFeature) {
                        String ancestor = (String) ancestorsFeatureItem;
                        ancestors.add(Integer.valueOf(ancestor.substring(GEONAMES_ID_PREFIX.length(), ancestor.length() - GEONAMES_ID_SUFFIX.length())));
                    }

                    candidates.add(new Location(id, type, latitude, longitude, population, numLinks, ancestors));
                    addedAnnotationIDs.add(candidate.getId());
                }

                lookups.add(candidates);
            }

            SortedSet<Location> locations = Disambiguator.disambiguate(lookups);
        }
    }
}
