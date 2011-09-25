/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Reads the configuration properties from a file and stores them in the servlet
 * context.
 * 
 * This must be invoked before any listener that requires configuration
 * properties.
 * 
 * The path to the file can be specified by an Environment name in the Context,
 * like this:
 * 
 * <pre>
 * 
 * <Context override="true">
 *     <Environment name="path.configuration" 
 *         value="/wherever/the/file/lives/deploy.properties"
 *         type="java.lang.String" 
 *         override="false" />
 * </Context>
 * 
 * </pre>
 * 
 * We look in this environment variable to find the path to the properties file.
 * If there is no such environment variable, the default path is used.
 * 
 * Once the path has been determined, we will use it to look for a resource in
 * the classpath. So if the path is "deploy.properties", it might be found in
 * "tomcat/webapps/vivo/WEB-INF/classes/deploy.properties". Of course, it might
 * also be found in any other portion of the classpath as well.
 * 
 * If we can't find the resource in the classpath, we will use it to look for an
 * external file. So, one might reasonably set this value to something like
 * "/usr/local/vitro/stuff/my.deploy.properties".
 * 
 * If neither a resource nor an external file can be found, we throw an
 * exception and set AbortStartup.
 */
public class ConfigurationPropertiesSetup implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(ConfigurationPropertiesSetup.class);

	/**
	 * The JNDI naming context where Tomcat stores environment attributes.
	 */
	static final String JNDI_BASE = "java:comp/env";

	/**
	 * The name of the JNDI environment mapping for the path to the
	 * configuration file (or resource).
	 */
	static final String PATH_CONFIGURATION = "path.configuration";

	/**
	 * If we don't find the path to the config properties from a JNDI mapping,
	 * use this. Not final, so we can jigger it for unit tests.
	 */
	private static String DEFAULT_CONFIG_PATH = "deploy.properties";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			InputStream stream = null;
			try {
				stream = locatePropertiesFile();
				
				ConfigurationPropertiesImpl bean = new ConfigurationPropertiesImpl(
						stream);
				ConfigurationProperties.setBean(ctx, bean);
				
				ss.info(this, "Loaded " + bean.getPropertyMap().size()
						+ " properties.");
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						log.error(e, e);
					}
				}
			}
		} catch (Exception e) {
			log.error(e, e);
			ss.fatal(this, e.getMessage(), e);
		}
	}

	private InputStream locatePropertiesFile() {
		String path = determinePathToProperties();
		log.debug("Configuration properties path is '" + path + "'");

		if (resourceExists(path)) {
			log.debug("Found configuration properties as a resource.");
			return getResourceStream(path);
		}

		if (externalFileExists(path)) {
			log.debug("Found configuration properties as an external file.");
			return getExternalFileStream(path);
		}

		throw new IllegalStateException("Can't find the properties file at '"
				+ path + "'");
	}

	/**
	 * If we can't find it with JNDI, use the default.
	 */
	private String determinePathToProperties() {
		try {
			Context envCtx = (Context) new InitialContext().lookup(JNDI_BASE);
			if (envCtx == null) {
				log.debug("JNDI Lookup on '" + JNDI_BASE + "' failed.");
				return DEFAULT_CONFIG_PATH;
			}

			String configPath = (String) envCtx.lookup(PATH_CONFIGURATION);
			if (configPath == null) {
				log.debug("JNDI Lookup on '" + PATH_CONFIGURATION + "' failed.");
				return DEFAULT_CONFIG_PATH;
			}

			log.debug("deploy.property as specified by JNDI: " + configPath);
			return configPath;
		} catch (NamingException e) {
			log.warn("JNDI lookup failed. "
					+ "Using default path for config properties.", e);
			return DEFAULT_CONFIG_PATH;
		}
	}

	private boolean resourceExists(String path) {
		return getResourceStream(path) != null;
	}

	private InputStream getResourceStream(String path) {
		return getClass().getClassLoader().getResourceAsStream(path);
	}

	private boolean externalFileExists(String path) {
		File file = new File(path);
		return file.isFile();
	}

	private InputStream getExternalFileStream(String path) {
		InputStream stream = null;
		File file = new File(path);
		if (file.isFile()) {
			try {
				stream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// testing file.isFile() should have prevented this
				log.error(e, e);
			}
		}
		return stream;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ConfigurationProperties.removeBean(sce.getServletContext());
	}

}
