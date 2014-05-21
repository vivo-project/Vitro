/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelMakerID.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelMakerID.CONTENT;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Sets up the content models, OntModelSelectors and webapp DAO factories.
 */
public class ModelMakerSetup extends JenaDataSourceSetupBase implements
		javax.servlet.ServletContextListener {
	private static final Log log = LogFactory.getLog(ModelMakerSetup.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		createConfigurationModelMaker(ctx);
		createContentModelMaker(ctx);

		ss.info(this, "Created model makers and model source");
	}

	private void createConfigurationModelMaker(ServletContext ctx) {
		String jdbcUrl = getJdbcUrl(ctx);
		String dbtypeStr = ConfigurationProperties.getBean(ctx).getProperty(
				"VitroConnection.DataSource.dbtype", "MySQL");
		String username = ConfigurationProperties.getBean(ctx).getProperty(
				"VitroConnection.DataSource.username");
		String password = ConfigurationProperties.getBean(ctx).getProperty(
				"VitroConnection.DataSource.password");
		VitroJenaModelMaker vjmm = new VitroJenaModelMaker(jdbcUrl, username,
				password, dbtypeStr, ctx);
		ModelAccess.on(ctx).setModelMaker(CONFIGURATION, vjmm);
	}

	private void createContentModelMaker(ServletContext ctx) {
		RDFServiceFactory rdfServiceFactory = RDFServiceUtils
				.getRDFServiceFactory(ctx);
		RDFServiceModelMaker vsmm = new RDFServiceModelMaker(rdfServiceFactory);
		ModelAccess.on(ctx).setModelMaker(CONTENT, vsmm);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do.
	}

}
