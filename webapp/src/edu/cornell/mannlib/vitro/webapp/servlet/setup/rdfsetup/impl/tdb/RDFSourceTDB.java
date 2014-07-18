/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.tdb;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb.RDFServiceTDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.RDFSource;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Create the connection to the TDB triple-store.
 * 
 * Create the RDFService on the directory. Create the RDFServiceFactory.
 */
public class RDFSourceTDB implements RDFSource {
	private static final Log log = LogFactory.getLog(RDFSourceTDB.class);

	private static final String DIRECTORY_TDB = "tdbModels";

	private final ConfigurationProperties props;
	private final StartupStatus ss;

	private final RDFService rdfService;
	private final RDFServiceFactory rdfServiceFactory;

	public RDFSourceTDB(ServletContext ctx, ServletContextListener parent) {
		this.props = ConfigurationProperties.getBean(ctx);
		this.ss = StartupStatus.getBean(ctx);

		try {
			this.rdfService = createRdfService();
			this.rdfServiceFactory = createRDFServiceFactory();
			ss.info(parent, "Initialized the RDF source for TDB");
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to set up the RDF source for TDB", e);
		}
	}

	private RDFService createRdfService() throws IOException {
		String vitroHome = props.getProperty("vitro.home");
		String directoryPath = vitroHome + File.separatorChar + DIRECTORY_TDB;
		return new RDFServiceTDB(directoryPath);
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
		return new ContentModelMakerFactoryTDB(this.rdfService);
	}

	@Override
	public ModelMakerFactory getConfigurationModelMakerFactory() {
		return new ConfigurationModelMakerFactoryTDB(this.rdfService);
	}

	@Override
	public void close() {
		if (this.rdfService != null) {
			this.rdfService.close();
		}
	}

}
