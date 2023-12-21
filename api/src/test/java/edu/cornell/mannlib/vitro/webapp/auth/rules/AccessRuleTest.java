package edu.cornell.mannlib.vitro.webapp.auth.rules;

import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.EQUALS;
import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.NOT_EQUALS;
import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.NOT_ONE_OF;
import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.ONE_OF;
import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.SPARQL_SELECT_QUERY_RESULTS_CONTAIN;
import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.SPARQL_SELECT_QUERY_RESULTS_NOT_CONTAIN;
import static org.junit.Assert.assertEquals;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.MutableAttributeValueSet;
import edu.cornell.mannlib.vitro.webapp.auth.checks.AccessObjectUriCheck;
import edu.cornell.mannlib.vitro.webapp.auth.checks.Check;
import org.junit.Test;

public class AccessRuleTest {

    @Test
    public void testAttributeOrderByComputationalCost() {
        AccessRule rule = new FastFailAccessRule();
        Check equalsAttribute = uriCheck("test:equals");
        equalsAttribute.setType(EQUALS);
        Check notEqualsAttribute = uriCheck("test:not-equals");
        notEqualsAttribute.setType(NOT_EQUALS);
        Check oneOfAttribute = uriCheck("test:one-of");
        oneOfAttribute.setType(ONE_OF);
        Check notOneOfAttribute = uriCheck("test:not-one-of");
        notOneOfAttribute.setType(NOT_ONE_OF);
        Check sparqlResultsContainAttribute = uriCheck("test:sparql-results-contain");
        sparqlResultsContainAttribute.setType(SPARQL_SELECT_QUERY_RESULTS_CONTAIN);
        Check sparqlResultsNotContainAttribute = uriCheck("test:sparql-results-not-contain");
        sparqlResultsNotContainAttribute.setType(SPARQL_SELECT_QUERY_RESULTS_NOT_CONTAIN);

        rule.addCheck(notOneOfAttribute);
        rule.addCheck(oneOfAttribute);
        rule.addCheck(sparqlResultsContainAttribute);
        rule.addCheck(equalsAttribute);
        rule.addCheck(notEqualsAttribute);
        rule.addCheck(sparqlResultsNotContainAttribute);

        List<Check> list = rule.getChecks();

        assertEquals(equalsAttribute.getUri(), list.get(0).getUri());
        assertEquals(notEqualsAttribute.getUri(), list.get(1).getUri());
        assertEquals(oneOfAttribute.getUri(), list.get(2).getUri());
        assertEquals(notOneOfAttribute.getUri(), list.get(3).getUri());
        assertEquals(sparqlResultsContainAttribute.getUri(), list.get(4).getUri());
        assertEquals(sparqlResultsNotContainAttribute.getUri(), list.get(5).getUri());
    }

    private AttributeValueSet value(String value) {
        return new MutableAttributeValueSet(value);
    }

    private Check uriCheck(String uri) {
        return new AccessObjectUriCheck(uri, value("test:objectUri"));
    }
}
