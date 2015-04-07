/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.sparql;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ModelMakerWithPersistentEmptyModels;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging.LoggingRDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql.RDFServiceSparql;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * For a SPARQL endpoint, each connection is created as needed by the
 * RDFService, so there is no need to manage connections here.
 * 
 * As a result, we have a single RDFService, a RDFServiceFactory that always
 * returns that single RDFService, a single instance of the Dataset and the
 * ModelMaker.
 * 
 * Memory-map the small content models, and add the standard decorators.
 */
public class ContentTripleSourceSPARQL extends ContentTripleSource {
	private String endpointURI;
	private String updateEndpointURI; // Optional

	private RDFService rdfService;
	private RDFServiceFactory rdfServiceFactory;
	private Dataset dataset;
	private ModelMaker modelMaker;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasEndpointURI")
	public void setEndpointURI(String eUri) {
		if (endpointURI == null) {
			endpointURI = eUri;
		} else {
			throw new IllegalStateException(
					"Configuration includes multiple instances of EndpointURI: "
							+ endpointURI + ", and " + eUri);
		}
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasUpdateEndpointURI")
	public void setUpdateEndpointURI(String ueUri) {
		if (updateEndpointURI == null) {
			updateEndpointURI = ueUri;
		} else {
			throw new IllegalStateException(
					"Configuration includes multiple instances of UpdateEndpointURI: "
							+ updateEndpointURI + ", and " + ueUri);
		}
	}

	@Validation
	public void validate() throws Exception {
		if (endpointURI == null) {
			throw new IllegalStateException(
					"Configuration did not include an EndpointURI.");
		}
	}

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		this.rdfServiceFactory = createRDFServiceFactory(createRDFService(ss,
				endpointURI, updateEndpointURI));
		this.rdfService = this.rdfServiceFactory.getRDFService();
		this.dataset = createDataset();
		this.modelMaker = createModelMaker();
	}

	protected RDFService createRDFService(ComponentStartupStatus ss,
			String endpoint, String updateEndpoint) {
		if (updateEndpoint == null) {
			ss.info("Using endpoint at " + endpoint);
			return new RDFServiceSparql(endpoint);
		} else {
			ss.info("Using read endpoint at " + endpoint
					+ " and update endpoint at " + updateEndpoint);
			return new RDFServiceSparql(endpoint, updateEndpoint);
		}
	}

	private RDFServiceFactory createRDFServiceFactory(RDFService service) {
		return new LoggingRDFServiceFactory(
				new RDFServiceFactorySingle(service));
	}

	private Dataset createDataset() {
		return new RDFServiceDataset(getRDFService());
	}

	private ModelMaker createModelMaker() {
		return addContentDecorators(new ModelMakerWithPersistentEmptyModels(
				new MemoryMappingModelMaker(new RDFServiceModelMaker(
						getRDFService()), SMALL_CONTENT_MODELS)));
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
		return "ContentTripleSourceSPARQL[" + ToString.hashHex(this)
				+ ", endpointURI=" + endpointURI + ", updateEndpointURI="
				+ updateEndpointURI + "]";
	}

	@Override
	public void shutdown(Application application) {
		if (this.rdfService != null) {
			this.rdfService.close();
		}
	}

}
