/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ArrayListFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.junit.Test;

public class ParameterConverterTest {

    // Instance method
    @Test
    public void stringDeserialization() throws Exception {
        final String className = "java.lang.String";
        ParameterType type = createType(className, "toString", "", false, false);
        final String input = "serialized string";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals(className, result.getClass().getCanonicalName());
        assertEquals(input, result);
    }

    @Test
    public void stringSerialization() throws Exception {
        final String className = "java.lang.String";
        ParameterType type = createType(className, "toString", "", false, true);
        final String input = "deserialized string";
        Object result = ParameterConverter.serialize(type, input);
        assertEquals("java.lang.String", result.getClass().getCanonicalName());
        assertEquals(input, result);
    }

    /*
     * @Test public void stringLiteralDeserialization() throws Exception { final String className = "java.lang.String";
     * ParameterType type = createType(className, "toString", "", false, false); final String input =
     * "serialized string"; Object result = ParameterConverter.deserialize(type, input); assertEquals(className,
     * result.getClass().getCanonicalName()); assertEquals(input, result); }
     * 
     * @Test public void stringLiteralSerialization() throws Exception { final String className = "java.lang.String";
     * ParameterType type = createType(className, "toString", "", false, true); final String input =
     * "deserialized string"; Object result = ParameterConverter.serialize(type, input);
     * assertEquals("java.lang.String", result.getClass().getCanonicalName()); assertEquals(input, result); }
     */
    // Static method
    @Test
    public void integerDerialization() throws Exception {
        final String className = "java.lang.Integer";
        ParameterType type = createType(className, "parseInt", "input", true, false);
        final String input = "42";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals(className, result.getClass().getCanonicalName());
        assertEquals(input, result.toString());
    }

    @Test
    public void IntegerSerialization() throws Exception {
        final String className = "java.lang.Integer";
        ParameterType type = createType(className, "toString", "", false, true);
        final Integer input = 42;
        Object result = ParameterConverter.serialize(type, input);
        assertEquals("java.lang.String", result.getClass().getCanonicalName());
        assertEquals(input.toString(), result);
    }

    // Constructor
    @Test
    public void bigIntegerDeserialization() throws Exception {
        final String className = "java.math.BigInteger";
        ParameterType type = createType(className, "", "input", false, false);
        final String input = "42";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals(className, result.getClass().getCanonicalName());
        assertEquals(input, result.toString());
    }

    @Test
    public void bigIntegerSerialization() throws Exception {
        final String className = "java.math.BigInteger";
        ParameterType type = createType(className, "toString", "", false, true);
        final BigInteger input = new BigInteger("42");
        Object result = ParameterConverter.serialize(type, input);
        assertEquals("java.lang.String", result.getClass().getCanonicalName());
        assertEquals(input.toString(), result);
    }

    @Test
    public void arrayOfStringDeserialization() throws Exception {
        ParameterType type = createArrayType("deserialize", "input type", true, "java.lang.String", "toString", "",
                false, false);
        final String input = "[\"42\", \"42\"]";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals("java.util.ArrayList", result.getClass().getCanonicalName());
        assertEquals(input, result.toString());
    }

    @Test
    public void arrayOfStringSerialization() throws Exception {
        ParameterType type = createArrayType("serialize", "input", true, "java.lang.String", "toString", "", false,
                true);
        ArrayList input = new ArrayList(Arrays.asList("42", "42"));
        Object result = ParameterConverter.serialize(type, input);
        assertEquals("java.lang.String", result.getClass().getCanonicalName());
        assertEquals(input.toString(), result);
    }

    @Test
    public void arrayOfIntegerDeserialization() throws Exception {
        ParameterType type = createArrayType("deserialize", "input type", true, "java.lang.Integer", "parseInt",
                "input", true, false);
        final String input = "[42, 42]";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals("java.util.ArrayList", result.getClass().getCanonicalName());
        assertEquals(input, result.toString());
    }

    @Test
    public void arrayOfIntegerSerialization() throws Exception {
        ParameterType type = createArrayType("serialize", "input", true, "java.lang.Integer", "toString", "", false,
                true);
        ArrayList input = new ArrayList(Arrays.asList(42, 42));
        Object result = ParameterConverter.serialize(type, input);
        assertEquals("java.lang.String", result.getClass().getCanonicalName());
        assertEquals(input.toString(), result);
    }

    private ParameterType createArrayType(String arrayMethod, String arrayArgs, boolean arrayIsStatic,
            String elementClassName, String elemMethodName, String elemArgs, boolean elemIsStatic,
            boolean serialization) throws Exception {
        final String arrayClassName = ArrayListFactory.class.getCanonicalName();
        ArrayParameterType arrayType = new ArrayParameterType();
        ImplementationType arrayImplType = new ImplementationType();
        arrayType.setImplementationType(arrayImplType);
        ImplementationConfig arrayConfig = new ImplementationConfig();
        if (serialization) {
            arrayImplType.setSerializationConfig(arrayConfig);
        } else {
            arrayImplType.setDeserializationConfig(arrayConfig);
        }
        arrayImplType.setClassName(ArrayList.class.getCanonicalName());
        arrayConfig.setClassName(arrayClassName);
        arrayConfig.setMethodName(arrayMethod);
        arrayConfig.setMethodArguments(arrayArgs);
        arrayConfig.setStaticMethod(arrayIsStatic);

        ParameterType elementType = createType(elementClassName, elemMethodName, elemArgs, elemIsStatic, serialization);
        arrayType.setValuesType(elementType);
        // arrayType.initialize();
        return arrayType;
    }

    private ParameterType createType(String className, String methodName, String args, boolean isStatic,
            boolean serialization) throws Exception {
        ParameterType type = new ParameterType();
        ImplementationType implType = new ImplementationType();
        type.setImplementationType(implType);
        ImplementationConfig config = new ImplementationConfig();
        if (serialization) {
            implType.setSerializationConfig(config);
        } else {
            implType.setDeserializationConfig(config);
        }
        implType.setClassName(className);
        config.setClassName(className);
        config.setMethodName(methodName);
        config.setMethodArguments(args);
        config.setStaticMethod(isStatic);
        // type.initialize();
        return type;
    }

}
