/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.tdb;

import static edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService.CONFIGURATION;
import static edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService.CONTENT;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.tdb.TDB;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb.RDFServiceTDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.RDFSource;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Create the connection to the TDB triple-store. This connection is either for
 * CONTENT or for CONFIGURATION, but not both.
 * 
 * Create the RDFService on the directory. Create the RDFServiceFactory.
 */
public class RDFSourceTDB implements RDFSource {
	private static final Log log = LogFactory.getLog(RDFSourceTDB.class);

	private static final String DIRECTORY_TDB = "tdbModels";
	public static final String PROPERTY_CONTENT_TDB_PATH = "VitroConnection.DataSource.tdbDirectory";

	private final ConfigurationProperties props;
	private final StartupStatus ss;

	private final WhichService which;
	private final RDFService rdfService;
	private final RDFServiceFactory rdfServiceFactory;

	public RDFSourceTDB(ServletContext ctx, ServletContextListener parent,
			WhichService which) {
		this.props = ConfigurationProperties.getBean(ctx);
		this.ss = StartupStatus.getBean(ctx);
		this.which = which;
		
		configureTDB();

		String tdbPath;
		if (CONTENT == which) {
			tdbPath = props.getProperty(PROPERTY_CONTENT_TDB_PATH);
		} else {
			String vitroHome = props.getProperty("vitro.home");
			tdbPath = vitroHome + File.separatorChar + DIRECTORY_TDB;
		}

		try {
			this.rdfService = new RDFServiceTDB(tdbPath);
			this.rdfServiceFactory = createRDFServiceFactory();
			ss.info(parent, "Initialized the RDF source for TDB");
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to set up the RDF source for TDB", e);
		}
	}

	private void configureTDB() {
		TDB.getContext().setTrue(TDB.symUnionDefaultGraph);
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
		if (CONTENT == which) {
			return new ContentModelMakerFactoryTDB(this.rdfService);
		} else {
			throw new IllegalStateException("This RDFSource is for " + which);
		}
	}

	@Override
	public ModelMakerFactory getConfigurationModelMakerFactory() {
		if (CONFIGURATION == which) {
			return new ConfigurationModelMakerFactoryTDB(this.rdfService);
		} else {
			throw new IllegalStateException("This RDFSource is for " + which);
		}
	}

	@Override
	public void close() {
		if (this.rdfService != null) {
			this.rdfService.close();
		}
	}

}
