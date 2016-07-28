package com.ontotext.ehri.tests;

import com.ontotext.ehri.lodis.Location;
import org.junit.Test;

import java.util.*;

import static com.ontotext.ehri.lodis.Disambiguator.disambiguate;
import static org.junit.Assert.assertTrue;

public class DisambiguatorTests {

    /**
     * https://jira.ontotext.com/browse/EHRI-172
     */
    @Test
    public void testDisambiguation() {

        // test recursive stop
        Set<SortedSet<Location>> locations = new HashSet<SortedSet<Location>>();
        assertTrue("expected an empty set", disambiguate(locations, false).isEmpty());

        // test popularity sorting
        SortedSet<Location> lookup = new TreeSet<Location>();
        lookup.add(new Location(1, "A.ADM", 0.0, 0.0, 1000, 0, new HashSet<Integer>()));
        lookup.add(new Location(2, "A.ADM", 0.0, 0.0, 5000, 0, new HashSet<Integer>()));
        locations.add(lookup);
        SortedSet<Location> disambiguated = disambiguate(locations, false);
        assertTrue("expected one result", disambiguated.size() == 1);
        assertTrue("expected candidate with larger population", disambiguated.iterator().next().getId() == 2);
        locations.iterator().next().add(new Location(3, "P.PPL", 0.0, 0.0, 0, 0, new HashSet<Integer>()));
        assertTrue("expected candidate which is a populated place", disambiguate(locations, false).iterator().next().getId() == 3);
        locations.iterator().next().add(new Location(4, "A.ADM", 0.0, 0.0, 0, 1, new HashSet<Integer>()));
        assertTrue("expected candidate with more links", disambiguate(locations, false).iterator().next().getId() == 4);

        // test basic ancestor chaining without disambiguation ancestors
        locations = new HashSet<SortedSet<Location>>();
        SortedSet<Location> lookupOne = new TreeSet<Location>();
        lookupOne.add(new Location(1, "P.PPL", 0.0, 0.0, 0, 0, new HashSet<Integer>(Arrays.asList(4))));
        lookupOne.add(new Location(2, "P.PPL", 0.0, 0.0, 0, 0, new HashSet<Integer>()));
        SortedSet<Location> lookupTwo = new TreeSet<Location>();
        lookupTwo.add(new Location(3, "P.PPL", 0.0, 0.0, 0, 0, new HashSet<Integer>()));
        lookupTwo.add(new Location(4, "P.PPL", 0.0, 0.0, 0, 1, new HashSet<Integer>()));
        locations.add(lookupOne);
        locations.add(lookupTwo);
        disambiguated = disambiguate(locations, false);
        assertTrue("expected one result", disambiguated.size() == 1);
        assertTrue("expected candidate with ancestor", disambiguated.iterator().next().getId() == 1);

        // test basic ancestor chaining with disambiguation ancestors
        disambiguated = disambiguate(locations, true);
        assertTrue("expected two results", disambiguated.size() == 2);
        Iterator<Location> iterator = disambiguated.iterator();
        assertTrue("expected candidate with more links first", iterator.next().getId() == 4);
        assertTrue("expected candidate with less links second", iterator.next().getId() == 1);

        // test advanced ancestor chaining without disambiguation ancestors
        locations = new HashSet<SortedSet<Location>>();
        lookupOne = new TreeSet<Location>();
        lookupOne.add(new Location(1, "P.PPL", 0.0, 0.0, 0, 2, new HashSet<Integer>(Arrays.asList(4))));
        lookupOne.add(new Location(2, "P.PPL", 0.0, 0.0, 0, 0, new HashSet<Integer>()));
        lookupTwo = new TreeSet<Location>();
        lookupTwo.add(new Location(3, "P.PPL", 0.0, 0.0, 0, 1, new HashSet<Integer>(Arrays.asList(2))));
        lookupTwo.add(new Location(4, "P.PPL", 0.0, 0.0, 0, 0, new HashSet<Integer>()));
        SortedSet<Location> lookupThree = new TreeSet<Location>();
        lookupThree.add(new Location(5, "P.PPL", 0.0, 0.0, 0, 0, new HashSet<Integer>()));
        lookupThree.add(new Location(6, "P.PPL", 0.0, 0.0, 0, 1, new HashSet<Integer>()));
        locations.add(lookupOne);
        locations.add(lookupTwo);
        locations.add(lookupThree);
        disambiguated = disambiguate(locations, false);
        assertTrue("expected two results", disambiguated.size() == 2);
        iterator = disambiguated.iterator();
        assertTrue("expected candidate with ancestor first", iterator.next().getId() == 1);
        assertTrue("expected candidate with more links second", iterator.next().getId() == 6);

        // test advanced ancestor chaining with disambiguation ancestors
        disambiguated = disambiguate(locations, true);
        assertTrue("expected three results", disambiguated.size() == 3);
        iterator = disambiguated.iterator();
        assertTrue("expected candidate with ancestor first", iterator.next().getId() == 1);
        assertTrue("expected candidate with more links second", iterator.next().getId() == 6);
        assertTrue("expected disambiguating ancestor third", iterator.next().getId() == 4);
    }
}
