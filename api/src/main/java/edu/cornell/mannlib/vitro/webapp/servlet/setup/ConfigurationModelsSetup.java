/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY_DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY_TBOX;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Set up the models that use the CONFIGURATION RDFService. They are all mapped
 * to memory-based models.
 */
public class ConfigurationModelsSetup implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			setupModel(ctx, DISPLAY, "display");
			setupModel(ctx, DISPLAY_TBOX, "displayTbox");
			setupModel(ctx, DISPLAY_DISPLAY, "displayDisplay");
			setupModel(ctx, USER_ACCOUNTS, "auth");
			ss.info(this,
					"Set up the display models and the user accounts model.");
		} catch (Exception e) {
			ss.fatal(this, e.getMessage(), e.getCause());
		}
	}

	private void setupModel(ServletContext ctx, String modelUri,
			String modelPath) {
		try {
			OntModel ontModel = ModelAccess.on(ctx).getOntModel(modelUri);
			loadFirstTimeFiles(ctx, modelPath, ontModel);
			loadEveryTimeFiles(ctx, modelPath, ontModel);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create the '" + modelPath
					+ "' model (" + modelUri + ").", e);
		}
	}

	private void loadFirstTimeFiles(ServletContext ctx, String modelPath,
			OntModel baseModel) {
		RDFFilesLoader.loadFirstTimeFiles(modelPath, baseModel, baseModel.isEmpty());
	}

	private void loadEveryTimeFiles(ServletContext ctx, String modelPath,
			OntModel memoryModel) {
		RDFFilesLoader.loadEveryTimeFiles(modelPath, memoryModel);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down.
	}

}
