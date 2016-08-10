package com.ontotext.ehri.lodis;

import java.util.Set;

public class Location implements Comparable<Location> {
    private static final String ADMINISTRATIVE_UNIT_PREFIX = "A.ADM";
    private static final String POPULATED_PLACE_PREFIX = "P.PPL";

    private int id;
    private String type;
    private double latitude, longitude;
    private long population;
    private int numLinks;
    private Set<Integer> ancestors;

    public Location(int id, String type, double latitude, double longitude, long population, int numLinks, Set<Integer> ancestors) {
        this.id = id;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.population = population;
        this.numLinks = numLinks;
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

    public int getNumLinks() {
        return numLinks;
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

        // prefer locations with more links
        if (numLinks > other.numLinks) return -1;
        if (numLinks < other.numLinks) return +1;

        // prefer populated places to administrative units
        if (type.startsWith(POPULATED_PLACE_PREFIX) && other.type.startsWith(ADMINISTRATIVE_UNIT_PREFIX)) return -1;
        if (type.startsWith(ADMINISTRATIVE_UNIT_PREFIX) && other.type.startsWith(POPULATED_PLACE_PREFIX)) return +1;

        // prefer more populated places
        if (population > other.population) return -1;
        if (population < other.population) return +1;

        // comply with the definition of equality
        return Integer.compare(id, other.id);
    }
}
