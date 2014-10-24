/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.tdb;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.tdb.TDB;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ListCachingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb.RDFServiceTDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.ConfigurationDataStructuresProvider;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * A TDB triple-store has no concept of connections, so we need not manage them
 * here.
 * 
 * As a result, we have a single RDFService, a RDFServiceFactory that always
 * returns that single RDFService, a single instance of the Dataset and the
 * ModelMaker.
 * 
 * Memory-map all of the configuration models, and add the standard decorators.
 */
public class ConfigurationDataStructuresProviderTDB extends
		ConfigurationDataStructuresProvider {

	private static final String DIRECTORY_TDB = "tdbModels";

	private final ConfigurationProperties props;
	private final StartupStatus ss;

	private final RDFServiceFactory rdfServiceFactory;
	private final RDFService rdfService;
	private final Dataset dataset;
	private final ModelMaker modelMaker;

	public ConfigurationDataStructuresProviderTDB(ServletContext ctx,
			ServletContextListener ctxListener) {
		this.props = ConfigurationProperties.getBean(ctx);
		this.ss = StartupStatus.getBean(ctx);

		configureTDB();

		String tdbPath = props.getProperty("vitro.home") + File.separatorChar
				+ DIRECTORY_TDB;

		try {
			this.rdfService = new RDFServiceTDB(tdbPath);
			this.rdfServiceFactory = createRDFServiceFactory();
			this.dataset = new RDFServiceDataset(this.rdfService);
			this.modelMaker = createModelMaker();
			ss.info(ctxListener, "Initialized the RDF source for TDB");
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to set up the RDF source for TDB", e);
		}
	}

	private void configureTDB() {
		TDB.getContext().setTrue(TDB.symUnionDefaultGraph);
	}

	private RDFServiceFactory createRDFServiceFactory() {
		return new RDFServiceFactorySingle(this.rdfService);
	}

	private ModelMaker createModelMaker() {
		ModelMaker longTermModelMaker = new ListCachingModelMaker(
				new MemoryMappingModelMaker(new RDFServiceModelMaker(
						this.rdfService), CONFIGURATION_MODELS));
		return addConfigurationDecorators(longTermModelMaker);
	}

	@Override
	public RDFServiceFactory getRDFServiceFactory() {
		return this.rdfServiceFactory;
	}

	@Override
	public RDFService getRDFService() {
		return this.rdfService;
	}

	@Override
	public Dataset getDataset() {
		return this.dataset;
	}

	@Override
	public ModelMaker getModelMaker() {
		return this.modelMaker;
	}

	@Override
	public OntModelCache getShortTermOntModels(RDFService shortTermRdfService,
			OntModelCache longTermOntModelCache) {
		// No need to use short-term models.
		return longTermOntModelCache;
	}

	@Override
	public void close() {
		if (this.rdfService != null) {
			this.rdfService.close();
		}
	}

	@Override
	public String toString() {
		return "ConfigurationDataStructuresProviderTDB["
				+ ToString.hashHex(this) + "]";
	}
}
