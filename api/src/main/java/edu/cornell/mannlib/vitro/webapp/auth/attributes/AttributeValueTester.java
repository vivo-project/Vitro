package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

public class AttributeValueTester {
    private static final Log log = LogFactory.getLog(AttributeValueTester.class);

    static boolean test(Attribute attr, AuthorizationRequest ar, String... values) {
        TestType testType = attr.getTestType();
        switch (testType) {
        case EQUALS:
            return equals(attr, values);
        case NOT_EQUALS:
            return !equals(attr, values);
        case ONE_OF:
            return contains(attr, values);
        case NOT_ONE_OF:
            return !contains(attr, values);
        case STARTS_WITH:
            return startsWith(attr, values);
        case SPARQL_SELECT_QUERY_CONTAINS:
            return sparqlQueryContains(attr, ar, values);
        default: 
            return false;
        }
    }

    private static boolean sparqlQueryContains(Attribute attr, AuthorizationRequest ar, String[] inputValues) {
        Set<String> values = attr.getValues();
        final int valuesSize = values.size();
        if(valuesSize != 1) {
            log.error("SparqlQueryContins value != 1");
            return false;
        }
        String queryTemplate = values.iterator().next();
        if (StringUtils.isBlank(queryTemplate)) {
            log.error("SparqlQueryContins template is empty");
            return false;
        }
        AccessObject ao = ar.getAccessObject();
        Model m = ao.getStatementOntModel();
        if (m == null) {
            log.error("SparqlQueryContains model is not provided");
            return false;
        }
        List<String> personUris = ar.getEditorUris();
        if (personUris.isEmpty()) {
            if (queryTemplate.contains("?personUri")) {
                log.debug("Subject has no person URIs");
                return false;    
            } else {
                personUris.add("");
            }
        }
        List<String> resourceUris = Arrays.asList(ao.getResourceUris());
        return ProximityChecker.isAanyRelated(m, resourceUris, personUris, queryTemplate);
    }

    private static boolean contains(Attribute attr, String... inputValues) {
        final Set<String> values = attr.getValues();
        for (String inputValue : inputValues) {
            if(values.contains(inputValue)){
                return true;
            }
        }
        return false;
    }

    private static boolean equals(Attribute attr, String... inputValues) {
        Set<String> values = attr.getValues();
        final int valuesSize = values.size();
        if(valuesSize != 1) {
            return false;
        }
        String value = values.iterator().next();
        for (String inputValue : inputValues) {
            if(value.equals(inputValue)){
                return true;
            }
        }
        return false;
    }
    
    private static boolean startsWith(Attribute attr, String... inputValues) {
        Set<String> values = attr.getValues();
        final int valuesSize = values.size();
        if(valuesSize != 1) {
            return false;
        }
        String value = values.iterator().next();
        for (String inputValue : inputValues) {
            if(inputValue != null && inputValue.startsWith(value)){
                return true;
            }
        }
        return false;
    }

}
