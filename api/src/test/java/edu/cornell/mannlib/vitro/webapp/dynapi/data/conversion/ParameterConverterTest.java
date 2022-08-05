package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class ParameterConverterTest {

	// Instance method
	@Test
	public void stringDeserialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.lang.String";
		ParameterType type = createType(className, "toString", "", false);
		final String input = "serialized string";
		Object result = ParameterConverter.deserialize(type, input);
		assertEquals(className, result.getClass().getCanonicalName());
		assertEquals(input, result);
	}

	@Test
	public void stringSerialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.lang.String";
		ParameterType type = createType(className, "toString", "", false);
		final String input = "deserialized string";
		Object result = ParameterConverter.serialize(type, input);
		assertEquals("java.lang.String", result.getClass().getCanonicalName());
		assertEquals(input, result);
	}

	// Static method
	@Test
	public void integerDerialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.lang.Integer";
		ParameterType type = createType(className, "parseInt", "input", true);
		final String input = "42";
		Object result = ParameterConverter.deserialize(type, input);
		assertEquals(className, result.getClass().getCanonicalName());
		assertEquals(input, result.toString());
	}

	@Test
	public void IntegerSerialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.lang.Integer";
		ParameterType type = createType(className, "toString", "", false);
		final Integer input = 42;
		Object result = ParameterConverter.serialize(type, input);
		assertEquals("java.lang.String", result.getClass().getCanonicalName());
		assertEquals(input.toString(), result);
	}

	// Constructor
	@Test
	public void bigIntegerDeserialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.math.BigInteger";
		ParameterType type = createType(className, "", "input", false);
		final String input = "42";
		Object result = ParameterConverter.deserialize(type, input);
		assertEquals(className, result.getClass().getCanonicalName());
		assertEquals(input, result.toString());
	}

	@Test
	public void bigIntegerSerialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.math.BigInteger";
		ParameterType type = createType(className, "toString", "", false);
		final BigInteger input = new BigInteger("42");
		Object result = ParameterConverter.serialize(type, input);
		assertEquals("java.lang.String", result.getClass().getCanonicalName());
		assertEquals(input.toString(), result);
	}

	@Test
	public void arrayOfStringDeserialization() throws ClassNotFoundException, ConversionException {
		final String className = "edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DynapiArrayList";
		ParameterType type = createArrayType("deserialize", "input type", true, "java.lang.String", "toString", "", false);
		final String input = "[\"42\", \"42\"]";
		Object result = ParameterConverter.deserialize(type, input);
		assertEquals("java.util.ArrayList", result.getClass().getCanonicalName());
		assertEquals(input, result.toString());
	}

	@Test
	public void arrayOfStringSerialization() throws ClassNotFoundException, ConversionException {
		final String className = "edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DynapiArrayList";
		ParameterType type = createArrayType("serialize", "input", true, "java.lang.String", "toString", "", false);
		ArrayList input = new ArrayList(Arrays.asList("42", "42"));
		Object result = ParameterConverter.serialize(type, input);
		assertEquals("java.lang.String", result.getClass().getCanonicalName());
		assertEquals(input.toString(), result);
	}
	
	@Test
	public void arrayOfIntegerDeserialization() throws ClassNotFoundException, ConversionException {
		final String className = "edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DynapiArrayList";
		ParameterType type = createArrayType("deserialize", "input type", true, "java.lang.Integer", "parseInt", "input", true);
		final String input = "[42, 42]";
		Object result = ParameterConverter.deserialize(type, input);
		assertEquals("java.util.ArrayList", result.getClass().getCanonicalName());
		assertEquals(input, result.toString());
	}

	@Test
	public void arrayOfIntegerSerialization() throws ClassNotFoundException, ConversionException {
		final String className = "edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DynapiArrayList";
		ParameterType type = createArrayType("serialize", "input", true, "java.lang.Integer", "toString", "", false);
		ArrayList input = new ArrayList(Arrays.asList(42, 42));
		Object result = ParameterConverter.serialize(type, input);
		assertEquals("java.lang.String", result.getClass().getCanonicalName());
		assertEquals(input.toString(), result);
	}

	private ParameterType createArrayType(String arrayMethod, String arrayArgs, boolean arrayIsStatic,
	String elementClassName, String elemMethodName, String elemArgs, boolean elemIsStatic) throws ClassNotFoundException {
		final String arrayClassName = "edu.cornell.mannlib.vitro.webapp.dynapi.data.types.DynapiArrayList";
		ArrayParameterType arrayType = new ArrayParameterType();
		ImplementationType arrayImplType = new ImplementationType();
		arrayType.setImplementationType(arrayImplType);
		ImplementationConfig arrayConfig = new ImplementationConfig();
		arrayImplType.setDeserializationConfig(arrayConfig);
		arrayConfig.setClassName(arrayClassName);
		arrayConfig.setMethodName(arrayMethod);
		arrayConfig.setMethodArguments(arrayArgs);
		arrayConfig.setStaticMethod(arrayIsStatic);

		ParameterType elementType = createType(elementClassName, elemMethodName, elemArgs, elemIsStatic);
		arrayType.setValuesType(elementType);
		return arrayType;
	}

	private ParameterType createType(String className, String methodName, String args, boolean isStatic)
			throws ClassNotFoundException {
		ParameterType type = new ParameterType();
		ImplementationType impType = new ImplementationType();
		type.setImplementationType(impType);
		ImplementationConfig config = new ImplementationConfig();
		impType.setDeserializationConfig(config);
		config.setClassName(className);
		config.setMethodName(methodName);
		config.setMethodArguments(args);
		config.setStaticMethod(isStatic);
		return type;
	}

}
