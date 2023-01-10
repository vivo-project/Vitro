package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class ParametersTest {

    @Test
    public void testParametersTestEquality() {
        Parameters params1 = new Parameters();
        Parameters params2 = new Parameters();
        assertEquals(params1, params2);
        Parameter param1 = new Parameter();
        params1.add(param1);
        assertNotEquals(params1, params2);
        Parameter param2 = new Parameter();
        params2.add(param2);
        assertEquals(params1, params2);
        param1.setName("name");
        assertNotEquals(params1, params2);
        param2.setName("name");
        assertEquals(params1, params2);
    }
}
