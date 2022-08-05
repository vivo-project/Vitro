package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class ParameterConverter {

	private static final Log log = LogFactory.getLog(ParameterConverter.class.getName());
	
	public static String serialize(ParameterType type, Object input) throws ConversionException {
		return convert(type, input).toString();
	}
	public static Object deserialize(ParameterType type, Object input) throws ConversionException {
		return convert(type, input);
	}
	private static Object convert(ParameterType type, Object input) throws ConversionException {
		ImplementationType implementation = type.getImplementationType();
		ImplementationConfig config = implementation.getDeserializationConfig();
		Class<?> classObject = config.getClassObject();
		String methodName = config.getMethodName();
		String rawArgs = config.getMethodArguments();
		Object instance = null;
		if (!config.isStatic()) {
			instance = input;
		}
		String[] arguments = rawArgs.trim().split("\\s+");
		final int length = getMethodArgsSize(arguments);
		
		Class<?> methodArgs[] = new Class[length];
		Object invokeArgs[] = new Object[length];
		for (int i = 0; i < methodArgs.length; i++) {
			String className = getClassName(arguments[i], input, type);
			try {
				methodArgs[i] = Class.forName(className);
			} catch (ClassNotFoundException e) {
				log.error(e,e);
				throw new ConversionException(e.getLocalizedMessage());
			}
		}
		
		for (int i = 0; i < invokeArgs.length; i++) {
			invokeArgs[i]= getArgument(arguments[i], input, type);
		}
		
		config.getMethodArguments();
		if (StringUtils.isBlank(methodName)){
			return invokeConstructor(classObject, methodArgs, invokeArgs);
		} else {
			return invokeMethod(classObject, methodName, instance, methodArgs, invokeArgs);	
		}
	}

	private static Object invokeConstructor(Class<?> classObject, Class<?>[] methodArgs, Object[] invokeArgs)
			throws ConversionException {
		try {
			Constructor<?> constructor = classObject.getConstructor(methodArgs);
			Object result = constructor.newInstance(invokeArgs);
			return result;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error(e,e);
			throw new ConversionException(e.getLocalizedMessage());
		}
	}

	private static Object invokeMethod(Class<?> classObject, String methodName, Object instance, Class<?>[] methodArgs,
			Object[] invokeArgs) throws ConversionException {
		try {
			Method method = classObject.getDeclaredMethod(methodName, methodArgs);
			Object result = method.invoke(instance, invokeArgs);
			return result;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error(e,e);
			throw new ConversionException(e.getLocalizedMessage());
		}
	}
	
	private static int getMethodArgsSize(String[] arguments) {
		int length = arguments.length;
		if (length == 1 && StringUtils.isBlank(arguments[0])) {
			return 0;
		}
		return length;
	}
	
	private static String getClassName(String var, Object input, ParameterType type) throws ConversionException {
		if ("input".equals(var)) {
			return input.getClass().getCanonicalName();
		}
		if ("type".equals(var)) {
			return type.getClass().getCanonicalName();
		}
		throw new ConversionException("Variable name " + var + " is not known");
	}
	
	private static Object getArgument(String var, Object input, ParameterType type) throws ConversionException {
		if ("input".equals(var)) {
			return input;
		}
		if ("type".equals(var)) {
			return type;
		}
		throw new ConversionException("Variable name " + var + " is not known");
	}
}
