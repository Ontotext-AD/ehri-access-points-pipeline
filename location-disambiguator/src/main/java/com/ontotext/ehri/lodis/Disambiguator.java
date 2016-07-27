package com.ontotext.ehri.lodis;

import java.util.*;

public class Disambiguator {

    public static SortedSet<Location> disambiguate(Set<SortedSet<Location>> lookups, boolean keepAncestors) {
        SortedSet<Location> result = new TreeSet<Location>();
        if (lookups.isEmpty()) return result;

        // map each candidate to its ancestors from other lookups
        Map<Location, Set<Location>> candidate2ancestors = new HashMap<Location, Set<Location>>();
        int maxNumAncestors = 0;

        for (SortedSet<Location> lookup : lookups) {
            for (Location candidate : lookup) {
                Set<Location> ancestors = new HashSet<Location>();

                for (SortedSet<Location> otherLookup : lookups) {
                    if (otherLookup == lookup) continue;
                    for (Location otherCandidate : otherLookup) {

                        // add the first ancestor from the other lookup
                        if (candidate.getAncestors().contains(otherCandidate.getId())) {
                            ancestors.add(otherCandidate);
                            break;
                        }
                    }
                }

                // store the candidate and its ancestors from other lookups
                candidate2ancestors.put(candidate, ancestors);
                if (ancestors.size() > maxNumAncestors) maxNumAncestors = ancestors.size();
            }
        }

        // collect the maximal candidates and sort them by popularity
        SortedSet<Location> maxCandidates = new TreeSet<Location>();
        for (Location candidate : candidate2ancestors.keySet()) {
            if (candidate2ancestors.get(candidate).size() == maxNumAncestors) maxCandidates.add(candidate);
        }

        // find the Chosen One
        Location chosenOne = maxCandidates.iterator().next();
        result.add(chosenOne);

        // add the ancestors of the Chosen One to the result if necessary
        if (keepAncestors) result.addAll(candidate2ancestors.get(chosenOne));

        // collect the remaining lookups
        Set<SortedSet<Location>> remainingLookups = new HashSet<SortedSet<Location>>();
        for (SortedSet<Location> lookup : lookups) {
            if (lookup.contains(chosenOne)) continue;

            // check if this lookup contains an ancestor of the Chosen One
            boolean containsChosenOneAncestor = false;
            for (Location chosenOneAncestor : candidate2ancestors.get(chosenOne)) {
                if (lookup.contains(chosenOneAncestor)) {
                    containsChosenOneAncestor = true;
                    break;
                }
            }

            // add only lookups that do not contain the Chosen One or any of its ancestors
            if (!containsChosenOneAncestor) remainingLookups.add(lookup);
        }

        // recursively disambiguate remaining lookups
        result.addAll(disambiguate(remainingLookups, keepAncestors));
        return result;
    }
}
