/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import stubs.edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextStub;

/**
 * Check to see that the subordinate GraphBuilder(s) is getting a suitably
 * augmented parameter map.
 */
public class IteratingGraphBuilderTest extends AbstractTestClass {
    private static final List<Map<String, List<String>>> EXPECTED_PARAMETER_MAPS = list(
            map(entry("subjectArea", list("cancer"))),
            map(entry("subjectArea", list("tissues"))),
            map(entry("subjectArea", list("echidnae"))));

    @Test
    public void knockThreeTimes() throws DataDistributorException {
        GraphBuilderStub childBuilder = new GraphBuilderStub();

        IteratingGraphBuilder iteratingBuilder = new IteratingGraphBuilder();
        iteratingBuilder.setParameterName("subjectArea");
        iteratingBuilder.addParameterValue("cancer");
        iteratingBuilder.addParameterValue("tissues");
        iteratingBuilder.addParameterValue("echidnae");
        iteratingBuilder.addChildGraphBuilder(childBuilder);

        iteratingBuilder.buildGraph(new DataDistributorContextStub(model()));

        assertEquals(EXPECTED_PARAMETER_MAPS, childBuilder.getParameterMaps());
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    @SafeVarargs
    private static <T> List<T> list(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    private static Map.Entry<String, List<String>> entry(String key,
            List<String> value) {
        Map<String, List<String>> map = new HashMap<>();
        map.put(key, value);
        return map.entrySet().iterator().next();
    }

    @SafeVarargs
    private static Map<String, List<String>> map(
            Map.Entry<String, List<String>>... entries) {
        Map<String, List<String>> map = new HashMap<>();
        for (Entry<String, List<String>> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

}
