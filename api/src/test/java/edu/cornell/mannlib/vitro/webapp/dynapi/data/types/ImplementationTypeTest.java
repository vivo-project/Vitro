package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ImplementationTypeTest {

    @Test
    public void testEquality() throws ClassNotFoundException {
        ImplementationType type1 = new ImplementationType();
        ImplementationType type2 = new ImplementationType();
        assertEquals(type1, type2);
        type1.setClassName(String.class.getCanonicalName());
        assertNotEquals(type1, type2);
        type2.setClassName(String.class.getCanonicalName());
        assertEquals(type1, type2);
        
        ImplementationConfig implementationConfig1 = new ImplementationConfig();
        type1.setDeserializationConfig(implementationConfig1);
        ImplementationConfig implementationConfig2 = new ImplementationConfig();
        assertNotEquals(type1, type2);
        type2.setDeserializationConfig(implementationConfig2);
        assertEquals(type1, type2);
        
        type1.setSerializationConfig(implementationConfig1);
        assertNotEquals(type1, type2);
        type2.setSerializationConfig(implementationConfig2);
        assertEquals(type1, type2);
    }
}
