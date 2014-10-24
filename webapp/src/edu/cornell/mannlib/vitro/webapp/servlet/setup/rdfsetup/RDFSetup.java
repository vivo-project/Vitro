/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sparql.ContentDataStructuresProviderSPARQL.PROPERTY_SPARQL_ENDPOINT_URI;
import static edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.tdb.ContentDataStructuresProviderTDB.PROPERTY_CONTENT_TDB_PATH;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.BasicDataStructuresProvider;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.ConfigurationDataStructuresProvider;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.ContentDataStructuresProvider;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sdb.ContentDataStructuresProviderSDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sparql.ContentDataStructuresProviderSPARQL;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.tdb.ConfigurationDataStructuresProviderTDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.tdb.ContentDataStructuresProviderTDB;

/**
 * Create the RDFServiceFactories and ModelMakers for the application to use.
 */
public class RDFSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(RDFSetup.class);

	private ServletContext ctx;
	private ConfigurationProperties configProps;

	private ContentDataStructuresProvider contentProvider;
	private ConfigurationDataStructuresProvider configurationProvider;
	private BasicDataStructuresProvider provider;

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		this.ctx = sce.getServletContext();
		this.configProps = ConfigurationProperties.getBean(ctx);

		configureJena();

		createProviders();

		RDFServiceUtils.setRDFServiceFactory(ctx,
				contentProvider.getRDFServiceFactory());
		RDFServiceUtils.setRDFServiceFactory(ctx,
				configurationProvider.getRDFServiceFactory(), CONFIGURATION);

		ModelAccess.setDataStructuresProvider(provider);
	}

	private void configureJena() {
		// we do not want to fetch imports when we wrap Models in OntModels
		OntDocumentManager.getInstance().setProcessImports(false);
	}

	/**
	 * For now, these steps are hard-coded. They should be driven by a
	 * configuration file.
	 */
	private void createProviders() {
		if (isSparqlEndpointContentConfigured()) {
			contentProvider = new ContentDataStructuresProviderSPARQL(ctx, this);
		} else if (isTdbConfigured()) {
			contentProvider = new ContentDataStructuresProviderTDB(ctx, this);
		} else {
			contentProvider = new ContentDataStructuresProviderSDB(ctx, this);
		}

		configurationProvider = new ConfigurationDataStructuresProviderTDB(ctx,
				this);

		provider = new BasicDataStructuresProvider(contentProvider,
				configurationProvider);
	}

	private boolean isSparqlEndpointContentConfigured() {
		return StringUtils.isNotBlank(configProps
				.getProperty(PROPERTY_SPARQL_ENDPOINT_URI));
	}

	private boolean isTdbConfigured() {
		return StringUtils.isNotBlank(configProps
				.getProperty(PROPERTY_CONTENT_TDB_PATH));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (contentProvider != null) {
			try {
				contentProvider.close();
			} catch (Exception e) {
				log.error("Problem when closing content provider", e);
			}
		}
		if (configurationProvider != null) {
			try {
				configurationProvider.close();
			} catch (Exception e) {
				log.error("Problem when closing configuration provider", e);
			}
		}
	}

}
