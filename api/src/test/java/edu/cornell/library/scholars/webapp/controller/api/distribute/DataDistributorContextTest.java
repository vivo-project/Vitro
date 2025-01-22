/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute;

import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.arraysToLists;
import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.deepCopyParameters;
import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.listsToArrays;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * TODO
 */
public class DataDistributorContextTest extends AbstractTestClass {

    private static final Map<String, String[]> ARRAYS = initArrays();

    private static Map<String, String[]> initArrays() {
        HashMap<String, String[]> map = new HashMap<>();
        map.put("nameZero", new String[] {});
        map.put("nameOne", new String[] { "valueOne" });
        map.put("nameTwo", new String[] { "valueTwoA", "valueTwoB" });
        return map;
    }

    private static final Map<String, List<String>> LISTS = initLists();

    private static Map<String, List<String>> initLists() {
        HashMap<String, List<String>> map = new HashMap<>();
        map.put("nameZero", new ArrayList<>());
        map.put("nameOne", new ArrayList<>(Arrays.asList("valueOne")));
        map.put("nameTwo",
                new ArrayList<>(Arrays.asList("valueTwoA", "valueTwoB")));
        return map;
    }

    // ----------------------------------------------------------------------
    // The tests
    // ----------------------------------------------------------------------

    @Test
    public void convertArraysToLists() {
        assertEquals(LISTS, arraysToLists(ARRAYS));
    }

    @Test
    public void convertListsToArrays() {
        assertEqualArrayMaps(ARRAYS, listsToArrays(LISTS));
    }

    @Test
    public void deepCopy() {
        assertEqualArrayMaps(ARRAYS, deepCopyParameters(ARRAYS));
    }

    @SuppressWarnings("deprecation")
    private void assertEqualArrayMaps(Map<String, String[]> a1,
            Map<String, String[]> a2) {
        assertEquals("key sets should be equals", a1.keySet(), a2.keySet());
        for (String key : a1.keySet()) {
            assertEquals("array values for '" + key + "'", a1.get(key),
                    a2.get(key));
        }
    }
}
