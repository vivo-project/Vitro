/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Locates the runtime configuration properties and stores them in the servlet
 * context.
 * 
 * This must be invoked before any listener that requires configuration
 * properties.
 * 
 * The properties are determined from a file called 'build.properties' in the
 * resources directory of the webapp, and a file called 'runtime.properties' in
 * the Vitro home directory. In case of conflict, runtime.properties wins.
 * 
 * The path to the Vitro home directory can be specifed by an JNDI value, or by
 * a System property, or by a property in build.properties, in that order. If
 * the Vitro home directory is specified in more than one way, a warning is
 * issued and the first value is used.
 * 
 * The value that was determined for 'vitro.home' is also included in the
 * ConfigurationProperties bean.
 * 
 * If build.properties or runtime.properties cannot be located or loaded, a
 * fatal error is registered to abort the startup.
 */
public class ConfigurationPropertiesSetup implements ServletContextListener {
	private static final Log log = LogFactory
			.getLog(ConfigurationPropertiesSetup.class);

	/** JNDI path that defines the Vitro home directory */
	private static final String VHD_JNDI_PATH = "java:comp/env/vitro/home";

	/** System property that defines the Vitro home directory */
	private static final String VHD_SYSTEM_PROPERTY = "vitro.home";

	/** build.properties property that defines the Vitro home directory */
	private static final String VHD_BUILD_PROPERTY = "vitro.home";

	/** Configuration property to store the Vitro home directory */
	private static final String VHD_CONFIGURATION_PROPERTY = "vitro.home";

	/** Name of the file that contains runtime properties. */
	private static final String FILE_RUNTIME_PROPERTIES = "runtime.properties";

