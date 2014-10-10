/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sparql;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ListCachingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql.RDFServiceSparql;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.ContentDataStructuresProvider;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
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
public class ContentDataStructuresProviderSPARQL extends
		ContentDataStructuresProvider {
	public static final String PROPERTY_SPARQL_ENDPOINT_URI = "VitroConnection.DataSource.endpointURI";
	public static final String PROPERTY_SPARQL_UPDATE_ENDPOINT_URI = "VitroConnection.DataSource.updateEndpointURI";

	private final ServletContextListener ctxListener;
	private final ConfigurationProperties props;
	private final StartupStatus ss;
	private final String endpointURI;
	private final String updateEndpointURI;

	private final RDFService rdfService;
	private final RDFServiceFactory rdfServiceFactory;
	private final Dataset dataset;
	private final ModelMaker modelMaker;

	public ContentDataStructuresProviderSPARQL(ServletContext ctx,
			ServletContextListener ctxListener) {
		this.ctxListener = ctxListener;
		this.props = ConfigurationProperties.getBean(ctx);
		this.ss = StartupStatus.getBean(ctx);

		this.endpointURI = props.getProperty(PROPERTY_SPARQL_ENDPOINT_URI);
		this.updateEndpointURI = props
				.getProperty(PROPERTY_SPARQL_UPDATE_ENDPOINT_URI);

		this.rdfService = createRDFService();
		this.rdfServiceFactory = createRDFServiceFactory();
		this.dataset = createDataset();
		this.modelMaker = createModelMaker();
	}

	private RDFService createRDFService() {
		if (updateEndpointURI == null) {
			ss.info(ctxListener, "Using endpoint at " + endpointURI);
			return new RDFServiceSparql(endpointURI);
		} else {
			ss.info(ctxListener, "Using read endpoint at " + endpointURI
					+ " and update endpoint at " + updateEndpointURI);
			return new RDFServiceSparql(endpointURI, updateEndpointURI);
		}
	}

	private RDFServiceFactory createRDFServiceFactory() {
		return new RDFServiceFactorySingle(this.rdfService);
	}

	private Dataset createDataset() {
		return new RDFServiceDataset(getRDFService());
	}

	private ModelMaker createModelMaker() {
		return addContentDecorators(new ListCachingModelMaker(
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
	public void close() {
		if (this.rdfService != null) {
			this.rdfService.close();
		}
	}

	@Override
	public String toString() {
		return "ContentDataStructuresProviderSPARQL[" + ToString.hashHex(this)
				+ ", endpointURI=" + endpointURI + ", updateEndpointURI="
				+ updateEndpointURI + "]";
	}

}
