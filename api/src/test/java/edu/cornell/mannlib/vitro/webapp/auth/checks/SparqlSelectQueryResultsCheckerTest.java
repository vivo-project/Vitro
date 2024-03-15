package edu.cornell.mannlib.vitro.webapp.auth.checks;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.webapp.auth.checks.SparqlSelectQueryResultsChecker.sparqlSelectQueryResultsContain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.MutableAttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.objects.NamedAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.TestAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.junit.Before;
import org.junit.Test;
import stubs.edu.cornell.mannlib.vitro.webapp.config.ConfigurationPropertiesStub;

public class SparqlSelectQueryResultsCheckerTest {

    private static final String SIMPLE_QUERY = "SELECT ?value WHERE {?subject ?property ?value .}";
    private static final String NOT_PRESENT_VALUE = "not present value";
    private static final String TEST_URI = "test:uri";
    private static final String TEST_PROPERTY = "test:property";
    private static final String PRESENT_VALUE = "present value";

    @Before
    public void init() {
        ConfigurationProperties configuration = new ConfigurationPropertiesStub();
        ConfigurationProperties.setInstance(configuration);
    }

    @Test
    public void testSimpleQuery() {
        assertTrue(QueryResultsMapCache.get().isEmpty());
        MutableAttributeValueSet values = new MutableAttributeValueSet(PRESENT_VALUE);
        Check check = new StatementObjectUriCheck(TEST_URI, values);
        check.setConfiguration(SIMPLE_QUERY);
        NamedAccessObject ao = new NamedAccessObject(TEST_URI);
        OntModel model = new OntModelImpl(OntModelSpec.OWL_MEM);
        model.add(dataProperty(TEST_URI, TEST_PROPERTY, PRESENT_VALUE));
        ao.setModel(model);
        AuthorizationRequest ar = new TestAuthorizationRequest(ao, AccessOperation.DISPLAY);
        boolean result = SparqlSelectQueryResultsChecker.sparqlSelectQueryResultsContain(check, ar, null);
        assertTrue(result);
    }

    @Test
    public void testCacheNotCorruptedAfterCaching() throws IOException {
        try (QueryResultsMapCache cache = new QueryResultsMapCache();) {
            assertTrue(QueryResultsMapCache.get().isEmpty());
            MutableAttributeValueSet values = new MutableAttributeValueSet(NOT_PRESENT_VALUE);
            Check check = new StatementObjectUriCheck(TEST_URI, values);
            check.setConfiguration(SIMPLE_QUERY);
            NamedAccessObject ao = new NamedAccessObject(TEST_URI);
            OntModel model = new OntModelImpl(OntModelSpec.OWL_MEM);
            model.add(dataProperty(TEST_URI, TEST_PROPERTY, PRESENT_VALUE));
            ao.setModel(model);
            AuthorizationRequest ar = new TestAuthorizationRequest(ao, AccessOperation.DISPLAY);
            assertFalse(sparqlSelectQueryResultsContain(check, ar, null));
            assertEquals(1, QueryResultsMapCache.get().size());
            values.remove(NOT_PRESENT_VALUE);
            values.add(PRESENT_VALUE);
            // Test that SPARQL results weren't modified after being cached
            assertTrue(sparqlSelectQueryResultsContain(check, ar, null));
        }
    }

    @Test
    public void testCacheResultsNotCorruptedOnReuse() throws IOException {
        try (QueryResultsMapCache cache = new QueryResultsMapCache();) {
            assertTrue(QueryResultsMapCache.get().isEmpty());
            MutableAttributeValueSet values = new MutableAttributeValueSet(PRESENT_VALUE);
            Check check = new StatementObjectUriCheck(TEST_URI, values);
            check.setConfiguration(SIMPLE_QUERY);
            NamedAccessObject ao = new NamedAccessObject(TEST_URI);
            OntModel model = new OntModelImpl(OntModelSpec.OWL_MEM);
            model.add(dataProperty(TEST_URI, TEST_PROPERTY, PRESENT_VALUE));
            ao.setModel(model);
            AuthorizationRequest ar = new TestAuthorizationRequest(ao, AccessOperation.DISPLAY);
            assertTrue(sparqlSelectQueryResultsContain(check, ar, null));
            assertEquals(1, QueryResultsMapCache.get().size());
            values.remove(PRESENT_VALUE);
            values.add(NOT_PRESENT_VALUE);
            assertFalse(sparqlSelectQueryResultsContain(check, ar, null));
            values.remove(NOT_PRESENT_VALUE);
            values.add(PRESENT_VALUE);
            // Test that SPARQL results weren't modified by previous cache client
            assertTrue(sparqlSelectQueryResultsContain(check, ar, null));
        }
    }

}
