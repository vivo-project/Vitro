/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.serialization.PrimitiveSerializationType;
import org.junit.Test;

public class ParameterTypesTest {

    @Test
    public void testEquality() {
        ParameterType type1 = new ParameterType();
        ParameterType type2 = new ParameterType();
        assertEquals(type1, type2);

        type1.setName("name1");
        assertNotEquals(type1, type2);
        type2.setName("name2");
        assertNotEquals(type1, type2);
        type2.setName("name1");
        assertEquals(type1, type2);

        type1.setIsInternal(true);
        assertEquals(type1, type2);

        PrimitiveSerializationType serializationType1 = new PrimitiveSerializationType();
        PrimitiveSerializationType serializationType2 = new PrimitiveSerializationType();
        type1.setSerializationType(serializationType1);
        assertNotEquals(type1, type2);
        type2.setSerializationType(serializationType2);
        assertEquals(type1, type2);

        ImplementationType implementationType1 = new ImplementationType();
        ImplementationType implementationType2 = new ImplementationType();
        type1.setImplementationType(implementationType1);
        assertNotEquals(type1, type2);
        type2.setImplementationType(implementationType2);
        assertEquals(type1, type2);

        RDFType rdfType1 = new RDFType();
        RDFType rdfType2 = new RDFType();
        type1.setRdfType(rdfType1);
        assertNotEquals(type1, type2);
        type2.setRdfType(rdfType2);
        assertEquals(type1, type2);

        ParameterType type3 = new ParameterType();
        ParameterType type4 = new ParameterType();
        type1.setValuesType(type3);
        assertNotEquals(type1, type2);
        type2.setValuesType(type4);
        assertEquals(type1, type2);
    }
}
