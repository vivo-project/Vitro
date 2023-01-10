package edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;


public class JsonContainerSerializationTest {
    @Test
    public void testEquality() {
        JsonContainerSerializationType type1 = new JsonContainerSerializationType();
        JsonContainerSerializationType type2 = new JsonContainerSerializationType();
        Parameter param1 = new Parameter();
        Parameter param2 = new Parameter();
        assertEquals(type1, type2);
        type1.setName("name1");
        assertNotEquals(type1, type2);
        type2.setName("name2");
        assertNotEquals(type1, type2);
        type2.setName("name1");
        assertEquals(type1, type2);
        type1.addInternalElement(param1);
        assertNotEquals(type1, type2);
        type2.addInternalElement(param2);
        assertEquals(type1, type2);
    }
}
