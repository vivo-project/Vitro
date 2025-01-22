/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.util;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.junit.Test;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.MissingParametersException;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;
import stubs.javax.servlet.http.HttpServletRequestStub;

/**
 * test plan
 * 
 * <pre>
 * null parameters throws NPE
 * 
 * null uriBindingNames or literalBindingNames throws NPE
 * 
 * null query throws NPE
 * 
 * missing parameter throws MissingPE, 
 * multiple parameter values throws MissingPE
 * 
 * show binding of zero/zero, one/one, zero/three, three/zero
 * </pre>
 */

public class VariableBinderTest extends AbstractTestClass {
    private static final Map<String, String[]> NO_PARAMETERS = Collections.emptyMap();
    private static final QueryHolder BASIC_QUERY = new QueryHolder("SELECT * WHERE { ?s ?p ?o }");
    
    private static final String URI_1_NAME = "uri_1";
    private static final String URI_1_VALUE = "http://test/uri_1";
    private static final String URI_2_NAME = "uri_2";
    private static final String URI_2_VALUE = "http://test/uri_2";
    private static final String URI_3_NAME = "uri_3";
    private static final String URI_3_VALUE = "http://test/uri_3";
    private static final String LITERAL_1_NAME = "literal_1";
    private static final String LITERAL_1_VALUE = "value_1";
    private static final String LITERAL_2_NAME = "literal_2";
    private static final String LITERAL_2_VALUE = "value_2";
    private static final String LITERAL_3_NAME = "literal_3";
    private static final String LITERAL_3_VALUE = "value_3";
    private static final String RAW_ZERO_ZERO = "SELECT * WHERE { ?s ?p ?o }";
    private static final String EXPECTED_ZERO_ZERO = "SELECT * WHERE { ?s ?p ?o }";
    private static final String RAW_ONE_ONE = "SELECT * WHERE { ?%s rdfs:label ?%s }";
    private static final String EXPECTED_ONE_ONE = "SELECT * WHERE { <%s> rdfs:label \"%s\" }";
    private static final String RAW_THREE_ZERO = "SELECT * WHERE { ?%s ?%s ?%s }";
    private static final String EXPECTED_THREE_ZERO = "SELECT * WHERE { <%s> <%s> <%s> }";
    private static final String RAW_ZERO_THREE = "SELECT * WHERE { ?s rdfs:label ?%s, ?%s, ?%s }";
    private static final String EXPECTED_ZERO_THREE = "SELECT * WHERE { ?s rdfs:label \"%s\", \"%s\", \"%s\" }";

    private VariableBinder binder;
    private QueryHolder input;
    private QueryHolder expected;
    private QueryHolder actual;

    // ----------------------------------------------------------------------
    // The tests
    // ----------------------------------------------------------------------
    
    @Test
    public void nullParameters_throwsException() {
        expectException(NullPointerException.class, "parameters");
        binder = new VariableBinder(null);
    }

    @Test
    public void nullUriBindingNames_throwsException() throws DataDistributorException {
        expectException(NullPointerException.class, "uriBindingNames");
        binder = new VariableBinder(NO_PARAMETERS);
        binder.bindValuesToQuery(null, set(), BASIC_QUERY);
    }

    @Test
    public void nullLiteralBindingNames_throwsException() throws DataDistributorException {
        expectException(NullPointerException.class, "literalBindingNames");
        binder = new VariableBinder(NO_PARAMETERS);
        binder.bindValuesToQuery(set(), null, BASIC_QUERY);
    }

    @Test
    public void nullQuery_throwsException() throws DataDistributorException {
        expectException(NullPointerException.class, "query");
        binder = new VariableBinder(NO_PARAMETERS);
        binder.bindValuesToQuery(set(), set(), null);
    }
    
    @Test
    public void missingParameterValue_throwsException() throws DataDistributorException {
        expectException(MissingParametersException.class, "is required");
        binder = new VariableBinder(NO_PARAMETERS);
        binder.bindValuesToQuery(set(URI_1_NAME), set(), BASIC_QUERY);
    }

