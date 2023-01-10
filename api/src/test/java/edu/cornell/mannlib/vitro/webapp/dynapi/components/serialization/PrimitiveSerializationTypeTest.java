package edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;


public class PrimitiveSerializationTypeTest {
    @Test
    public void testEquality() {
        PrimitiveSerializationType type1 = new PrimitiveSerializationType();
        PrimitiveSerializationType type2 = new PrimitiveSerializationType();
        assertEquals(type1, type2);
        type1.setName("name1");
        assertNotEquals(type1, type2);
        type2.setName("name2");
        assertNotEquals(type1, type2);
        type2.setName("name1");
        assertEquals(type1, type2);
    }
}
