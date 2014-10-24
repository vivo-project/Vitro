/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.utils.jena.Critical;

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

	public static boolean isJavaUri(String uri) {
		return uri.startsWith(JAVA_URI_PREFIX);
	}

	public static String fromJavaUri(String uri) {
		if (!isJavaUri(uri)) {
			throw new IllegalArgumentException("Not a java class URI: '" + uri
					+ "'");
		}
		return uri.substring(JAVA_URI_PREFIX.length());
	}

	// ----------------------------------------------------------------------
	// the instance
	// ----------------------------------------------------------------------

	/** Must not be null. */
	private final Model model;

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
		this(model, null, null);
	}

	public ConfigurationBeanLoader(Model model, ServletContext ctx) {
		this(model, ctx, null);
	}

	public ConfigurationBeanLoader(Model model, HttpServletRequest req) {
		this(model,
				(req == null) ? null : req.getSession().getServletContext(),
				req);
	}

	private ConfigurationBeanLoader(Model model, ServletContext ctx,
			HttpServletRequest req) {
		if (model == null) {
			throw new NullPointerException("model may not be null.");
		}

		this.model = model;
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
			ConfigurationRdf<T> parsedRdf = ConfigurationRdfParser.parse(model,
					uri, resultClass);
			WrappedInstance<T> wrapper = InstanceWrapper.wrap(parsedRdf
					.getConcreteClass());
			wrapper.satisfyInterfaces(ctx, req);
			wrapper.setProperties(this, parsedRdf.getPropertyStatements());
			wrapper.validate();
			return wrapper.getInstance();
		} catch (Exception e) {
			throw new ConfigurationBeanLoaderException("Failed to load '" + uri
					+ "'", e);
		}
	}

	/**
	 * Find all of the resources with the specified class, and instantiate them.
	 */
	public <T> Set<T> loadAll(Class<T> resultClass)
			throws ConfigurationBeanLoaderException {
		Set<String> uris = new HashSet<>();
		try (Critical section = Critical.read(model)) {
			List<Resource> resources = model.listResourcesWithProperty(
					RDF.type, createResource(toJavaUri(resultClass))).toList();
			for (Resource r : resources) {
				if (r.isURIResource()) {
					uris.add(r.getURI());
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
