/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sparql;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql.RDFServiceSparql;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.RDFSource;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Create the connection to a SPARQL endpoint.
 * 
 * Depending on the settings, we might use two endpoints: one for reads and one
 * for updates.
 * 
 * Create the RDFServiceFactory.
 */
public class RDFSourceSPARQL implements RDFSource {
	private static final Log log = LogFactory.getLog(RDFSourceSPARQL.class);

	public static final String PROPERTY_SPARQL_ENDPOINT_URI = "VitroConnection.DataSource.endpointURI";
	public static final String PROPERTY_SPARQL_UPDATE_ENDPOINT_URI = "VitroConnection.DataSource.updateEndpointURI";

	private final ServletContextListener parent;
	private final ConfigurationProperties props;
	private final StartupStatus ss;
	private final String endpointURI;
	private final String updateEndpointURI;

	private final RDFService rdfService;
	private final RDFServiceFactory rdfServiceFactory;

	public RDFSourceSPARQL(ServletContext ctx, ServletContextListener parent) {
		this.parent = parent;
		this.props = ConfigurationProperties.getBean(ctx);
		this.ss = StartupStatus.getBean(ctx);

		this.endpointURI = props.getProperty(PROPERTY_SPARQL_ENDPOINT_URI);
		this.updateEndpointURI = props
				.getProperty(PROPERTY_SPARQL_UPDATE_ENDPOINT_URI);

		this.rdfService = createRDFService();
		this.rdfServiceFactory = createRDFServiceFactory();
	}

	private RDFService createRDFService() {
		if (updateEndpointURI == null) {
			ss.info(parent, "Using endpoint at " + endpointURI);
			return new RDFServiceSparql(endpointURI);
		} else {
			ss.info(parent, "Using read endpoint at " + endpointURI
					+ " and update endpoint at " + updateEndpointURI);
			return new RDFServiceSparql(endpointURI, updateEndpointURI);
		}
	}

	private RDFServiceFactory createRDFServiceFactory() {
		return new RDFServiceFactorySingle(this.rdfService);
	}

	@Override
	public RDFServiceFactory getRDFServiceFactory() {
		return this.rdfServiceFactory;
	}

	@Override
	public ModelMakerFactory getContentModelMakerFactory() {
		return new ContentModelMakerFactorySPARQL(this.rdfService);
	}

	@Override
	public ModelMakerFactory getConfigurationModelMakerFactory() {
		return new ConfigurationModelMakerFactorySPARQL(this.rdfService);
	}

	@Override
	public void close() {
		if (this.rdfService != null) {
			this.rdfService.close();
		}
	}

}