    @Test
    public void multipleParameterValues_throwsException() throws DataDistributorException {
        expectException(MissingParametersException.class, "multiple values");
        
        Map<String, String[]> pMap = new HashMap<>();
        pMap.put(LITERAL_3_NAME, new String[] {LITERAL_1_VALUE, LITERAL_3_VALUE});
        
        binder = new VariableBinder(pMap);
        binder.bindValuesToQuery(set(), set(LITERAL_3_NAME), BASIC_QUERY);
    }

    @Test
    public void bind_zero_and_zero() throws DataDistributorException {
        binder = variableBinder();

        input = formatQh(RAW_ZERO_ZERO);
        expected = formatQh(EXPECTED_ZERO_ZERO);
        actual = binder.bindValuesToQuery(set(), set(), input);

        assertBinding();
    }

    @Test
    public void bind_one_and_one() throws DataDistributorException {
        binder = variableBinder( //
                parameter(URI_1_NAME, URI_1_VALUE),
                parameter(LITERAL_1_NAME, LITERAL_1_VALUE));

        input = formatQh(RAW_ONE_ONE, URI_1_NAME, LITERAL_1_NAME);
        expected = formatQh(EXPECTED_ONE_ONE, URI_1_VALUE, LITERAL_1_VALUE);
        actual = binder.bindValuesToQuery(set(URI_1_NAME), set(LITERAL_1_NAME),
                input);

        assertBinding();
    }

    @Test
    public void bind_three_and_zero() throws DataDistributorException {
        binder = variableBinder( //
                parameter(URI_1_NAME, URI_1_VALUE),
                parameter(URI_2_NAME, URI_2_VALUE),
                parameter(URI_3_NAME, URI_3_VALUE));

        input = formatQh(RAW_THREE_ZERO, URI_1_NAME, URI_2_NAME, URI_3_NAME);
        expected = formatQh(EXPECTED_THREE_ZERO, URI_1_VALUE, URI_2_VALUE,
                URI_3_VALUE);
        actual = binder.bindValuesToQuery(
                set(URI_1_NAME, URI_2_NAME, URI_3_NAME), set(), input);

        assertBinding();
    }

    @Test
    public void bind_zero_and_three() throws DataDistributorException {
        binder = variableBinder( //
                parameter(LITERAL_1_NAME, LITERAL_1_VALUE),
                parameter(LITERAL_2_NAME, LITERAL_2_VALUE),
                parameter(LITERAL_3_NAME, LITERAL_3_VALUE));

        input = formatQh(RAW_ZERO_THREE, LITERAL_1_NAME, LITERAL_2_NAME,
                LITERAL_3_NAME);
        expected = formatQh(EXPECTED_ZERO_THREE, LITERAL_1_VALUE,
                LITERAL_2_VALUE, LITERAL_3_VALUE);
        actual = binder.bindValuesToQuery(set(),
                set(LITERAL_1_NAME, LITERAL_2_NAME, LITERAL_3_NAME), input);

        assertBinding();
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private VariableBinder variableBinder(Parm... parms) {
        HttpServletRequestStub req = new HttpServletRequestStub();
        for (Parm p : parms) {
            req.addParameter(p.name, p.value);
        }
        return new VariableBinder(req.getParameterMap());
    }

    private Parm parameter(String name, String value) {
        return new Parm(name, value);
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> set(T... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    private QueryHolder formatQh(String template, Object... values) {
        return new QueryHolder(String.format(template, values));
    }

    private void assertBinding() {
        String message = String.format("unexpected binding: \n" //
                + "   input    = '%s' \n" //
                + "   expected = '%s' \n" //
                + "   actual   = '%s'", input, expected, actual);
        // System.out.println(message);

        if (!Objects.equals(expected, actual)) {
            fail(message);
        }
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    private static class Parm {
        final String name;
        final String value;

        public Parm(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
