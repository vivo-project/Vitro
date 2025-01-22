/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.modelToStrings;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import stubs.edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextStub;

/**
 * TODO
 */
public class DrilldownGraphBuilderTest extends AbstractTestClass {
    private static final String IGNORED = "http://ignore/me";
    private static final String SUBJECT_URI = "http://subject/uri";
    private static final String PROPERTY_URI_1 = "http://property/uri_1";
    private static final String PROPERTY_URI_2 = "http://property/uri_2";
    private static final String VALUE_1 = "value_1";
    private static final String VALUE_2 = "value_2";
    private static final String RESPONSE_SUBJECT_1 = "http://responding_1";
    private static final String RESPONSE_SUBJECT_2 = "http://responding_2";

    private static final Set<Map<String, List<String>>> EXPECTED_TOP_LEVEL_MAPS = set(
            map(entry("subject", list(SUBJECT_URI))));
    private static final Set<Map<String, List<String>>> EXPECTED_CHILD_MAPS = set(
            map(entry("subject", list(SUBJECT_URI)),
                    entry("pred", list(PROPERTY_URI_1)),
                    entry("obj", list(VALUE_1))),
            map(entry("subject", list(SUBJECT_URI)),
                    entry("pred", list(PROPERTY_URI_2)),
                    entry("obj", list(VALUE_2))));

    // Output of all builders, top-level and child
    private static final Model EXPECTED_RESULT_GRAPH = model(
            dataProperty(SUBJECT_URI, PROPERTY_URI_1, VALUE_1),
            dataProperty(SUBJECT_URI, PROPERTY_URI_2, VALUE_2),
            dataProperty(IGNORED, PROPERTY_URI_1, VALUE_1),
            dataProperty(IGNORED, PROPERTY_URI_2, VALUE_2),
            dataProperty(RESPONSE_SUBJECT_1, PROPERTY_URI_1, VALUE_1),
            dataProperty(RESPONSE_SUBJECT_1, PROPERTY_URI_2, VALUE_2),
            dataProperty(RESPONSE_SUBJECT_2, PROPERTY_URI_1, VALUE_1),
            dataProperty(RESPONSE_SUBJECT_2, PROPERTY_URI_2, VALUE_2));

    @Test
    public void omnibusTest() throws DataDistributorException {
        GraphBuilderStub topLevelBuilder1 = new GraphBuilderStub().setGraph( //
                model( // provide one row for the SELECT results
                        dataProperty(SUBJECT_URI, PROPERTY_URI_1, VALUE_1),
                        dataProperty(IGNORED, PROPERTY_URI_1, VALUE_1)));
        GraphBuilderStub topLevelBuilder2 = new GraphBuilderStub().setGraph( //
                model( // //provide one row for the SELECT results
                        dataProperty(SUBJECT_URI, PROPERTY_URI_2, VALUE_2),
                        dataProperty(IGNORED, PROPERTY_URI_2, VALUE_2)));

        GraphBuilderStub childBuilder1 = new AugmentedGraphBuilderStub(
                RESPONSE_SUBJECT_1);
        GraphBuilderStub childBuilder2 = new AugmentedGraphBuilderStub(
                RESPONSE_SUBJECT_2);

        DataDistributorContextStub context = new DataDistributorContextStub(
                model()).setParameter("subject", SUBJECT_URI);

        DrillDownGraphBuilder mainBuilder = new DrillDownGraphBuilder();

        mainBuilder.addTopLevelGraphBuilder(topLevelBuilder1);
        mainBuilder.addTopLevelGraphBuilder(topLevelBuilder2);
        mainBuilder.addUriBindingName("subject");
        mainBuilder.setDrillDownQuery(
                "SELECT ?pred ?obj WHERE { ?subject ?pred ?obj }");
        mainBuilder.addChildGraphBuilder(childBuilder1);
        mainBuilder.addChildGraphBuilder(childBuilder2);

        Model actual = mainBuilder.buildGraph(context);

        assertEquals("TOP1 parameter maps", EXPECTED_TOP_LEVEL_MAPS,
                set(topLevelBuilder1.getParameterMaps()));
        assertEquals("TOP2 parameter maps", EXPECTED_TOP_LEVEL_MAPS,
                set(topLevelBuilder2.getParameterMaps()));

        assertEquals("CHILD1 parameter maps", EXPECTED_CHILD_MAPS,
                set(childBuilder1.getParameterMaps()));
        assertEquals("CHILD2 parameter maps", EXPECTED_CHILD_MAPS,
                set(childBuilder2.getParameterMaps()));

        assertEquals(modelToStrings(EXPECTED_RESULT_GRAPH),
                modelToStrings(actual));
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    @SafeVarargs
    private static List<String> list(String... elements) {
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

    @SafeVarargs
    private static Set<Map<String, List<String>>> set(
            Map<String, List<String>>... maps) {
        return new HashSet<>(Arrays.asList(maps));
    }

    private Object set(List<Map<String, List<String>>> maps) {
        return new HashSet<>(maps);
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    /**
     * Rather than a pre-recorded response, the response is a model that
     * reflects the request parameters.
     */
    private static class AugmentedGraphBuilderStub extends GraphBuilderStub {
        private final String responseSubject;

        public AugmentedGraphBuilderStub(String responseSubject) {
            this.responseSubject = responseSubject;
        }

        @Override
        public Model buildGraph(DataDistributorContext ddContext)
                throws DataDistributorException {
            super.buildGraph(ddContext);
            return model(dataProperty(responseSubject,
                    ddContext.getRequestParameters().get("pred")[0],
                    ddContext.getRequestParameters().get("obj")[0]));
        }

    }
}
