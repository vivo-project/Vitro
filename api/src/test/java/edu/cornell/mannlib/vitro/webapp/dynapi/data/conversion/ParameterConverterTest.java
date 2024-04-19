/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.ArrayListFactory;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ConversionConfiguration;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DataFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DefaultFormat;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.junit.Test;

public class ParameterConverterTest {

    // Instance method
    @Test
    public void stringDeserialization() throws Exception {
        Class<?> clazz = String.class;
        ParameterType type = createType(clazz, "toString", "", false, false);
        final String input = "serialized string";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals(clazz, result.getClass());
        assertEquals(input, result);
    }

    @Test
    public void stringSerialization() throws Exception {
        Class<?> clazz = String.class;
        ParameterType type = createType(clazz, "toString", "", false, true);
        final String input = "deserialized string";
        Object result = ParameterConverter.serialize(type, input);
        assertEquals(String.class, result.getClass());
        assertEquals(input, result);
    }

    /*
     * @Test public void stringLiteralDeserialization() throws Exception { final String className = "java.lang.String";
     * ParameterType type = createType(className, "toString", "", false, false); final String input =
     * "serialized string"; Object result = ParameterConverter.deserialize(type, input); assertEquals(className,
     * result.getClass()); assertEquals(input, result); }
     * 
     * @Test public void stringLiteralSerialization() throws Exception { final String className = "java.lang.String";
     * ParameterType type = createType(className, "toString", "", false, true); final String input =
     * "deserialized string"; Object result = ParameterConverter.serialize(type, input);
     * assertEquals("java.lang.String", result.getClass()); assertEquals(input, result); }
     */
    // Static method
    @Test
    public void integerDerialization() throws Exception {
        Class<?> clazz = Integer.class;
        ParameterType type = createType(clazz, "parseInt", "input", true, false);
        final String input = "42";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals(clazz, result.getClass());
        assertEquals(input, result.toString());
    }

    @Test
    public void IntegerSerialization() throws Exception {
        Class<?> clazz = Integer.class;
        ParameterType type = createType(clazz, "toString", "", false, true);
        final Integer input = 42;
        Object result = ParameterConverter.serialize(type, input);
        assertEquals(String.class, result.getClass());
        assertEquals(input.toString(), result);
    }

    // Constructor
    @Test
    public void bigIntegerDeserialization() throws Exception {
        Class<?> clazz = BigInteger.class;
        ParameterType type = createType(clazz, "", "input", false, false);
        final String input = "42";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals(clazz, result.getClass());
        assertEquals(input, result.toString());
    }

    @Test
    public void bigIntegerSerialization() throws Exception {
        ParameterType type = createType(BigInteger.class, "toString", "", false, true);
        final BigInteger input = new BigInteger("42");
        Object result = ParameterConverter.serialize(type, input);
        assertEquals(String.class, result.getClass());
        assertEquals(input.toString(), result);
    }

    @Test
    public void arrayOfStringDeserialization() throws Exception {
        ParameterType type = createArrayType("deserialize", "input type", true, String.class, "toString", "",
                false, false);
        final String input = "[\"42\", \"42\"]";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals(ArrayList.class, result.getClass());
        assertEquals(input, result.toString());
    }

    @Test
    public void arrayOfStringSerialization() throws Exception {
        ParameterType type = createArrayType("serialize", "input", true, String.class, "toString", "", false,
                true);
        ArrayList<?> input = new ArrayList<Object>(Arrays.asList("42", "42"));
        Object result = ParameterConverter.serialize(type, input);
        assertEquals(String.class, result.getClass());
        assertEquals(input.toString(), result);
    }

    @Test
    public void arrayOfIntegerDeserialization() throws Exception {
        ParameterType type = createArrayType("deserialize", "input type", true, Integer.class, "parseInt",
                "input", true, false);
        final String input = "[42, 42]";
        Object result = ParameterConverter.deserialize(type, input);
        assertEquals(ArrayList.class, result.getClass());
        assertEquals(input, result.toString());
    }

    @Test
    public void arrayOfIntegerSerialization() throws Exception {
        ParameterType type = createArrayType("serialize", "input", true, Integer.class, "toString", "", false,
                true);
        ArrayList<?> input = new ArrayList<Object>(Arrays.asList(42, 42));
        Object result = ParameterConverter.serialize(type, input);
        assertEquals(String.class, result.getClass());
        assertEquals(input.toString(), result);
    }

    private ParameterType createArrayType(String arrayMethod, String arrayArgs, boolean arrayIsStatic,
            Class<?> clazz, String elemMethodName, String elemArgs, boolean elemIsStatic,
            boolean serialization) throws Exception {
        ParameterType arrayType = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();
        arrayType.addFormat(defaultFormat);
        ConversionConfiguration arrayConfig = new ConversionConfiguration();
        if (serialization) {
            defaultFormat.setSerializationConfig(arrayConfig);
            arrayConfig.setInputInterface(ArrayList.class);
        } else {
            defaultFormat.setDeserializationConfig(arrayConfig);
        }
        arrayType.addInterface(ArrayList.class);
        arrayConfig.setClass(ArrayListFactory.class);
        arrayConfig.setMethodName(arrayMethod);
        arrayConfig.setMethodArguments(arrayArgs);
        arrayConfig.setStaticMethod(arrayIsStatic);

        ParameterType elementType = createType(clazz, elemMethodName, elemArgs, elemIsStatic, serialization);
        arrayType.setValuesType(elementType);
        return arrayType;
    }

    private ParameterType createType(Class<?> className, String methodName, String args, boolean isStatic,
            boolean serialization) throws Exception {
        ParameterType type = new ParameterType();
        DataFormat defaultFormat = new DefaultFormat();
        type.addFormat(defaultFormat);
        ConversionConfiguration config = new ConversionConfiguration();
        if (serialization) {
            defaultFormat.setSerializationConfig(config);
        } else {
            defaultFormat.setDeserializationConfig(config);
        }
        type.addInterface(className);
        config.setClass(className);
        config.setMethodName(methodName);
        config.setMethodArguments(args);
        config.setStaticMethod(isStatic);
        return type;
    }

}
