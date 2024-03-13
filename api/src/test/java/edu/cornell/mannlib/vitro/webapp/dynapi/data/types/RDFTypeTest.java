/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class RDFTypeTest {

    @Test
    public void testEquality() {
        RDFType type1 = new RDFType();
        RDFType type2 = new RDFType();
        assertEquals(type1, type2);
        type1.setName("name1");
        assertNotEquals(type1, type2);
        type2.setName("name2");
        assertNotEquals(type1, type2);
        type2.setName("name1");
        assertEquals(type1, type2);
    }
}
