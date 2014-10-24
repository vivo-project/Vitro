/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyMethod;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyTypeException;

/**
 * Parse the annotations on this class and package them with a newly-created
 * instance of the class.
 */
public class InstanceWrapper {
	public static <T> WrappedInstance<T> wrap(Class<? extends T> concreteClass)
			throws InstanceWrapperException {
		return new WrappedInstance<T>(createInstance(concreteClass),
				parsePropertyAnnotations(concreteClass),
				parseValidationAnnotations(concreteClass));
	}

	private static <T> T createInstance(Class<? extends T> concreteClass)
			throws InstanceWrapperException {
		try {
			return concreteClass.newInstance();
		} catch (Exception e) {
			throw new InstanceWrapperException("Failed to create an instance.",
					e);
		}
	}

	private static Map<String, PropertyMethod> parsePropertyAnnotations(
			Class<?> concreteClass) throws InstanceWrapperException {
		Map<String, PropertyMethod> map = new HashMap<>();
		for (Method method : concreteClass.getDeclaredMethods()) {
			Property annotation = method.getAnnotation(Property.class);
			if (annotation == null) {
				continue;
			}
			if (!method.getReturnType().equals(Void.TYPE)) {
				throw new InstanceWrapperException("Property method '" + method
						+ "' should return void.");
			}
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes.length != 1) {
				throw new InstanceWrapperException("Property method '" + method
						+ "' must accept exactly one parameter.");
			}

			String uri = annotation.uri();
			if (map.containsKey(uri)) {
				throw new InstanceWrapperException(
						"Two property methods have the same URI value: "
								+ map.get(uri).getMethod() + ", and " + method);
			}
			try {
				map.put(uri, PropertyType.createPropertyMethod(method));
			} catch (PropertyTypeException e) {
				throw new InstanceWrapperException(
						"Failed to create the PropertyMethod", e);
			}
		}
		return map;
	}

	private static Set<Method> parseValidationAnnotations(Class<?> concreteClass)
			throws InstanceWrapperException {
		Set<Method> methods = new HashSet<>();
		for (Method method : concreteClass.getDeclaredMethods()) {
			if (method.getAnnotation(Validation.class) == null) {
				continue;
			}
			if (method.getParameterTypes().length > 0) {
				throw new InstanceWrapperException("Validation method '"
						+ method + "' should not have parameters.");
			}
			if (!method.getReturnType().equals(Void.TYPE)) {
				throw new InstanceWrapperException("Validation method '"
						+ method + "' should return void.");
			}
			methods.add(method);
		}
		return methods;
	}

	public static class InstanceWrapperException extends Exception {
		public InstanceWrapperException(String message) {
			super(message);
		}

		public InstanceWrapperException(String message, Throwable cause) {
			super(message, cause);
		}

	}
}
