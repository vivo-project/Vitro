/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationConfig;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ImplementationType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConversionMethod {

    private static final Log log = LogFactory.getLog(ConversionMethod.class.getName());
    private Constructor<?> constructor = null;
    private Method method = null;
    private ImplementationConfig config = null;
    private boolean isConstructor;
    private int methodArgsLength;
    private String[] arguments;
    private boolean isStatic;
    private Class<?> methodArgs[];
    private String methodName;
    private Class<?> classObject;

    public ConversionMethod(ParameterType type, boolean serialize) throws InitializationException {
        validateInput(type, serialize);
        Class<?> inputClass = null;
        ImplementationType implementation = type.getImplementationType();
        if (serialize) {
            config = implementation.getSerializationConfig();
            inputClass = type.getImplementationType().getClassName();
        } else {
            config = implementation.getDeserializationConfig();
            inputClass = String.class;
        }

        classObject = config.getClassObject();
        methodName = config.getMethodName();
        String rawArgs = config.getMethodArguments();
        isStatic = config.isStatic();

        arguments = getRawArgs(rawArgs);
        methodArgsLength = getMethodArgsSize(arguments);
        methodArgs = new Class[methodArgsLength];
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

    private void validateInput(ParameterType type, boolean serialize) throws InitializationException {
        if (type == null) {
            throw new InitializationException("Parameter type provided into constructor is null");
        }
        ImplementationType implType = type.getImplementationType();
        if (implType == null) {
            throw new InitializationException("Implemenation type in parameter type " +
                    "provided into constructor is null");
        }
        ImplementationConfig validatingConfig;
        if (serialize) {
            if (implType.getClassName() == null) {
                throw new InitializationException(
                        "Implemenation type class in parameter type provided into constructor is null");
            }
            validatingConfig = implType.getSerializationConfig();
            if (validatingConfig == null) {
                throw new InitializationException(
                        "Serialization config from implemenation type in parameter type " +
                        "provided into constructor is null");
            }
        } else {
            validatingConfig = implType.getDeserializationConfig();
            if (validatingConfig == null) {
                throw new InitializationException(
                        "Deserialization config from implemenation type in parameter type " +
                        "provided into constructor is null");
            }
        }
        if (validatingConfig.getClassObject() == null) {
            throw new InitializationException(
                    "Class object of implementation config from implemenation type in parameter " +
                    "type provided into constructor is null");
        }
        if (validatingConfig.getMethodName() == null) {
            throw new InitializationException(
                    "Method name of implementation config from implemenation type in parameter " +
                    "type provided into constructor is null");
        }
        if (validatingConfig.getMethodArguments() == null) {
            throw new InitializationException(
                    "Method arguments of implementation config from implemenation type in parameter " +
                    "type provided into constructor is null");
        }

    }

    public Object invoke(ParameterType type, Object input) throws ConversionException {
        Object instance = null;
        if (!isStatic) {
            instance = input;
            if (instance == null) {
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
        Object invokeArgs[] = new Object[methodArgsLength];
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ConversionMethod)) {
            return false;
        }
        if (object == this) {
            return true;
        }
        ConversionMethod compared = (ConversionMethod) object;

        return new EqualsBuilder()
                .append(constructor, compared.constructor)
                .append(method, compared.method)
                .append(isConstructor, compared.isConstructor)
                .append(methodArgsLength, compared.methodArgsLength)
                .append(arguments, compared.arguments)
                .append(isStatic, compared.isStatic)
                .append(methodArgs, compared.methodArgs)
                .append(methodName, compared.methodName)
                .append(classObject, compared.classObject)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(59, 103)
                .append(constructor)
                .append(method)
                .append(isConstructor)
                .append(methodArgsLength)
                .append(arguments)
                .append(isStatic)
                .append(methodArgs)
                .append(methodName)
                .append(classObject)
                .toHashCode();
    }
}
