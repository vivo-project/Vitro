package edu.cornell.mannlib.vitro.webapp.auth.rules;

import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.EQUALS;
import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.ONE_OF;
import static edu.cornell.mannlib.vitro.webapp.auth.checks.CheckType.SPARQL_SELECT_QUERY_CONTAINS;
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
        Check cheapAttribute = uriCheck("test:cheapAttributeUri", value("test:objectUri"));
        cheapAttribute.setType(EQUALS);
        Check affordableAttribute = uriCheck("test:affordableAttributeUri", value("test:objectUri"));
        cheapAttribute.setType(ONE_OF);
        Check expensiveAttribute = uriCheck("test:expensiveAttributeUri", value("test:objectUri"));
        cheapAttribute.setType(SPARQL_SELECT_QUERY_CONTAINS);
        rule.addCheck(affordableAttribute);
        rule.addCheck(expensiveAttribute);
        rule.addCheck(cheapAttribute);
        List<Check> list = rule.getChecks();
        assertEquals(cheapAttribute.getUri(), list.get(0).getUri());
        assertEquals(affordableAttribute.getUri(), list.get(1).getUri());
        assertEquals(expensiveAttribute.getUri(), list.get(2).getUri());
    }

    private AttributeValueSet value(String value) {
        return new MutableAttributeValueSet(value);
    }

    private Check uriCheck(String uri, AttributeValueSet avc) {
        return new AccessObjectUriCheck(uri, avc);
    }
}
