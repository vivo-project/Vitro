package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class ParameterConverterTest {
	
	//Instance method
	@Test
	public void stringDeserialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.lang.String";
		Parameter param = createParameter(className, "toString", "", false);
		final String input = "serialized string";
		Object result = ParameterConverter.deserialize(param, input);
		assertEquals(className, result.getClass().getCanonicalName());
		assertEquals(input, result);
	}
	
	@Test
	public void stringSerialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.lang.String";
		Parameter param = createParameter(className, "toString", "", false);
		final String input = "deserialized string";
		Object result = ParameterConverter.serialize(param, input);
		assertEquals("java.lang.String", result.getClass().getCanonicalName());
		assertEquals(input, result);
	}
	
	//Static method
	@Test
	public void integerDerialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.lang.Integer";
		Parameter param = createParameter(className, "parseInt", "input", true);
		final String input = "42";
		Object result = ParameterConverter.deserialize(param, input);
		assertEquals(className, result.getClass().getCanonicalName());
		assertEquals(input, result.toString());
	}
	
	@Test
	public void IntegerSerialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.lang.Integer";
		Parameter param = createParameter(className, "toString", "", false);
		final Integer input = 42;
		Object result = ParameterConverter.serialize(param, input);
		assertEquals("java.lang.String", result.getClass().getCanonicalName());
		assertEquals(input.toString(), result);
	}
	
	//Constructor
	@Test
	public void bigIntegerDeserialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.math.BigInteger";
		Parameter param = createParameter(className, "", "input", false);
		final String input = "42";
		Object result = ParameterConverter.deserialize(param, input);
		assertEquals(className, result.getClass().getCanonicalName());
		assertEquals(input, result.toString());
	}
	
	@Test
	public void bigIntegerSerialization() throws ClassNotFoundException, ConversionException {
		final String className = "java.math.BigInteger";
		Parameter param = createParameter(className, "toString", "", false);
		final BigInteger input = new BigInteger("42");
		Object result = ParameterConverter.serialize(param, input);
		assertEquals("java.lang.String", result.getClass().getCanonicalName());
		assertEquals(input.toString(), result);
	}
	
	private Parameter createParameter(String className, String methodName, String args, boolean isStatic) throws ClassNotFoundException {
		Parameter param = new Parameter();
		ParameterType type = new ParameterType();
		param.setType(type);
		ImplementationType impType = new ImplementationType();
		type.setImplementationType(impType);
		ImplementationConfig config = new ImplementationConfig();
		impType.setDeserializationConfig(config);
		config.setClassName(className);
		config.setMethodName(methodName);
		config.setMethodArguments(args);
		config.setStaticMethod(isStatic);
		return param;
	}

}
