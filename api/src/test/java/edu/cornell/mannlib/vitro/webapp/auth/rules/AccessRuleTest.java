package edu.cornell.mannlib.vitro.webapp.auth.rules;

import static org.junit.Assert.assertEquals;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectUriCheck;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.Check;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.CheckType;
import org.junit.Test;

public class AccessRuleTest {

    @Test
    public void testAttributeOrderByComputationalCost() {
        AccessRule rule = new AccessRuleImpl();
        Check cheapAttribute = new AccessObjectUriCheck("test:cheapAttributeUri", "test:objectUri");
        cheapAttribute.setType(CheckType.EQUALS);
        Check affordableAttribute = new AccessObjectUriCheck("test:affordableAttributeUri", "test:objectUri");
        cheapAttribute.setType(CheckType.ONE_OF);
        Check expensiveAttribute = new AccessObjectUriCheck("test:expensiveAttributeUri", "test:objectUri");
        cheapAttribute.setType(CheckType.SPARQL_SELECT_QUERY_CONTAINS);
        rule.addCheck(affordableAttribute);
        rule.addCheck(expensiveAttribute);
        rule.addCheck(cheapAttribute);
        List<Check> list = rule.getChecks();
        assertEquals(cheapAttribute.getUri(), list.get(0).getUri());
        assertEquals(affordableAttribute.getUri(), list.get(1).getUri());
        assertEquals(expensiveAttribute.getUri(), list.get(2).getUri());
    }
}
