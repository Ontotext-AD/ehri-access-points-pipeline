package com.ontotext.ehri.lodis;

import java.util.Set;

public class Location implements Comparable<Location> {
    private static final String[] TYPE_PRIORITY = {

            // continents and oceans
            "L.CONT",  // continent
            "H.OCN",   // ocean

            // countries and capitals
            "A.PCLH",  // historical political entity
            "A.PCLI",  // independent political entity
            "P.PPLC",  // capital of a political entity

            // major cities and administrative divisions
            "P.PPLA",  // seat of a first-order administrative division
            "A.ADM1",  // first-order administrative division

            // concentration camps
            "S.HSTS",  // historical site
            "S.PRN",   // prison
            "S.MNMT",  // monument
            "S.CMTY",  // cemetery

            // cities
            "P.PPLA2", // seat of a second-order administrative division
            "P.PPLA3", // seat of a third-order administrative division
            "P.PPLA4", // seat of a fourth-order administrative division

            // landmasses
            "T.PEN",   // peninsula
            "T.ISL",   // island
            "T.MTS",   // mountains
            "L.RGN",   // region

            // bodies of water
            "H.SEA",   // sea
            "H.LK",    // lake
            "H.STM",   // stream

            // administrative divisions
            "A.ADM2",  // second-order administrative division
            "A.ADM3",  // third-order administrative division
            "A.ADM4",  // fourth-order administrative division
            "A.ADM5",  // fifth-order administrative division
            "A.ADMD",  // administrative division
    };

    private static final String[] CLASS_PRIORITY = { "P", "A", "L", "T", "V", "H", "U", "R", "S" };

    private int id;
    private String type;
    private double latitude, longitude;
    private long population;
    private Set<Integer> ancestors;

    public Location(int id, String type, double latitude, double longitude, long population, Set<Integer> ancestors) {
        this.id = id;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.population = population;
        this.ancestors = ancestors;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getPopulation() {
        return population;
    }

    public Set<Integer> getAncestors() {
        return ancestors;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        Location location = (Location) other;
        return id == location.id;
    }

    @Override
    public int compareTo(Location other) {

        // prefer places according to type priority
        for (String t : TYPE_PRIORITY) {
            boolean thisMatches = type.equals(t);
            boolean otherMatches = other.type.equals(t);

            if (thisMatches && otherMatches) break;
            if (thisMatches) return -1;
            if (otherMatches) return +1;
        }

        // prefer places according to class priority
        String thisClass = type.substring(0, 1);
        String otherClass = other.type.substring(0, 1);
        for (String c : CLASS_PRIORITY) {
            boolean thisMatches = thisClass.equals(c);
            boolean otherMatches = otherClass.equals(c);

            if (thisMatches && otherMatches) break;
            if (thisMatches) return -1;
            if (otherMatches) return +1;
        }

        // prefer more populated places
        if (population > other.population) return -1;
        if (population < other.population) return +1;

        // comply with the definition of equality
        return Integer.compare(id, other.id);
    }
}
