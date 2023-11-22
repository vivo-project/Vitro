/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import java.util.Arrays;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;

public class AttributeValueChecker {
    private static final Log log = LogFactory.getLog(AttributeValueChecker.class);

    static boolean test(Check attr, AuthorizationRequest ar, String... values) {
        CheckType testType = attr.getType();
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

    private static boolean sparqlQueryContains(Check attr, AuthorizationRequest ar, String[] inputValues) {
        AttributeValueSet values = attr.getValues();
        if (!values.containsSingleValue()) {
            log.error("SparqlQueryContains more than  one value");
            return false;
        }
        String queryTemplate = values.getSingleValue();
        if (StringUtils.isBlank(queryTemplate)) {
            log.error("SparqlQueryContains template is empty");
            return false;
        }
        AccessObject ao = ar.getAccessObject();
        Model m = ao.getStatementOntModel();
        if (m == null) {
            log.debug("SparqlQueryContains model is not provided");
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
        return ProximityChecker.isAnyRelated(m, resourceUris, personUris, queryTemplate);
    }

    private static boolean contains(Check attr, String... inputValues) {
        AttributeValueSet values = attr.getValues();
        for (String inputValue : inputValues) {
            if (values.contains(inputValue)) {
                return true;
            }
        }
        return false;
    }

    private static boolean equals(Check attr, String... inputValues) {
        AttributeValueSet values = attr.getValues();
        if (!values.containsSingleValue()) {
            return false;
        }
        for (String inputValue : inputValues) {
            if (values.contains(inputValue)) {
                return true;
            }
        }
        return false;
    }

    private static boolean startsWith(Check attr, String... inputValues) {
        AttributeValueSet values = attr.getValues();
        if (!values.containsSingleValue()) {
            return false;
        }
        String value = values.getSingleValue();
        for (String inputValue : inputValues) {
            if (inputValue != null && inputValue.startsWith(value)) {
                return true;
            }
        }
        return false;
    }

}
