/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ConversionMethodTest {

    @org.junit.runners.Parameterized.Parameter(0)
    public String methodName1;
    @org.junit.runners.Parameterized.Parameter(1)
    public String methodName2;
    @org.junit.runners.Parameterized.Parameter(2)
    public Class<?> class1;
    @org.junit.runners.Parameterized.Parameter(3)
    public Class<?> class2;
    @org.junit.runners.Parameterized.Parameter(4)
    public String methodArgs1;
    @org.junit.runners.Parameterized.Parameter(5)
    public String methodArgs2;
    @org.junit.runners.Parameterized.Parameter(6)
    public boolean result;

    @Test
    public void testEquality() throws InitializationException, ClassNotFoundException {
        ConversionMethod cm1 = new ConversionMethod(getType1());
        ConversionMethod cm2 = new ConversionMethod(getType2());
        assertEquals(result, cm1.equals(cm2));
    }

    private ConversionConfiguration getType1() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClass(class1);
        config.setMethodName(methodName1);
        config.setMethodArguments(methodArgs1);
        config.setInputInterface(class1);
        return config;
    }

    private ConversionConfiguration getType2() throws ClassNotFoundException {
        ConversionConfiguration config = new ConversionConfiguration();
        config.setClass(class2);
        config.setMethodName(methodName2);
        config.setMethodArguments(methodArgs2);
        config.setInputInterface(class2);
        return config;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { "toString", "toString", String.class, String.class, "", "", true },
                { "toLowerCase", "toString", String.class, String.class, "", "", false },
                { "toString", "toString", String.class, Integer.class, "", "", false },
                { "", "", String.class, String.class, "input", "", false },

        });
    }
}
