package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ConversionMethod {
	
	private static final Log log = LogFactory.getLog(ConversionMethod.class.getName());
	private Constructor<?> constructor = null;
	private Method method = null;
	private ImplementationConfig config = null;
	private boolean isConstructor;
	private int methodArgslength;
	private String[] arguments;
	private boolean isStatic;
	private Class<?> methodArgs[];
	private String methodName;
	private Class<?> classObject;


	public ConversionMethod(ParameterType type, boolean serialize) throws InitializationException {
		Class<?> inputClass = null;
		ImplementationType implementation = type.getImplementationType();
		if (serialize) {
			config = implementation.getSerializationConfig();
			inputClass = type.getImplementationType().getClassName();
		} else {
			config = implementation.getDeserializationConfig();
			inputClass = "".getClass();
		}

		classObject = config.getClassObject();
		methodName = config.getMethodName();
		String rawArgs = config.getMethodArguments();
		isStatic = config.isStatic();

		arguments = getRawArgs(rawArgs);
		methodArgslength = getMethodArgsSize(arguments);
		methodArgs = new Class[methodArgslength];
		for (int i = 0; i < methodArgs.length; i++) {
			String className = getClassName(arguments[i], inputClass, type);
			try {
				methodArgs[i] = Class.forName(className);
			} catch (ClassNotFoundException e) {
				log.error(e, e);
				throw new InitializationException(e.getLocalizedMessage());
			}
		}
		isConstructor = isConstructorInvocation(methodName);
		try {
			if (isConstructor) {
				constructor = classObject.getConstructor(methodArgs);
			} else {
				method = classObject.getDeclaredMethod(methodName, methodArgs);
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new InitializationException(e.getLocalizedMessage());
		}
	}

	public Object invoke(ParameterType type, Object input) throws ConversionException {
		Object instance = null;
		if (!isStatic) {
			instance = input;
			 if (instance == null ) {
                 throw new ConversionException("input instance is null");
             }
		}
		Object[] invokeArgs = prepareInvokeArgs(type, input);
		try {
			if (isConstructor) {
				return constructor.newInstance(invokeArgs);
			} else {
				return method.invoke(instance, invokeArgs);
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new ConversionException(e.getLocalizedMessage());
		}
	}

	private Object[] prepareInvokeArgs(ParameterType type, Object input) throws ConversionException {
		Object invokeArgs[] = new Object[methodArgslength];
		for (int i = 0; i < invokeArgs.length; i++) {
			invokeArgs[i] = getArgument(arguments[i], input, type);
		}
		return invokeArgs;
	}

	private static String[] getRawArgs(String rawArgs) {
		return rawArgs.trim().split("\\s+");
	}

	private static boolean isConstructorInvocation(String methodName) {
		return StringUtils.isBlank(methodName);
	}

	private static int getMethodArgsSize(String[] arguments) {
		int length = arguments.length;
		if (length == 1 && StringUtils.isBlank(arguments[0])) {
			return 0;
		}
		return length;
	}

	private static String getClassName(String var, Class<?> inputClass, ParameterType type)
			throws InitializationException {
		if ("input".equals(var)) {
			return inputClass.getCanonicalName();
		}
		if ("type".equals(var)) {
			return type.getClass().getCanonicalName();
		}
		throw new InitializationException("Variable name " + var + " is not known");
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
