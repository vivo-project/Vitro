/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides an mechanism for modules to read the configuration properties that
 * are attached to the servlet context.
 *
 * The customary behavior is for ConfigurationPropertiesSetup to create a
 * ConfigurationPropertiesImpl, which will obtain the properties from the
 * build.properties file and the runtime.properties file.
 */
public abstract class ConfigurationProperties {
	private static final Log log = LogFactory
			.getLog(ConfigurationProperties.class);
	
	/** If they ask for a bean before one has been set, they get this. */
	private static final ConfigurationProperties DUMMY_PROPERTIES = new DummyConfigurationProperties();

    private static ConfigurationProperties INSTANCE = DUMMY_PROPERTIES;

    public static ConfigurationProperties getInstance() {
        if (INSTANCE == DUMMY_PROPERTIES) {
            log.error("ConfigurationProperties bean has not been set.");
        }
        return INSTANCE;
    }

    public static void setInstance(ConfigurationProperties props) {
        INSTANCE = props;
    }
	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

    @Deprecated
	public static ConfigurationProperties getBean(ServletRequest request) {
        return getInstance();

	}
    @Deprecated
	public static ConfigurationProperties getBean(HttpSession session) {
        return getInstance();

	}
    @Deprecated
	public static ConfigurationProperties getBean(HttpServlet servlet) {
        return getInstance();

	}
    @Deprecated
	public static ConfigurationProperties getBean(ServletContextEvent sce) {
        return getInstance();

	}
    @Deprecated
	public static ConfigurationProperties getBean(ServletConfig servletConfig) {
        return getInstance();

	}
    @Deprecated
	public static ConfigurationProperties getBean(ServletContext context) {
		return getInstance();
	}

	/**
	 * Protected access, so the Stub class can call it for unit tests.
	 * Otherwise, this should only be called by ConfigurationPropertiesSetup.
	 */
    @Deprecated
	protected static void setBean(ConfigurationProperties bean) {
	    setInstance(bean);
	}

    @Deprecated
	/** Package access, so unit tests can call it. */
	static void removeBean(ServletContext context) {
	    INSTANCE = DUMMY_PROPERTIES;
	}

	// ----------------------------------------------------------------------
	// The interface
	// ----------------------------------------------------------------------

	/**
	 * Get the value of the property, or {@code null} if the property has
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
