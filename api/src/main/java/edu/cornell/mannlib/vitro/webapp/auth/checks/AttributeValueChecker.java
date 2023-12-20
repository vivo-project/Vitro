/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.checks;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AttributeValueChecker {
    static final Log log = LogFactory.getLog(AttributeValueChecker.class);

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
            case SPARQL_SELECT_QUERY_RESULTS_CONTAIN:
                return SparqlSelectQueryResultsChecker.sparqlSelectQueryResultsContain(attr, ar, values);
            case SPARQL_SELECT_QUERY_RESULTS_NOT_CONTAIN:
                return !SparqlSelectQueryResultsChecker.sparqlSelectQueryResultsContain(attr, ar, values);
            default:
                return false;
        }
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
