/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.impl.sdb;

import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.util.StoreUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelMakerFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.sdb.RDFServiceFactorySDB;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.rdfsetup.RDFSource;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Create the connection to the SDB triple-store.
 * 
 * Do some smoke-tests on the parameters, create the connection pool, and create
 * the RDFServiceFactory.
 * 
 * Create the ModelMakerFactories only if requested.
 */
public class RDFSourceSDB implements RDFSource {
	private static final Log log = LogFactory.getLog(RDFSourceSDB.class);

	static final String PROPERTY_DB_URL = "VitroConnection.DataSource.url";
	static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
	static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";
	static final String PROPERTY_DB_DRIVER_CLASS_NAME = "VitroConnection.DataSource.driver";
	static final String PROPERTY_DB_TYPE = "VitroConnection.DataSource.dbtype";
	static final String PROPERTY_DB_MAX_ACTIVE = "VitroConnection.DataSource.pool.maxActive";
	static final String PROPERTY_DB_MAX_IDLE = "VitroConnection.DataSource.pool.maxIdle";
	static final String PROPERTY_DB_VALIDATION_QUERY = "VitroConnection.DataSource.validationQuery";
	static final String PROPERTY_DB_SDB_LAYOUT = "VitroConnection.DataSource.sdb.layout";

	static final String DEFAULT_TYPE = "MySQL";
	static final String DEFAULT_DRIVER_CLASS = "com.mysql.jdbc.Driver";
	static final String DEFAULT_LAYOUT = "layout2/hash";
	static final String DEFAULT_VALIDATION_QUERY = "SELECT 1";

	static final int DEFAULT_MAXACTIVE = 40; // ms
	static final int MINIMUM_MAXACTIVE = 20; // ms
	static final int DEFAULT_MAXIDLE = 10; // ms

	static final boolean DEFAULT_TESTONBORROW = true;
	static final boolean DEFAULT_TESTONRETURN = true;

	private final ServletContext ctx;
	private final StartupStatus ss;
	private final ComboPooledDataSource ds;
	private final RDFServiceFactory rdfServiceFactory;
	private final RDFService rdfService;

	public RDFSourceSDB(ServletContext ctx, ServletContextListener parent) {
		try {
			this.ctx = ctx;
			this.ss = StartupStatus.getBean(ctx);

			configureSDBContext();

			new SDBConnectionSmokeTests(ctx, parent)
					.checkDatabaseConnection();

			this.ds = new SDBDataSource(ctx).getDataSource();
			this.rdfServiceFactory = createRdfServiceFactory();
			this.rdfService = rdfServiceFactory.getRDFService();
			ss.info(parent, "Initialized the RDF source for SDB");
		} catch (SQLException e) {
			throw new RuntimeException(
					"Failed to set up the RDF source for SDB", e);
		}
	}

	private void configureSDBContext() {
		SDB.getContext().set(SDB.unionDefaultGraph, true);
	}

	private RDFServiceFactory createRdfServiceFactory() throws SQLException {
		StoreDesc storeDesc = makeStoreDesc();
		Store store = connectStore(ds, storeDesc);

		if (!isSetUp(store)) {
			JenaDataSourceSetupBase.thisIsFirstStartup();
			setupSDB(store);
		}

		return new RDFServiceFactorySDB(ds, storeDesc);
	}
	
	/**
	 * Tests whether an SDB store has been formatted and populated for use.
	 * 
	 * @param store
	 * @return
	 */
	private boolean isSetUp(Store store) throws SQLException {
		if (!(StoreUtils.isFormatted(store))) {
			return false;
		}

		// even if the store exists, it may be empty

		try {
			return (SDBFactory.connectNamedModel(store,
					ModelNames.TBOX_ASSERTIONS)).size() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	private StoreDesc makeStoreDesc() {
		ConfigurationProperties props = ConfigurationProperties.getBean(ctx);
		String layoutStr = props.getProperty(PROPERTY_DB_SDB_LAYOUT,
				DEFAULT_LAYOUT);
		String dbtypeStr = props.getProperty(PROPERTY_DB_TYPE, DEFAULT_TYPE);
		return new StoreDesc(LayoutType.fetch(layoutStr),
				DatabaseType.fetch(dbtypeStr));
	}

	private Store connectStore(DataSource bds, StoreDesc storeDesc)
			throws SQLException {
		SDBConnection conn = new SDBConnection(bds.getConnection());
		return SDBFactory.connectStore(conn, storeDesc);
	}

	private void setupSDB(Store store) {
		log.info("Initializing SDB store");
		store.getTableFormatter().create();
		store.getTableFormatter().truncate();
	}

	@Override
	public RDFServiceFactory getRDFServiceFactory() {
		return this.rdfServiceFactory;
	}

	@Override
	public ModelMakerFactory getContentModelMakerFactory() {
		return new ContentModelMakerFactorySDB(this.rdfService);
	}

	@Override
	public ModelMakerFactory getConfigurationModelMakerFactory() {
		return new ConfigurationModelMakerFactorySDB(this.rdfService);
	}

	@Override
	public void close() {
		if (ds != null) {
			ds.close();
		}
	}

}
