/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.fromJavaUri;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.isJavaUri;
import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.toJavaUri;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyStatement;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.PropertyType.PropertyTypeException;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedModel;

/**
 * Parse the RDF for a single individual in the model to create a
 * ConfigurationRdf object.
 */
public class ConfigurationRdfParser {
	public static <T> ConfigurationRdf<T> parse(LockableModel locking,
			String uri, Class<T> resultClass)
			throws InvalidConfigurationRdfException {
		Objects.requireNonNull(locking, "locking may not be null.");
		Objects.requireNonNull(uri, "uri may not be null.");
		Objects.requireNonNull(resultClass, "resultClass may not be null.");

		confirmExistenceInModel(locking, uri);

		confirmEligibilityForResultClass(locking, uri, resultClass);

		Set<PropertyStatement> properties = loadProperties(locking, uri);

		Class<? extends T> concreteClass = determineConcreteClass(locking, uri,
				resultClass);

		return new ConfigurationRdf<T>(concreteClass, properties);
	}

	private static void confirmExistenceInModel(LockableModel locking,
			String uri) throws InvalidConfigurationRdfException {
		Selector s = new SimpleSelector(createResource(uri), null,
				(RDFNode) null);
		try (LockedModel m = locking.read()) {
			if (m.listStatements(s).toList().isEmpty()) {
				throw individualDoesNotAppearInModel(uri);
			}
		}
	}

	private static void confirmEligibilityForResultClass(LockableModel locking,
			String uri, Class<?> resultClass)
			throws InvalidConfigurationRdfException {
		Statement s = createStatement(createResource(uri), RDF.type,
				createResource(toJavaUri(resultClass)));
		try (LockedModel m = locking.read()) {
			if (!m.contains(s)) {
				throw noTypeStatementForResultClass(s);
			}
		}
	}

	private static Set<PropertyStatement> loadProperties(LockableModel locking,
			String uri) throws InvalidConfigurationRdfException {
		Set<PropertyStatement> set = new HashSet<>();

		try (LockedModel m = locking.read()) {
			List<Statement> rawStatements = m.listStatements(
					m.getResource(uri), (Property) null, (RDFNode) null)
					.toList();
			if (rawStatements.isEmpty()) {
				throw noRdfStatements(uri);
			}

			for (Statement s : rawStatements) {
				if (s.getPredicate().equals(RDF.type)) {
					continue;
				} else {
					try {
						set.add(PropertyType.createPropertyStatement(s));
					} catch (PropertyTypeException e) {
						throw new InvalidConfigurationRdfException(
								"Invalid property statement on '" + uri + "'",
								e);
					}
				}
			}
			return set;
		}
	}

	private static <T> Class<? extends T> determineConcreteClass(
			LockableModel locking, String uri, Class<T> resultClass)
			throws InvalidConfigurationRdfException {
		Set<Class<? extends T>> concreteClasses = new HashSet<>();

		try (LockedModel m = locking.read()) {
			for (RDFNode node : m.listObjectsOfProperty(createResource(uri),
					RDF.type).toSet()) {
				if (!node.isURIResource()) {
					throw typeMustBeUriResource(node);
				}

				String typeUri = node.asResource().getURI();
				if (!isConcreteClass(typeUri)) {
					continue;
				}

				concreteClasses.add(processTypeUri(typeUri, resultClass));
			}
		}

		if (concreteClasses.isEmpty()) {
			throw noConcreteClasses(uri);
		}

		if (concreteClasses.size() > 1) {
			throw tooManyConcreteClasses(uri, concreteClasses);
		}

		return concreteClasses.iterator().next();
	}

	private static boolean isConcreteClass(String typeUri)
			throws InvalidConfigurationRdfException {
		try {
			if (!isJavaUri(typeUri)) {
				return false;
			}
			Class<?> clazz = Class.forName(fromJavaUri(typeUri));
			if (clazz.isInterface()) {
				return false;
			}
			if (Modifier.isAbstract(clazz.getModifiers())) {
				return false;
			}
			return true;
		} catch (ClassNotFoundException | ExceptionInInitializerError e) {
			throw failedToLoadClass(typeUri, e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<? extends T> processTypeUri(String typeUri,
			Class<T> resultClass) throws InvalidConfigurationRdfException {
		try {
			Class<?> clazz = Class.forName(fromJavaUri(typeUri));
			if (!resultClass.isAssignableFrom(clazz)) {
				throw notAssignable(resultClass, clazz);
			}
			try {
				clazz.getConstructor();
			} catch (NoSuchMethodException e) {
				throw noZeroArgumentConstructor(clazz);
			} catch (SecurityException e) {
				throw constructorNotPublic(clazz);
			}
			return (Class<? extends T>) clazz;
		} catch (ClassNotFoundException e) {
			throw failedToLoadClass(typeUri, e);
		}
	}

	private static InvalidConfigurationRdfException individualDoesNotAppearInModel(
			String uri) {
		return new InvalidConfigurationRdfException(
				"The model contains no statements about '" + uri + "'");
	}

	private static InvalidConfigurationRdfException noConcreteClasses(String uri) {
		return new InvalidConfigurationRdfException(
				"No concrete class is declared for '" + uri + "'");
	}

	private static InvalidConfigurationRdfException tooManyConcreteClasses(
			String uri, Set<?> concreteClasses) {
		return new InvalidConfigurationRdfException("'" + uri
				+ "' is declared with more than one " + "concrete class: "
				+ concreteClasses);
	}

	private static InvalidConfigurationRdfException notAssignable(
			Class<?> resultClass, Class<?> clazz) {
		return new InvalidConfigurationRdfException(clazz
				+ " cannot be assigned to " + resultClass);
	}

	private static InvalidConfigurationRdfException noZeroArgumentConstructor(
			Class<?> clazz) {
		return new InvalidConfigurationRdfException("Can't instantiate '"
				+ clazz + "': no zero-argument constructor.");
	}

	private static InvalidConfigurationRdfException constructorNotPublic(
			Class<?> clazz) {
		return new InvalidConfigurationRdfException("Can't instantiate '"
				+ clazz + "': zero-argument constructor is not public.");
	}

	private static InvalidConfigurationRdfException failedToLoadClass(
			String typeUri, Throwable e) {
		return new InvalidConfigurationRdfException("Can't load this type: '"
				+ typeUri + "'", e);
	}

	private static InvalidConfigurationRdfException typeMustBeUriResource(
			RDFNode node) {
		return new InvalidConfigurationRdfException(
				"Type must be a URI Resource: " + node);
	}

	private static InvalidConfigurationRdfException noTypeStatementForResultClass(
			Statement s) {
		return new InvalidConfigurationRdfException(
				"A type statement is required: '" + s);
	}

	private static InvalidConfigurationRdfException noRdfStatements(String uri) {
		return new InvalidConfigurationRdfException("'" + uri
				+ "' does not appear as the subject of any "
				+ "statements in the model.");
	}

	public static class InvalidConfigurationRdfException extends Exception {
		public InvalidConfigurationRdfException(String message) {
			super(message);
		}

		public InvalidConfigurationRdfException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
