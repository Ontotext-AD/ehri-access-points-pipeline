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
        Collections.addAll(REQUIRED_CANDIDATE_FEATURES,
                ID_FEATURE_NAME,
                TYPE_FEATURE_NAME,
                ANCESTORS_FEATURE_NAME,
                LATITUDE_FEATURE_NAME,
                LONGITUDE_FEATURE_NAME
        );
    }

    private String annotationSetName;
    private String contextAnnotationType;
    private String candidateAnnotationType;

    private Boolean keepAncestors;

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

    public Boolean getKeepAncestors() {
        return keepAncestors;
    }

    @RunTime
    @CreoleParameter(defaultValue = "false", comment = "Keep disambiguating ancestors or not.")
    public void setKeepAncestors(Boolean keepAncestors) {
        this.keepAncestors = keepAncestors;
    }

    @Override
    public void execute() throws ExecutionException {
        AnnotationSet annotationSet = document.getAnnotations(annotationSetName);
        AnnotationSet candidateAnnotationSet = annotationSet.get(candidateAnnotationType, REQUIRED_CANDIDATE_FEATURES);

        // disambiguate each context separately
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
                    FeatureMap candidateFeatures = candidate.getFeatures();
                    Object instFeature = candidateFeatures.get(ID_FEATURE_NAME);
                    Object classFeature = candidateFeatures.get(TYPE_FEATURE_NAME);
                    Object ancestorsFeature = candidateFeatures.get(ANCESTORS_FEATURE_NAME);
                    Object latitudeFeature = candidateFeatures.get(LATITUDE_FEATURE_NAME);
                    Object longitudeFeature = candidateFeatures.get(LONGITUDE_FEATURE_NAME);
                    Object populationFeature = candidateFeatures.get(POPULATION_FEATURE_NAME);
                    Object wikipediaLinksFeature = candidateFeatures.get(LINKS_FEATURE_NAME);

                    // should not happen according to GATE documentation
                    if (instFeature == null || classFeature == null || ancestorsFeature == null || latitudeFeature == null || longitudeFeature == null) {
                        throw new ExecutionException("GATE sucks");
                    }

                    // parse ID from instance
                    String url = (String) instFeature;
                    int id = Integer.parseInt(url.substring(GEONAMES_ID_PREFIX.length(), url.length() - GEONAMES_ID_SUFFIX.length()));

                    // parse type from class
                    String type = (String) classFeature;
                    type = type.substring(GEONAMES_TYPE_PREFIX.length());

                    // parse latitude and longitude
                    double latitude = Double.parseDouble((String) ((Collection) latitudeFeature).iterator().next());
                    double longitude = Double.parseDouble((String) ((Collection) longitudeFeature).iterator().next());

                    // parse population if present
                    long population = 0;
                    if (populationFeature != null) population = Long.parseLong((String) ((Collection) populationFeature).iterator().next());

                    // parse the number of links if present
                    int numLinks = 0;
                    if (wikipediaLinksFeature != null) numLinks = ((Collection) wikipediaLinksFeature).size();

                    // parse the ancestor IDs
                    Set<Integer> ancestors = new HashSet<Integer>();
                    for (Object ancestorsFeatureItem : (Collection) ancestorsFeature) {
                        String ancestor = (String) ancestorsFeatureItem;
                        ancestors.add(Integer.valueOf(ancestor.substring(GEONAMES_ID_PREFIX.length(), ancestor.length() - GEONAMES_ID_SUFFIX.length())));
                    }

                    // add location to the set of candidates
                    candidates.add(new Location(id, type, latitude, longitude, population, numLinks, ancestors));
                    addedAnnotationIDs.add(candidate.getId());
                }

                // add the set of candidates to the set of lookups
                lookups.add(candidates);
            }

            // collect the instances to keep
            SortedSet<Location> locations = Disambiguator.disambiguate(lookups, keepAncestors);
            Set<String> instancesToKeep = new HashSet<String>();
            for (Location location : locations) {
                instancesToKeep.add(GEONAMES_ID_PREFIX + location.getId() + GEONAMES_ID_SUFFIX);
            }

            // remove other instances
            for (Annotation candidate : candidatesInContext) {
                String instance = (String) candidate.getFeatures().get(ID_FEATURE_NAME);
                if (instance == null || !instancesToKeep.contains(instance)) annotationSet.remove(candidate);
            }

            // add the coordinates of the center as features of the context annotation, because we can
            double[] centerCoordinates = calculateCenterCoordinates(locations);
            context.getFeatures().put(LATITUDE_FEATURE_NAME, centerCoordinates[0]);
            context.getFeatures().put(LONGITUDE_FEATURE_NAME, centerCoordinates[1]);
        }
    }

    /**
     * Calculate the center coordinates of some locations.
     * @param locations Set of locations.
     * @return Array containing the coordinates of the center: latitude and longitude.
     */
    private static double[] calculateCenterCoordinates(Set<Location> locations) {
        double sumLatitudes = 0;
        double sumLongitudes = 0;

        // sum up the coordinates of locations
        for (Location location : locations) {
            sumLatitudes += location.getLatitude();
            sumLongitudes += location.getLongitude();
        }

        // calculate the average coordinates
        double centerLatitude = sumLatitudes / (double) locations.size();
        double centerLongitude = sumLongitudes / (double) locations.size();
        return new double[] { centerLatitude, centerLongitude };
    }
}
