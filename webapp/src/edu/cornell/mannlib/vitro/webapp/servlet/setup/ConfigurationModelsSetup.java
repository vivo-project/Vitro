/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
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
			setupModel(ctx, ModelNames.DISPLAY, "display", ModelID.DISPLAY);

			setupModel(ctx, ModelNames.DISPLAY_TBOX, "displayTbox",
					ModelID.DISPLAY_TBOX);

			setupModel(ctx, ModelNames.DISPLAY_DISPLAY, "displayDisplay",
					ModelID.DISPLAY_DISPLAY);

			setupModel(ctx, ModelNames.USER_ACCOUNTS, "auth",
					ModelID.USER_ACCOUNTS);

			ss.info(this,
					"Set up the display models and the user accounts model.");
		} catch (Exception e) {
			ss.fatal(this, e.getMessage(), e.getCause());
		}
	}

	private void setupModel(ServletContext ctx, String modelUri,
			String modelPath, ModelID modelId) {
		try {
			OntModel ontModel = ModelAccess.on(ctx).getOntModel(modelId);
			loadFirstTimeFiles(ctx, modelPath, ontModel);
			loadEveryTimeFiles(ctx, modelPath, ontModel);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create the '" + modelPath
					+ "' model (" + modelUri + ").", e);
		}
	}

	private void loadFirstTimeFiles(ServletContext ctx, String modelPath,
			OntModel baseModel) {
		RDFFilesLoader.loadFirstTimeFiles(ctx, modelPath, baseModel,
				baseModel.isEmpty());
	}

	private void loadEveryTimeFiles(ServletContext ctx, String modelPath,
			OntModel memoryModel) {
		RDFFilesLoader.loadEveryTimeFiles(ctx, modelPath, memoryModel);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Nothing to tear down.
	}

}
