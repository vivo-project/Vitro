/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyMethod;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyTypeException;

/**
 * Parse the annotations on this class and package them with a newly-created
 * instance of the class.
 */
public class InstanceWrapper {
	private static final Log log = LogFactory.getLog(InstanceWrapper.class);

	public static <T> WrappedInstance<T> wrap(Class<? extends T> concreteClass)
			throws InstanceWrapperException {
		T instance = createInstance(concreteClass);
		HashSet<Method> validationMethods = new HashSet<>(
				parseValidationAnnotations(concreteClass).values());
		Map<String, PropertyMethod> propertyMethods = new PropertyAnnotationsMap(
				concreteClass).byUri();
		return new WrappedInstance<T>(instance, propertyMethods,
				validationMethods);
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

	private static Map<String, Method> parseValidationAnnotations(Class<?> clazz)
			throws InstanceWrapperException {
		if (Object.class.equals(clazz)) {
			return new HashMap<>();
		} else {
			Map<String, Method> methods = parseValidationAnnotations(clazz
					.getSuperclass());
			for (Method method : clazz.getDeclaredMethods()) {
				String name = method.getName();
				if (methods.containsKey(name)) {
					Method m = methods.get(name);
					throw new InstanceWrapperException("Method " + name
							+ " in " + method.getDeclaringClass().getName()
							+ " overrides a validation method in "
							+ m.getDeclaringClass().getName());
				}
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
				methods.put(name, method);
			}
			return methods;
		}
	}

	private static class PropertyAnnotationsMap {
		private Map<String, PropertyMethod> mapByUri = new HashMap<>();
		private Map<String, PropertyMethod> mapByName = new HashMap<>();

		public PropertyAnnotationsMap(Class<?> clazz)
				throws InstanceWrapperException {
			if (!Object.class.equals(clazz)) {
				populateTheMaps(clazz);
			}
		}

		private void populateTheMaps(Class<?> clazz)
				throws InstanceWrapperException {
			PropertyAnnotationsMap superMap = new PropertyAnnotationsMap(
					clazz.getSuperclass());
			mapByUri = superMap.byUri();
			mapByName = superMap.byName();
			for (Method method : clazz.getDeclaredMethods()) {
				String name = method.getName();

				Method matchByName = methodByName(name);
				if (matchByName != null) {
					throw new InstanceWrapperException("Method " + name
							+ " in " + clazz.getName()
							+ " conflicts with a property method in "
							+ matchByName.getDeclaringClass().getName());
				}

				Property annotation = method.getAnnotation(Property.class);
				if (annotation == null) {
					continue;
				}

				if (!method.getReturnType().equals(Void.TYPE)) {
					throw new InstanceWrapperException("Property method '"
							+ method + "' should return void.");
				}

				if (method.getParameterTypes().length != 1) {
					throw new InstanceWrapperException("Property method '"
							+ method + "' must accept exactly one parameter.");
				}

				String uri = annotation.uri();
				Method matchByUri = methodByUri(uri);
				if (matchByUri != null) {
					throw new InstanceWrapperException(
							"Two property methods have the same URI value: "
									+ matchByUri + ", and " + method);
				}

				if (annotation.minOccurs() < 0) {
					throw new InstanceWrapperException(
							"minOccurs must not be negative.");
				}

				if (annotation.maxOccurs() < annotation.minOccurs()) {
					throw new InstanceWrapperException(
							"maxOccurs must not be less than minOccurs.");
				}

				try {
					PropertyMethod pm = PropertyType.createPropertyMethod(
							method, annotation);
					mapByUri.put(uri, pm);
					mapByName.put(name, pm);
				} catch (PropertyTypeException e) {
					throw new InstanceWrapperException(
							"Failed to create the PropertyMethod", e);
				}
			}

		}

		private Method methodByName(String name) {
			PropertyMethod pm = mapByName.get(name);
			return (pm == null) ? null : pm.getMethod();
		}

		private Method methodByUri(String name) {
			PropertyMethod pm = mapByUri.get(name);
			return (pm == null) ? null : pm.getMethod();
		}

		public Map<String, PropertyMethod> byUri() {
			return mapByUri;
		}

		public Map<String, PropertyMethod> byName() {
			return mapByName;
		}

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
