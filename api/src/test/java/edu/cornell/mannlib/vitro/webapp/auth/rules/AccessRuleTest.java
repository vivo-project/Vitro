package edu.cornell.mannlib.vitro.webapp.auth.rules;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessObjectUriAttribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.Attribute;
import edu.cornell.mannlib.vitro.webapp.auth.attributes.TestType;

public class AccessRuleTest {

    @Test
    public void testAttributeOrderByComputationalCost() {
        AccessRule rule = new AccessRuleImpl();
        Attribute cheapAttribute = new AccessObjectUriAttribute("test:cheapAttributeUri", "test:objectUri");
        cheapAttribute.setTestType(TestType.EQUALS);
        Attribute affordableAttribute = new AccessObjectUriAttribute("test:affordableAttributeUri", "test:objectUri");
        cheapAttribute.setTestType(TestType.ONE_OF);
        Attribute expensiveAttribute = new AccessObjectUriAttribute("test:expensiveAttributeUri", "test:objectUri");
        cheapAttribute.setTestType(TestType.SPARQL_SELECT_QUERY_CONTAINS);
        rule.addAttribute(affordableAttribute);
        rule.addAttribute(expensiveAttribute);
        rule.addAttribute(cheapAttribute);
        List<Attribute> list = rule.getAttributes();
        assertEquals(cheapAttribute.getUri(), list.get(0).getUri());
        assertEquals(affordableAttribute.getUri(), list.get(1).getUri());
        assertEquals(expensiveAttribute.getUri(), list.get(2).getUri());
    }
}
