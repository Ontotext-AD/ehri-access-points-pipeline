package com.ontotext.ehri.lodis;

import java.util.Set;

public class Location implements Comparable<Location> {
    private static final String[] TYPE_PRIORITY = {

            // countries and major cities
            "A.PCL",   // political entity
            "P.PPLC",  // capital of a political entity
            "P.PPLA",  // seat of a first-order administrative division

            // important landmasses
            "L.CONT",  // continent
            "L.RGN",   // region
            "T.PEN",   // peninsula
            "T.ISL",   // island

            // bodies of water
            "H.OCN",   // ocean
            "H.SEA",   // sea
            "H.LK",    // lake
            "H.STM",   // stream

            // concentration camps
            "S.HSTS",  // historical site
            "S.PRN",   // prison
            "S.MNMT",  // monument
            "S.CMTY",  // cemetery

            // minor cities
            "P.PPLA2", // seat of a second-order administrative division
            "P.PPLA3", // seat of a third-order administrative division
            "P.PPLA4", // seat of a fourth-order administrative division
            "P.PPL",   // populated place

            // administrative divisions
            "A.ADM1",  // first-order administrative division
            "A.ADM2",  // second-order administrative division
            "A.ADM3",  // third-order administrative division
            "A.ADM4",  // fourth-order administrative division
            "A.ADM5",  // fifth-order administrative division
            "A.ADMD",  // administrative division

            // class priority
            "P", "A", "L", "T", "V", "H", "U", "R", "S"
    };

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
            else if (thisMatches) return -1;
            else if (otherMatches) return +1;
        }

        // prefer more populated places
        if (population > other.population) return -1;
        if (population < other.population) return +1;

        // comply with the definition of equality
        return Integer.compare(id, other.id);
    }
}
