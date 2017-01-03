/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.application;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoaderException;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableModel;

/**
 * Read the application setup file and create the components it describes.
 */
public class ApplicationSetup implements ServletContextListener {
	private static final String APPLICATION_SETUP_PATH = "config/applicationSetup.n3";

	private ServletContext ctx;
	private StartupStatus ss;

	private ApplicationImpl app;

	private VitroHomeDirectory vitroHomeDir;

	private Path configFile;
	private LockableModel configModel;
	private ConfigurationBeanLoader loader;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			this.ctx = sce.getServletContext();
			this.ss = StartupStatus.getBean(ctx);

			this.vitroHomeDir = VitroHomeDirectory.find(ctx);
			ss.info(this, vitroHomeDir.getDiscoveryMessage());

			locateApplicationConfigFile();
			loadApplicationConfigFile();
			createConfigurationBeanLoader();
			instantiateTheApplication();

			app.setServletContext(this.ctx);
			app.setHomeDirectory(this.vitroHomeDir);

			ApplicationUtils.setInstance(app);
			ss.info(this, "Application is configured.");
		} catch (Exception e) {
			ss.fatal(this, "Failed to initialize the Application.", e);
		}
	}

	private void locateApplicationConfigFile() {
		Path path = this.vitroHomeDir.getPath().resolve(APPLICATION_SETUP_PATH);
		if (!Files.exists(path)) {
			throw new IllegalStateException("'" + path + "' does not exist.");
		}
		if (!Files.isReadable(path)) {
			throw new IllegalStateException("Can't read '" + path + "'");
		}
		this.configFile = path;
	}

	private void loadApplicationConfigFile() {
		try (InputStream in = Files.newInputStream(this.configFile)) {
			Model m = ModelFactory.createDefaultModel();
			m.read(in, null, "N3");
			this.configModel = new LockableModel(m);
		} catch (Exception e) {
			throw new RuntimeException("Failed to read '" + this.configFile
					+ "'", e);
		}
	}

	private void createConfigurationBeanLoader() {
		this.loader = new ConfigurationBeanLoader(configModel);
	}

	private void instantiateTheApplication() {
		try {
			Set<ApplicationImpl> apps = loader.loadAll(ApplicationImpl.class);
			if (apps.isEmpty()) {
				throw new IllegalStateException("'" + this.configFile
						+ "' does not define an instance of "
						+ ApplicationImpl.class.getName());
			} else if (apps.size() > 1) {
				throw new IllegalStateException("'" + this.configFile
						+ "' defines " + apps.size() + " instances of "
						+ ApplicationImpl.class.getName());
			} else {
				this.app = apps.iterator().next();
			}
		} catch (ConfigurationBeanLoaderException e) {
			throw new IllegalStateException("Failed to setup the application",
					e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do.
	}
}
