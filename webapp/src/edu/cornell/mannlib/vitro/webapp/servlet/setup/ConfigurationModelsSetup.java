/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService.CONFIGURATION;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
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
			setupModel(ctx, ModelNames.DISPLAY, "display",
					ModelID.DISPLAY);

			setupModel(ctx, ModelNames.DISPLAY_TBOX, "displayTbox",
					ModelID.DISPLAY_TBOX);

			setupModel(ctx, ModelNames.DISPLAY_DISPLAY, "displayDisplay",
					ModelID.DISPLAY_DISPLAY);

			ss.info(this, "Set up the display models.");

			setupModel(ctx, ModelNames.USER_ACCOUNTS, "auth",
					ModelID.USER_ACCOUNTS);

			ss.info(this, "Set up the user accounts model.");
		} catch (Exception e) {
			ss.fatal(this, e.getMessage(), e.getCause());
		}
	}

	private void setupModel(ServletContext ctx, String modelUri,
			String modelPath, ModelID modelId) {
		try {
			Dataset dataset = getConfigurationModelsDataset(ctx);
			OntModel baseModel = getNamedOntModel(modelUri, dataset);
			
			loadFirstTimeFiles(ctx, modelPath, baseModel);
			loadEveryTimeFiles(ctx, modelPath, baseModel);

			OntModel memoryModel = wrapWithMemoryModel(baseModel);
			ModelAccess.on(ctx).setOntModel(modelId, memoryModel);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create the '" + modelPath
					+ "' model (" + modelUri + ").", e);
		}
	}

	private Dataset getConfigurationModelsDataset(ServletContext ctx) {
		RDFServiceFactory factory = RDFServiceUtils.getRDFServiceFactory(ctx,
				CONFIGURATION);
		return new RDFServiceDataset(factory.getRDFService());
	}

	private OntModel getNamedOntModel(String modelUri, Dataset dataset) {
		Model model = dataset.getNamedModel(modelUri);
		return VitroModelFactory.createOntologyModel(model);
	}

	private void loadFirstTimeFiles(ServletContext ctx, String modelPath,
			OntModel baseModel) {
		RDFFilesLoader.loadFirstTimeFiles(ctx, modelPath, baseModel,
				baseModel.isEmpty());
	}

	private OntModel wrapWithMemoryModel(OntModel baseModel) {
		OntModel memoryModel = VitroModelFactory.createOntologyModel();
		memoryModel.add(baseModel);
		memoryModel.getBaseModel().register(new ModelSynchronizer(baseModel));
		return memoryModel;
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
