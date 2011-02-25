/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides an mechanism for modules to read the configuration properties that
 * are attached to the servlet context.
 * 
 * The customary behavior is for ConfigurationPropertiesSetup to create a
 * ConfigurationPropertiesImpl, which will parse the deploy.properties file for
 * these properties.
 */
public abstract class ConfigurationProperties {
	private static final Log log = LogFactory
			.getLog(ConfigurationProperties.class);

	/** The bean is attached to the session by this name. */
	private static final String ATTRIBUTE_NAME = ConfigurationProperties.class
			.getName();

	/** If they ask for a bean before one has been set, they get this. */
	private static final ConfigurationProperties DUMMY_PROPERTIES = new DummyConfigurationProperties();

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	public static ConfigurationProperties getBean(ServletRequest request) {
		if (request == null) {
			throw new NullPointerException("request may not be null.");
		}
		if (!(request instanceof HttpServletRequest)) {
			throw new IllegalArgumentException(
					"request must be an HttpServletRequest");
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		return getBean(httpRequest.getSession());
	}

	public static ConfigurationProperties getBean(HttpSession session) {
		if (session == null) {
			throw new NullPointerException("session may not be null.");
		}
		return getBean(session.getServletContext());
	}

	public static ConfigurationProperties getBean(HttpServlet servlet) {
		if (servlet == null) {
			throw new NullPointerException("servlet may not be null.");
		}
		return getBean(servlet.getServletContext());
	}

	public static ConfigurationProperties getBean(ServletContextEvent sce) {
		if (sce == null) {
			throw new NullPointerException("sce may not be null.");
		}
		return getBean(sce.getServletContext());
	}

	public static ConfigurationProperties getBean(ServletConfig servletConfig) {
		if (servletConfig == null) {
			throw new NullPointerException("servletConfig may not be null.");
		}
		return getBean(servletConfig.getServletContext());
	}

	public static ConfigurationProperties getBean(ServletContext context) {
		if (context == null) {
			throw new NullPointerException("context may not be null.");
		}

		Object o = context.getAttribute(ATTRIBUTE_NAME);
		if (o == null) {
			log.error("ConfigurationProperties bean has not been set.");
			return DUMMY_PROPERTIES;
		} else if (!(o instanceof ConfigurationProperties)) {
			log.error("Error: ConfigurationProperties was set to an "
					+ "invalid object: " + o);
			return DUMMY_PROPERTIES;
		}

		return (ConfigurationProperties) o;
	}

	/**
	 * Protected access, so the Stub class can call it for unit tests.
	 * Otherwise, this should only be called by ConfigurationPropertiesSetup.
	 */
	protected static void setBean(ServletContext context,
			ConfigurationProperties bean) {
		if (context == null) {
			throw new NullPointerException("context may not be null.");
		}
		if (bean == null) {
			throw new NullPointerException("bean may not be null.");
		}
		context.setAttribute(ATTRIBUTE_NAME, bean);
		log.info(bean);
	}

	/** Package access, so unit tests can call it. */
	static void removeBean(ServletContext context) {
		if (context == null) {
			throw new NullPointerException("context may not be null.");
		}
		context.removeAttribute(ATTRIBUTE_NAME);
	}

	// ----------------------------------------------------------------------
	// The interface
	// ----------------------------------------------------------------------

	/**
	 * Get the value of the property, or <code>null</code> if the property has
	 * not been assigned a value.
	 */
	public abstract String getProperty(String key);

	/**
	 * Get the value of the property, or use the default value if the property
	 * has not been assigned a value.
	 */
	public abstract String getProperty(String key, String defaultValue);

	/**
	 * Get a copy of the map of the configuration properties and their settings.
	 * Because this is a copy, it cannot be used to modify the settings.
	 */
	public abstract Map<String, String> getPropertyMap();

}
