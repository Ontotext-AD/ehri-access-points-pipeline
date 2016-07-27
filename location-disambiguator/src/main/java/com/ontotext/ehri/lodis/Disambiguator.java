package com.ontotext.ehri.lodis;

import java.util.Set;
import java.util.SortedSet;

public class Disambiguator {

    public static SortedSet<Location> disambiguate(Set<SortedSet<Location>> lookups) {
        return lookups.iterator().next(); // TODO: implement
    }
}
