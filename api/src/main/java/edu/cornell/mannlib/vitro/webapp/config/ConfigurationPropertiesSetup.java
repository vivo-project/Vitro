/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.application.BuildProperties;
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

	/** Name of the file that contains runtime properties. */
	private static final String FILE_RUNTIME_PROPERTIES = "runtime.properties";

	/** Configuration property to store the Vitro home directory */
	private static final String VHD_CONFIGURATION_PROPERTY = "vitro.home";

	/** Configuration property used to determine if there are runtime.properties files in multiple locations **/
	static final String RP_MULTIPLE = "rp.multiple";

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			InputStream stream = null;
			try {
				File vitroHomeDir = ApplicationUtils.instance()
						.getHomeDirectory().getPath().toFile();

				File vitroHomeDirConfig = new File(vitroHomeDir.getPath()
						.concat(File.separator).concat("config"));

				String rpfLocation = findMultipleRuntimePropertiesFiles(
						vitroHomeDir, vitroHomeDirConfig);

				File runtimePropertiesFile = locateRuntimePropertiesFile(
					vitroHomeDir, vitroHomeDirConfig, ss);
				stream = new FileInputStream(runtimePropertiesFile);

				Map<String, String> preempts = createPreemptiveProperties(
						VHD_CONFIGURATION_PROPERTY, vitroHomeDir, RP_MULTIPLE, rpfLocation);

				ConfigurationPropertiesImpl bean = new ConfigurationPropertiesImpl(
						stream, preempts, new BuildProperties(ctx).getMap());

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

	private String findMultipleRuntimePropertiesFiles(File vitroHomeDir,
			File vitroHomeDirConfig) {

		File rpf = new File(vitroHomeDir, FILE_RUNTIME_PROPERTIES);
		File rpfc = new File(vitroHomeDirConfig, FILE_RUNTIME_PROPERTIES);

		if (rpf.exists() && !rpfc.exists()) {
			return "home";
		} else if (rpf.exists() && rpfc.exists()) {
			return "both";
		} else if (rpfc.exists()) {
			return "config";
		} else {
			throw new IllegalStateException("Did not find '"
					+ FILE_RUNTIME_PROPERTIES + "' in vitro home directory '"
					+ vitroHomeDir + "' or config directory '" + vitroHomeDirConfig + "'");
		}
	}

	private File locateRuntimePropertiesFile(File vitroHomeDir,
			File vitroHomeDirConfig, StartupStatus ss) {

		File rpf = new File(vitroHomeDir, FILE_RUNTIME_PROPERTIES);
		File rpfc = new File(vitroHomeDirConfig, FILE_RUNTIME_PROPERTIES);

		if (!rpf.exists()) {
			rpf = rpfc;
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
			String propertyVitroHome, File vitroHomeDir, String propertyRpfMultiple,
			String rpfLocation) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(propertyVitroHome, vitroHomeDir.getAbsolutePath());
		map.put(propertyRpfMultiple, rpfLocation);
		return map;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// No need to remove the bean. It's only a map of strings, and if we
		// restart the app, it will be replaced.
	}
}
