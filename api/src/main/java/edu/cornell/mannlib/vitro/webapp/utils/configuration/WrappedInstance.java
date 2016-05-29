/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyMethod;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyStatement;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyTypeException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.ResourcePropertyStatement;

/**
 * An instance of a ConfigurationBean, packaged with the distilled information
 * about the annotated methods on the class.
 */
public class WrappedInstance<T> {
	private final T instance;
	private final Map<String, PropertyMethod> propertyMethods;
	private final Set<Method> validationMethods;

	public WrappedInstance(T instance,
			Map<String, PropertyMethod> propertyMethods,
			Set<Method> validationMethods) {
		this.instance = instance;
		this.propertyMethods = propertyMethods;
		this.validationMethods = validationMethods;
	}

	/**
	 * The loader calls this as soon as the instance is created.
	 * 
	 * If the loader did not have access to a request object, then req will be
	 * null. If the instance expects request models, an exception will be
	 * thrown.
	 */
	public void satisfyInterfaces(ServletContext ctx, HttpServletRequest req)
			throws ResourceUnavailableException {
		if (instance instanceof ContextModelsUser) {
			if (ctx == null) {
				throw new ResourceUnavailableException("Cannot satisfy "
						+ "ContextModelsUser interface: context not available.");
			} else {
				ContextModelsUser cmu = (ContextModelsUser) instance;
				cmu.setContextModels(ModelAccess.on(ctx));
			}
		}
		if (instance instanceof RequestModelsUser) {
			if (req == null) {
				throw new ResourceUnavailableException("Cannot satisfy "
						+ "RequestModelsUser interface: request not available.");
			} else {
				RequestModelsUser rmu = (RequestModelsUser) instance;
				rmu.setRequestModels(ModelAccess.on(req));
			}
		}
	}

	/**
	 * The loader provides the distilled property statements from the RDF. Check
	 * that they satisfy the cardinality requested on their methods.
	 */
	public void checkCardinality(Set<PropertyStatement> propertyStatements)
			throws CardinalityException {
		Map<String, Integer> statementCounts = countPropertyStatementsByPredicateUri(propertyStatements);
		for (PropertyMethod pm : propertyMethods.values()) {
			Integer c = statementCounts.get(pm.getPropertyUri());
			int count = (c == null) ? 0 : c;
			if (count < pm.getMinOccurs()) {
				throw new CardinalityException("Expecting at least "
						+ pm.getMinOccurs() + " values for '"
						+ pm.getPropertyUri() + "', but found " + count + ".");
			}
			if (count > pm.getMaxOccurs()) {
				throw new CardinalityException("Expecting no more than "
						+ pm.getMaxOccurs() + " values for '"
						+ pm.getPropertyUri() + "', but found " + count + ".");
			}
		}
		statementCounts.hashCode();
	}

	private Map<String, Integer> countPropertyStatementsByPredicateUri(
			Set<PropertyStatement> propertyStatements) {
		Map<String, Integer> statementCounts = new HashMap<>();
		for (String pmPredicateUri : propertyMethods.keySet()) {
			int count = 0;
			for (PropertyStatement ps : propertyStatements) {
				if (ps.getPredicateUri().equals(pmPredicateUri)) {
					count++;
				}
			}
			statementCounts.put(pmPredicateUri, count);
		}
		return statementCounts;
	}

	/**
	 * The loader provides the distilled property statements from the RDF, to
	 * populate the instance.
	 */
	public void setProperties(ConfigurationBeanLoader loader,
			Collection<PropertyStatement> propertyStatements)
			throws PropertyTypeException, NoSuchPropertyMethodException,
			ConfigurationBeanLoaderException {
		for (PropertyStatement ps : propertyStatements) {
			PropertyMethod pm = propertyMethods.get(ps.getPredicateUri());
			if (pm == null) {
				throw new NoSuchPropertyMethodException(ps);
			}
			pm.confirmCompatible(ps);

			if (ps instanceof ResourcePropertyStatement) {
				ResourcePropertyStatement rps = (ResourcePropertyStatement) ps;
				Object subordinate = loader.loadInstance(rps.getValue(),
						pm.getParameterType());
				pm.invoke(instance, subordinate);
			} else {
				pm.invoke(instance, ps.getValue());
			}
		}
	}

	/**
	 * After the interfaces have been satisfied and the instance has been
	 * populated, call any validation methods to see whether the instance is
	 * viable.
	 */
	public void validate() throws ValidationFailedException {
		for (Method method : validationMethods) {
			try {
				method.invoke(instance);
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new ValidationFailedException(
						"Error executing validation method '" + method + "'", e);
			}
		}
	}

	/**
	 * Once satisfied, populated, and validated, the instance is ready to go.
	 */
	public T getInstance() {
		return instance;
	}

	public static class ResourceUnavailableException extends Exception {
		public ResourceUnavailableException(String message) {
			super(message);
		}
	}

	public static class ValidationFailedException extends Exception {
		public ValidationFailedException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	public static class NoSuchPropertyMethodException extends Exception {
		public NoSuchPropertyMethodException(PropertyStatement ps) {
			super("No property method for '" + ps.getPredicateUri() + "'");
		}
	}

	public static class CardinalityException extends Exception {
		public CardinalityException(String message) {
			super(message);
		}
	}

}