/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup;

import static edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sparql.RDFSourceSPARQL.PROPERTY_SPARQL_ENDPOINT_URI;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.ontology.OntDocumentManager;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelMakerID;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelMakerUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sdb.RDFSourceSDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sparql.RDFSourceSPARQL;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.tdb.RDFSourceTDB;

/**
 * Create the RDFServiceFactories and ModelMakers for the application to use.
 */
public class RDFSetup implements ServletContextListener {
	private ServletContext ctx;
	private ConfigurationProperties configProps;

	private RDFSource contentRdfSource;
	private RDFSource configurationRdfSource;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		this.ctx = sce.getServletContext();
		this.configProps = ConfigurationProperties.getBean(ctx);

		configureJena();

		createRdfSources();

		RDFServiceUtils.setRDFServiceFactory(ctx,
				contentRdfSource.getRDFServiceFactory(), WhichService.CONTENT);
		ModelMakerUtils.setContentModelMakerFactory(ctx,
				contentRdfSource.getContentModelMakerFactory());
		ModelAccess.on(ctx).setModelMaker(ModelMakerID.CONTENT,
				ModelMakerUtils.getModelMaker(ctx, WhichService.CONTENT));

		RDFServiceUtils.setRDFServiceFactory(ctx,
				configurationRdfSource.getRDFServiceFactory(),
				WhichService.CONFIGURATION);
		ModelMakerUtils.setConfigurationModelMakerFactory(ctx,
				configurationRdfSource.getConfigurationModelMakerFactory());
		ModelAccess.on(ctx).setModelMaker(ModelMakerID.CONFIGURATION,
				ModelMakerUtils.getModelMaker(ctx, WhichService.CONFIGURATION));

	}

	private void configureJena() {
		// we do not want to fetch imports when we wrap Models in OntModels
		OntDocumentManager.getInstance().setProcessImports(false);
	}

	/**
	 * For now, these steps are hard-coded. They should be driven by a
	 * configuration file.
	 */
	private void createRdfSources() {
		if (isSparqlEndpointContentConfigured()) {
			contentRdfSource = new RDFSourceSPARQL(ctx, this);
		} else {
			contentRdfSource = new RDFSourceSDB(ctx, this);
		}
		configurationRdfSource = new RDFSourceTDB(ctx, this);
	}

	private boolean isSparqlEndpointContentConfigured() {
		return StringUtils.isNotBlank(configProps
				.getProperty(PROPERTY_SPARQL_ENDPOINT_URI));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (configurationRdfSource != null) {
			configurationRdfSource.close();
		}
		if (contentRdfSource != null) {
			contentRdfSource.close();
		}
	}

}
