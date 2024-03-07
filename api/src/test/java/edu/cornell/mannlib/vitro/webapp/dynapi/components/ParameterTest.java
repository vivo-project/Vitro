package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.junit.Test;

public class ParameterTest {

    @Test
    public void testEquality() throws InitializationException {
        Parameter param1 = new Parameter();
        Parameter param2 = new Parameter();
        assertEquals(param1, param2);
        param1.setName("name1");
        assertNotEquals(param1, param2);
        param2.setName("name1");
        assertEquals(param1, param2);
        // Description is not important for logical equality
        param1.setDescription("description");
        assertEquals(param1, param2);
        // Default value is not important for logical equality
        param1.setDefaultValue("1");
        assertEquals(param1, param2);
        // internal attributeis not important for logical equality
        param1.setInternal(true);
        assertEquals(param1, param2);

        ParameterType parameterType1 = new ParameterType();
        ParameterType parameterType2 = new ParameterType();
        ImplementationType implementationType1 = new ImplementationType();
        parameterType1.setImplementationType(implementationType1);
        ImplementationType implementationType2 = new ImplementationType();
        parameterType2.setImplementationType(implementationType2);

        param1.setType(parameterType1);
        assertNotEquals(param1, param2);

        param2.setType(parameterType2);
        assertEquals(param1, param2);
    }
}
