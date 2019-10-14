/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.tdb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ModelMakerWithPersistentEmptyModels;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.rdfDelta.RDFDeltaDatasetFactory;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb.RDFServiceTDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging.LoggingRDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * A TDB triple-store has no concept of connections, so we need not manage them
 * here.
 *
 * As a result, we have a single RDFService, a RDFServiceFactory that always
 * returns that single RDFService, a single instance of the Dataset and the
 * ModelMaker.
 *
 * We keep a copy of the RDFService wrapped in an Unclosable shell, and hand
 * that out when requested. The inner RDFService is only closed on shutdown().
 *
 * Memory-map the small content models, and add the standard decorators.
 */
public class ContentTripleSourceTDB extends ContentTripleSource {
	private String tdbPath;
	
	private String rdfDeltaDatasource;
	
	private boolean emitPatches = false;

	private volatile RDFService rdfService;
	private RDFServiceFactory rdfServiceFactory;
	private RDFService unclosableRdfService;
	private Dataset dataset;
	private ModelMaker modelMaker;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasTdbDirectory", minOccurs = 1, maxOccurs = 1)
	public void setTdbPath(String path) {
		tdbPath = path;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasRDFDeltaDatasource", minOccurs = 0, maxOccurs = 1)
	public void setRDFDeltaDatasource(String deltaDatasource) {
		rdfDeltaDatasource = deltaDatasource;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#shouldEmitPatches", minOccurs = 0, maxOccurs = 1)
	public void setShouldEmitPatches(String emitPatches) {
		this.emitPatches = Boolean.parseBoolean(emitPatches);
	}

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		configureTDB();
		try {
			String tdbPath = resolveTdbPath();
			Dataset dataset = TDBFactory.createDataset(tdbPath);
			if (rdfDeltaDatasource != null) {
				RDFDeltaDatasetFactory rdfDeltaDatasetFactory = application.getRDFDeltaDatasetFactory();
				if (rdfDeltaDatasetFactory != null) {
					dataset = rdfDeltaDatasetFactory.wrap(rdfDeltaDatasource, emitPatches, dataset);
				}
			}
			this.rdfService = new RDFServiceTDB(dataset);
			this.rdfServiceFactory = createRDFServiceFactory();
			this.unclosableRdfService = this.rdfServiceFactory.getRDFService();
			this.dataset = new RDFServiceDataset(this.unclosableRdfService);
			this.modelMaker = createModelMaker();
			checkForFirstTimeStartup();
			ss.info("Initialized the RDF source for TDB");
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to set up the RDF source for TDB", e);
		}
	}

	private void configureTDB() {
		TDB.getContext().setTrue(TDB.symUnionDefaultGraph);
	}

	private String resolveTdbPath() throws IOException {
		Path tdbPath = ApplicationUtils.instance().getHomeDirectory().getPath().resolve(this.tdbPath);
		Files.createDirectories(tdbPath);
		return tdbPath.toString();
	}

	private RDFServiceFactory createRDFServiceFactory() {
		return new LoggingRDFServiceFactory(new RDFServiceFactorySingle(
				this.rdfService));
	}

	private ModelMaker createModelMaker() {
		return addContentDecorators(new ModelMakerWithPersistentEmptyModels(
				new MemoryMappingModelMaker(new RDFServiceModelMaker(
						this.unclosableRdfService), SMALL_CONTENT_MODELS)));
	}

	private void checkForFirstTimeStartup() {
		if (this.dataset.getNamedModel(ModelNames.TBOX_ASSERTIONS).getGraph()
				.isEmpty()) {
			JenaDataSourceSetupBase.thisIsFirstStartup();
		}
	}

	@Override
	public RDFServiceFactory getRDFServiceFactory() {
		return this.rdfServiceFactory;
	}

	@Override
	public RDFService getRDFService() {
		return this.unclosableRdfService;
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
		synchronized (this) {
			if (this.rdfService != null) {
				this.rdfService.close();
				this.rdfService = null;
			}
		}
	}
}
