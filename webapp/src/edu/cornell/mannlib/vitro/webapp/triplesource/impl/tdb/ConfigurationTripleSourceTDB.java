/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.tdb;

import java.io.IOException;
import java.nio.file.Path;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.tdb.TDB;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ListCachingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ConfigurationTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb.RDFServiceTDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging.LoggingRDFServiceFactory;
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
public class ConfigurationTripleSourceTDB extends ConfigurationTripleSource {

	private static final String DIRECTORY_TDB = "tdbModels";

	private RDFServiceFactory rdfServiceFactory;
	private RDFService rdfService;
	private Dataset dataset;
	private ModelMaker modelMaker;

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		configureTDB();

		Path vitroHome = ApplicationUtils.instance().getHomeDirectory()
				.getPath();
		String tdbPath = vitroHome.resolve(DIRECTORY_TDB).toString();

		try {
			this.rdfServiceFactory = createRDFServiceFactory(tdbPath);
			this.rdfService = this.rdfServiceFactory.getRDFService();
			this.dataset = new RDFServiceDataset(this.rdfService);
			this.modelMaker = createModelMaker();
			ss.info("Initialized the RDF source for TDB");
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to set up the RDF source for TDB", e);
		}
	}

	private void configureTDB() {
		TDB.getContext().setTrue(TDB.symUnionDefaultGraph);
	}

	private RDFServiceFactory createRDFServiceFactory(String tdbPath)
			throws IOException {
		return new LoggingRDFServiceFactory(new RDFServiceFactorySingle(
				new RDFServiceTDB(tdbPath)));
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
	public String toString() {
		return "ConfigurationTripleSourceTDB[" + ToString.hashHex(this) + "]";
	}

	@Override
	public void shutdown(Application application) {
		if (this.rdfService != null) {
			this.rdfService.close();
		}
	}

}
