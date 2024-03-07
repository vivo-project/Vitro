package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
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
    public String className1;
    @org.junit.runners.Parameterized.Parameter(3)
    public String className2;
    @org.junit.runners.Parameterized.Parameter(4)
    public String methodArgs1;
    @org.junit.runners.Parameterized.Parameter(5)
    public String methodArgs2;
    @org.junit.runners.Parameterized.Parameter(6)
    public boolean result;

    @Test
    public void testEquality() throws InitializationException, ClassNotFoundException {
        ConversionMethod cm1 = new ConversionMethod(getType1(), true);
        ConversionMethod cm2 = new ConversionMethod(getType2(), true);
        assertEquals(result, cm1.equals(cm2));
    }

    private ParameterType getType1() throws ClassNotFoundException {
        ImplementationConfig config1 = new ImplementationConfig();
        config1.setClassName(className1);
        config1.setMethodName(methodName1);
        config1.setMethodArguments(methodArgs1);
        ParameterType type1 = new ParameterType();
        ImplementationType implType1 = new ImplementationType();
        implType1.setClassName(className1);
        type1.setImplementationType(implType1);
        implType1.setSerializationConfig(config1);
        type1.setImplementationType(implType1);
        return type1;
    }

    private ParameterType getType2() throws ClassNotFoundException {
        ImplementationConfig config1 = new ImplementationConfig();
        config1.setClassName(className2);
        config1.setMethodName(methodName2);
        config1.setMethodArguments(methodArgs2);
        ParameterType type1 = new ParameterType();
        ImplementationType implType1 = new ImplementationType();
        implType1.setClassName(className2);
        type1.setImplementationType(implType1);
        implType1.setSerializationConfig(config1);
        type1.setImplementationType(implType1);
        return type1;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> requests() {
        return Arrays.asList(new Object[][] {
                { "toString", "toString", "java.lang.String", "java.lang.String", "", "", true },
                { "toLowerCase", "toString", "java.lang.String", "java.lang.String", "", "", false },
                { "toString", "toString", "java.lang.String", "java.lang.Integer", "", "", false },
                { "", "", "java.lang.String", "java.lang.String", "input", "", false },

        });
    }
}
