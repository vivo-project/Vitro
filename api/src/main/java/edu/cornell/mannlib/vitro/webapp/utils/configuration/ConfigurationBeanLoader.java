/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import static org.apache.jena.rdf.model.ResourceFactory.createResource;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedModel;

/**
 * Load one or more Configuration beans from a specified model.
 */
public class ConfigurationBeanLoader {

	private static final String JAVA_URI_PREFIX = "java:";

	// ----------------------------------------------------------------------
	// utility methods
	// ----------------------------------------------------------------------

	public static String toJavaUri(Class<?> clazz) {
		return JAVA_URI_PREFIX + clazz.getName();
	}

	public static String toCanonicalJavaUri(String uri) {
		return uri.replace("#", ".");
	}

	public static boolean isJavaUri(String uri) {
		return uri.startsWith(JAVA_URI_PREFIX);
	}

	public static Set<String> toPossibleJavaUris(Class<?> clazz) {
		Set<String> set = new TreeSet<>();
		String[] uriPieces = toJavaUri(clazz).split("\\.");
		for (int hashIndex = 0; hashIndex < uriPieces.length; hashIndex++) {
			set.add(joinWithPeriodsAndAHash(uriPieces, hashIndex));
		}
		return set;
	}

	private static String joinWithPeriodsAndAHash(String[] pieces,
			int hashIndex) {
		StringBuilder buffer = new StringBuilder(pieces[0]);
		for (int i = 1; i < pieces.length; i++) {
			buffer.append(i == hashIndex ? '#' : '.').append(pieces[i]);
		}
		return buffer.toString();
	}

	public static String classnameFromJavaUri(String uri) {
		if (!isJavaUri(uri)) {
			throw new IllegalArgumentException(
					"Not a java class URI: '" + uri + "'");
		}
		return toCanonicalJavaUri(uri).substring(JAVA_URI_PREFIX.length());
	}

	public static boolean isMatchingJavaUri(String uri1, String uri2) {
		return toCanonicalJavaUri(uri1).equals(toCanonicalJavaUri(uri2));
	}

	// ----------------------------------------------------------------------
	// the instance
	// ----------------------------------------------------------------------

	/** Must not be null. */
	private final LockableModel locking;

	/**
	 * May be null, but the loader will be unable to satisfy instances of
	 * ContextModelUser.
	 */
	private final ServletContext ctx;

	/**
	 * May be null, but the loader will be unable to satisfy instances of
	 * RequestModelUser.
	 */
	private final HttpServletRequest req;

	public ConfigurationBeanLoader(Model model) {
		this(new LockableModel(model), null, null);
	}

	public ConfigurationBeanLoader(LockableModel locking) {
		this(locking, null, null);
	}

	public ConfigurationBeanLoader(Model model, ServletContext ctx) {
		this(new LockableModel(model), ctx, null);
	}

	public ConfigurationBeanLoader(LockableModel locking, ServletContext ctx) {
		this(locking, ctx, null);
	}

	public ConfigurationBeanLoader(Model model, HttpServletRequest req) {
		this(new LockableModel(model), req);
	}

	public ConfigurationBeanLoader(LockableModel locking,
			HttpServletRequest req) {
		this(locking,
				(req == null) ? null : req.getSession().getServletContext(),
				req);
	}

	private ConfigurationBeanLoader(LockableModel locking, ServletContext ctx,
			HttpServletRequest req) {
		this.locking = Objects.requireNonNull(locking,
				"locking may not be null.");
		this.req = req;
		this.ctx = ctx;
	}

	/**
	 * Load the instance with this URI, if it is assignable to this class.
	 */
	public <T> T loadInstance(String uri, Class<T> resultClass)
			throws ConfigurationBeanLoaderException {
		if (uri == null) {
			throw new NullPointerException("uri may not be null.");
		}
		if (resultClass == null) {
			throw new NullPointerException("resultClass may not be null.");
		}

		try {
			ConfigurationRdf<T> parsedRdf = ConfigurationRdfParser
					.parse(locking, uri, resultClass);
			WrappedInstance<T> wrapper = InstanceWrapper
					.wrap(parsedRdf.getConcreteClass());
			wrapper.satisfyInterfaces(ctx, req);
			wrapper.checkCardinality(parsedRdf.getPropertyStatements());
			wrapper.setProperties(this, parsedRdf.getPropertyStatements());
			wrapper.validate();
			return wrapper.getInstance();
		} catch (Exception e) {
			throw new ConfigurationBeanLoaderException(
					"Failed to load '" + uri + "'", e);
		}
	}

	/**
	 * Find all of the resources with the specified class, and instantiate them.
	 */
	public <T> Set<T> loadAll(Class<T> resultClass)
			throws ConfigurationBeanLoaderException {
		Set<String> uris = new HashSet<>();
		try (LockedModel m = locking.read()) {
			for (String typeUri : toPossibleJavaUris(resultClass)) {
				List<Resource> resources = m.listResourcesWithProperty(RDF.type,
						createResource(typeUri)).toList();
				for (Resource r : resources) {
					if (r.isURIResource()) {
						uris.add(r.getURI());
					}
				}
			}
		}

		Set<T> instances = new HashSet<>();
		for (String uri : uris) {
			instances.add(loadInstance(uri, resultClass));
		}
		return instances;
	}
}
