/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionMethod;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import org.junit.Test;

public class ConversionConfigurationTest {
    @Test
    public void testEquality() throws ClassNotFoundException, InitializationException {

        ConversionConfiguration config1 = new ConversionConfiguration();
        ConversionConfiguration config2 = new ConversionConfiguration();
        assertEquals(config1, config2);
        config1.setClassName(String.class.getCanonicalName());
        assertNotEquals(config1, config2);
        config2.setClassName(String.class.getCanonicalName());
        assertEquals(config1, config2);

        config1.setMethodName("toString");
        assertNotEquals(config1, config2);
        config2.setMethodName("toString");
        assertEquals(config1, config2);

        config1.setMethodArguments("");
        assertNotEquals(config1, config2);
        config2.setMethodArguments("");
        assertEquals(config1, config2);

        config1.setStaticMethod(true);
        assertNotEquals(config1, config2);
        config2.setStaticMethod(true);
        assertEquals(config1, config2);

        ParameterType type1 = new ParameterType();
        ImplementationType implType1 = new ImplementationType();
        type1.addInterface(String.class.getCanonicalName());

        type1.setImplementationType(implType1);
        implType1.setSerializationConfig(config1);
        type1.setImplementationType(implType1);
        ConversionMethod cm1 = new ConversionMethod(config1);

        config1.setConversionMethod(cm1);
        assertNotEquals(config1, config2);

        ParameterType type2 = new ParameterType();
        ImplementationType implType2 = new ImplementationType();
        type2.addInterface(String.class.getCanonicalName());

        implType2.setSerializationConfig(config2);
        type2.setImplementationType(implType2);
        ConversionMethod cm2 = new ConversionMethod(config2);

        config2.setConversionMethod(cm2);
        assertEquals(config1, config2);
        config1.setMethodArguments("input");
        assertNotEquals(config1, config2);
        config2.setMethodArguments("input");
        assertEquals(config1, config2);
    }
}
