/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.tdb;

import java.io.IOException;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.tdb.TDB;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ListCachingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb.RDFServiceTDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging.LoggingRDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * A TDB triple-store has no concept of connections, so we need not manage them
 * here.
 * 
 * As a result, we have a single RDFService, a RDFServiceFactory that always
 * returns that single RDFService, a single instance of the Dataset and the
 * ModelMaker.
 * 
 * Memory-map the small content models, and add the standard decorators.
 */
public class ContentTripleSourceTDB extends ContentTripleSource {
	private String tdbPath;

	private RDFServiceFactory rdfServiceFactory;
	private RDFService rdfService;
	private Dataset dataset;
	private ModelMaker modelMaker;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTdbDirectory")
	public void setTdbPath(String path) {
		if (tdbPath == null) {
			tdbPath = path;
		} else {
			throw new IllegalStateException(
					"Configuration includes multiple instances of TdbDirectory: "
							+ tdbPath + ", and " + path);
		}
	}

	@Validation
	public void validate() throws Exception {
		if (tdbPath == null) {
			throw new IllegalStateException(
					"Configuration did not include a TdbDirectory.");
		}
	}

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		configureTDB();
		try {
			this.rdfService = new RDFServiceTDB(tdbPath);
			this.rdfServiceFactory = createRDFServiceFactory();
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

	private RDFServiceFactory createRDFServiceFactory() {
		return new LoggingRDFServiceFactory(new RDFServiceFactorySingle(
				this.rdfService));
	}

	private ModelMaker createModelMaker() {
		return addContentDecorators(new ListCachingModelMaker(
				new MemoryMappingModelMaker(new RDFServiceModelMaker(
						this.rdfService), SMALL_CONTENT_MODELS)));
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
		return "ContentTripleSourceTDB[" + ToString.hashHex(this) + "]";
	}

	@Override
	public void shutdown(Application application) {
		if (this.rdfService != null) {
			this.rdfService.close();
		}
	}

}
