/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.arraysToLists;
import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.listsToArrays;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderUtilities.EnhancedDataDistributionContext;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import stubs.edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextStub;

/**
 * TODO
 */
public class GraphBuilderUtilitiesTest extends AbstractTestClass {

    private DataDistributorContextStub original;
    private EnhancedDataDistributionContext enhanced;
    private Map<String, String[]> expected;
    private Map<String, String> additions;

    @Before
    public void setup() {
        original = new DataDistributorContextStub(
                ModelFactory.createDefaultModel());
        original.setParameterMap(
                map().add("existingKey", "existingValue").toParameterMap());
        enhanced = new EnhancedDataDistributionContext(original);
    }
    // ----------------------------------------------------------------------
    // The tests: EnhancedDistributionContext
    // ----------------------------------------------------------------------

    @Test
    public void createANewParameter() {
        expected = map() //
                .add("newKey", "newValue1") //
                .add("existingKey", "existingValue") //
                .toParameterMap();

        enhanced.addParameterValue("newKey", "newValue1");
        assertExpectedMap(enhanced.getRequestParameters());
    }

    @Test
    public void addAValueToAnExistingParameter() {
        expected = map() //
                .add("existingKey", "existingValue", "newValue1") //
                .toParameterMap();

        enhanced.addParameterValue("existingKey", "newValue1");
        assertExpectedMap(enhanced.getRequestParameters());
    }

    @Test
    public void addTwoValuesToAnExistingParameter() {
        expected = map() //
                .add("existingKey", "existingValue", "newValue1", "newValue2") //
                .toParameterMap();

        enhanced.addParameterValue("existingKey", "newValue1");
        enhanced.addParameterValue("existingKey", "newValue2");
        assertExpectedMap(enhanced.getRequestParameters());
    }

    @Test
    public void createNewParameterWithTwoIdenticalValues() {
        expected = map() //
                .add("newKey", "newValue", "newValue") //
                .add("existingKey", "existingValue") //
                .toParameterMap();

        enhanced.addParameterValue("newKey", "newValue");
        enhanced.addParameterValue("newKey", "newValue");
        assertExpectedMap(enhanced.getRequestParameters());
    }

    @Test
    public void addMultipleParametersFromAMap() {
        expected = map() //
                .add("newKey", "newValue") //
                .add("existingKey", "existingValue", "anotherValue") //
                .toParameterMap();

        additions = new HashMap<String, String>() {
            {
                put("newKey", "newValue");
                put("existingKey", "anotherValue");
            }
        };
        enhanced.addParameterValues(additions);
        assertExpectedMap(enhanced.getRequestParameters());
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    private static ListsMap map() {
        return new ListsMap();
    }

    private void assertExpectedMap(Map<String, String[]> actual) {
        assertEquals(arraysToLists(expected), arraysToLists(actual));
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    private static class ListsMap {
        Map<String, List<String>> parameters = new HashMap<>();

        public ListsMap add(String key, String... values) {
            if (!parameters.containsKey(key)) {
                parameters.put(key, new ArrayList<>());
            }
            parameters.get(key).addAll(Arrays.asList(values));
            return this;
        }

        public Map<String, String[]> toParameterMap() {
            return listsToArrays(parameters);
        }
    }
}