package edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ArraySerializationTypeTest {

    @Test
    public void testEquality() {
        ArraySerializationType type1 = new ArraySerializationType();
        ArraySerializationType type2 = new ArraySerializationType();
        PrimitiveSerializationType ptype1 = new PrimitiveSerializationType();
        ptype1.setName("name");
        PrimitiveSerializationType ptype2 = new PrimitiveSerializationType();
        assertEquals(type1, type2);
        type1.setName("name1");
        assertNotEquals(type1, type2);
        type2.setName("name2");
        assertNotEquals(type1, type2);
        type2.setName("name1");
        assertEquals(type1, type2);
        type1.setElementsType(ptype1);
        assertNotEquals(type1, type2);
        type2.setElementsType(ptype2);
        assertNotEquals(type1, type2);
        ptype2.setName("name");
        assertEquals(type1, type2);
    }
}