	/** Path to the file of build properties baked into the webapp. */
	private static final String PATH_BUILD_PROPERTIES = "/WEB-INF/resources/build.properties";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			InputStream stream = null;
			try {
				File vitroHomeDir = locateVitroHomeDirectory(ctx, ss);

				File runtimePropertiesFile = locateRuntimePropertiesFile(
						vitroHomeDir, ss);
				stream = new FileInputStream(runtimePropertiesFile);

				Map<String, String> preempts = createPreemptiveProperties(
						VHD_CONFIGURATION_PROPERTY, vitroHomeDir);
				
				ConfigurationPropertiesImpl bean = new ConfigurationPropertiesImpl(
						stream, preempts, getBuildProperties(ctx));

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
			ss.fatal(this, e.getMessage(), e);
		}
	}

	/**
	 * Look in the JDNI environment, the system properties, and the
	 * build.properties file.
	 * 
	 * If we don't find it, fail. If we find it more than once, warn and use the
	 * first one.
	 * 
	 * Confirm that it is an existing, readable directory.
	 */
	private File locateVitroHomeDirectory(ServletContext ctx, StartupStatus ss) {
		Map<String, String> whereWasIt = new LinkedHashMap<String, String>();
		getVhdFromJndi(whereWasIt);
		getVhdFromSystemProperties(whereWasIt);
		getVhdFromBuildProperties(ctx, whereWasIt);

		if (whereWasIt.isEmpty()) {
			String message = String.format("Can't find a value "
					+ "for the Vitro home directory. "
					+ "Looked in JNDI environment at '%s'. "
					+ "Looked for a system property named '%s'. "
					+ "Looked in 'WEB-INF/resources/build.properties' "
					+ "for '%s'.", VHD_JNDI_PATH, VHD_SYSTEM_PROPERTY,
					VHD_BUILD_PROPERTY);
			throw new IllegalStateException(message);
		} else if (whereWasIt.size() > 1) {
			String message = String.format("Found multiple values for the "
					+ "Vitro home directory: " + whereWasIt.keySet());
			ss.warning(this, message);
		}
		String message = whereWasIt.keySet().iterator().next();
		String vhdPath = whereWasIt.values().iterator().next();

		ss.info(this, message);

		File vhd = new File(vhdPath);
		if (!vhd.exists()) {
			throw new IllegalStateException("Vitro home directory '" + vhdPath
					+ "' does not exist.");
		}
		if (!vhd.isDirectory()) {
			throw new IllegalStateException("Vitro home directory '" + vhdPath
					+ "' is not a directory.");
		}
		if (!vhd.canRead()) {
			throw new IllegalStateException("Vitro home directory '" + vhdPath
					+ "' cannot be read.");
		}
		if (!vhd.canWrite()) {
			throw new IllegalStateException(
					"Can't write to Vitro home directory: '" + vhdPath + "'.");
		}

		return vhd;
	}

	private void getVhdFromJndi(Map<String, String> whereWasIt) {
		try {
			String vhdPath = (String) new InitialContext()
					.lookup(VHD_JNDI_PATH);

			if (vhdPath == null) {
				log.debug("Didn't find a JNDI value at '" + VHD_JNDI_PATH
						+ "'.");
				return;
			}

			log.debug("'" + VHD_JNDI_PATH + "' as specified by JNDI: "
					+ vhdPath);
			String message = String.format(
					"JNDI environment '%s' was set to '%s'", VHD_JNDI_PATH,
					vhdPath);
			whereWasIt.put(message, vhdPath);
		} catch (NamingException e) {
			log.debug("JNDI lookup failed. " + e);
		}
	}

	private void getVhdFromSystemProperties(Map<String, String> whereWasIt) {
		String vhdPath = System.getProperty(VHD_SYSTEM_PROPERTY);

		if (vhdPath == null) {
			log.debug("Didn't find a system property value at '"
					+ VHD_SYSTEM_PROPERTY + "'.");
			return;
		}

		log.debug("'" + VHD_SYSTEM_PROPERTY
				+ "' as specified by system property: " + vhdPath);
		String message = String.format("System property '%s' was set to '%s'",
				VHD_SYSTEM_PROPERTY, vhdPath);
		whereWasIt.put(message, vhdPath);
	}

	private void getVhdFromBuildProperties(ServletContext ctx,
			Map<String, String> whereWasIt) {
		Map<String, String> buildProps = getBuildProperties(ctx);
		String vhdPath = buildProps.get(VHD_BUILD_PROPERTY);
		if (vhdPath == null) {
			log.debug("'" + PATH_BUILD_PROPERTIES
					+ "' didn't contain a value for '" + VHD_BUILD_PROPERTY
					+ "'.");
			return;
		}

		log.debug("'" + VHD_BUILD_PROPERTY
				+ "' as specified by build.properties: " + vhdPath);
		String message = String.format(
				"In resource '%s', '%s' was set to '%s'.",
				PATH_BUILD_PROPERTIES, VHD_BUILD_PROPERTY, vhdPath);
		whereWasIt.put(message, vhdPath);
	}

	private Map<String, String> getBuildProperties(ServletContext ctx) {
		Map<String, String> map = new HashMap<>();

		try (InputStream stream = ctx
				.getResourceAsStream(PATH_BUILD_PROPERTIES)) {
			if (stream == null) {
				log.debug("Didn't find a resource at '" + PATH_BUILD_PROPERTIES
						+ "'.");
			} else {

				Properties props = new Properties();
				props.load(stream);
				for (String key : props.stringPropertyNames()) {
					map.put(key, props.getProperty(key));
				}
			}
		} catch (IOException e) {
			throw new SetupException("Failed to load from '"
					+ PATH_BUILD_PROPERTIES + "'.", e);
		}
		return map;
	}

	private File locateRuntimePropertiesFile(File vitroHomeDir, StartupStatus ss) {
		File rpf = new File(vitroHomeDir, FILE_RUNTIME_PROPERTIES);

		if (!rpf.exists()) {
			throw new IllegalStateException("Did not find '"
					+ FILE_RUNTIME_PROPERTIES + "' in vitro home directory '"
					+ vitroHomeDir + "'");
		}
		if (!rpf.isFile()) {
			throw new IllegalStateException("'" + rpf.getPath()
					+ "' is not a file.");
		}
		if (!rpf.canRead()) {
			throw new IllegalStateException("Cannot read '" + rpf.getPath()
					+ "'.");
		}
		ss.info(this, "Loading runtime properties from '" + rpf.getPath() + "'");
		return rpf;
	}

	private Map<String, String> createPreemptiveProperties(
			String propertyVitroHome, File vitroHomeDir) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(propertyVitroHome, vitroHomeDir.getAbsolutePath());
		return map;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ConfigurationProperties.removeBean(sce.getServletContext());
	}

	/**
	 * Indicates a problem setting up. Abandon ship.
	 */
	private static class SetupException extends RuntimeException {
		public SetupException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
