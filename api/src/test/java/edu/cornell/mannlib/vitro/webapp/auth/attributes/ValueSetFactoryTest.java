package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

public class ValueSetFactoryTest {

    @Test
    public void testCreate() {
        QuerySolutionMap qs = new QuerySolutionMap();
        qs.add("setElementsType", ResourceFactory.createPlainLiteral("some type"));
        AttributeValueKey key = new AttributeValueKey();
        String oldValue = "value";
        AttributeValueSet valueSet1 = ValueSetFactory.create(oldValue, qs, key);
        assertTrue(valueSet1.contains(oldValue));
        String newValue = "new value";
        AttributeValueSet valueSet2 = ValueSetFactory.create(newValue, qs, key);
        assertNotEquals(valueSet1, valueSet2);
    }
}
