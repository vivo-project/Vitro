/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.sdb;

import static edu.cornell.mannlib.vitro.webapp.triplesource.impl.BasicCombinedTripleSource.CONTENT_UNIONS;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelMaker;
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
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.MemoryMappingModelMaker;
import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.ModelMakerWithPersistentEmptyModels;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.MaskingOntModelCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.ModelMakerOntModelCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.OntModelCache;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels.UnionModelsOntModelsCache;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tripleSource.ContentTripleSource;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.sdb.RDFServiceFactorySDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging.LoggingRDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * Create the connection to the SDB triple-store.
 * 
 * Do some smoke-tests on the parameters, create the connection pool, and create
 * the RDFServiceFactory.
 */
public class ContentTripleSourceSDB extends ContentTripleSource {
	private static final Log log = LogFactory
			.getLog(ContentTripleSourceSDB.class);

	static final String PROPERTY_DB_URL = "VitroConnection.DataSource.url";
	static final String PROPERTY_DB_USERNAME = "VitroConnection.DataSource.username";
	static final String PROPERTY_DB_PASSWORD = "VitroConnection.DataSource.password";
	static final String PROPERTY_DB_DRIVER_CLASS_NAME = "VitroConnection.DataSource.driver";
	static final String PROPERTY_DB_TYPE = "VitroConnection.DataSource.dbtype";
	static final String PROPERTY_DB_MAX_ACTIVE = "VitroConnection.DataSource.pool.maxActive";
	static final String PROPERTY_DB_MAX_IDLE = "VitroConnection.DataSource.pool.maxIdle";
	static final String PROPERTY_DB_MAX_IDLE_TIME = "VitroConnection.DataSource.pool.maxIdleTime";
	static final String PROPERTY_DB_MAX_IDLE_TIME_EXCESS = "VitroConnection.DataSource.pool.maxIdleTimeExcess";
	static final String PROPERTY_DB_VALIDATION_QUERY = "VitroConnection.DataSource.validationQuery";
	static final String PROPERTY_DB_SDB_LAYOUT = "VitroConnection.DataSource.sdb.layout";

	static final String DEFAULT_TYPE = "MySQL";
	static final String DEFAULT_DRIVER_CLASS = "com.mysql.jdbc.Driver";
	static final String DEFAULT_LAYOUT = "layout2/hash";
	static final String DEFAULT_VALIDATION_QUERY = "SELECT 1";

	static final int DEFAULT_MAXACTIVE = 40; // ms
	static final int MINIMUM_MAXACTIVE = 20; // ms
	static final int DEFAULT_MAXIDLE = 10; // ms

	static final int DEFAULT_MAX_IDLE_TIME = 1800; // seconds
	static final int DEFAULT_MAX_IDLE_TIME_EXCESS = 300; // seconds

	static final boolean DEFAULT_TESTONBORROW = true;
	static final boolean DEFAULT_TESTONRETURN = true;

	private ServletContext ctx;
	private ComboPooledDataSource ds;
	private RDFServiceFactory rdfServiceFactory;
	private RDFService rdfService;
	private Dataset dataset;
	private ModelMaker modelMaker;

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		try {
			this.ctx = application.getServletContext();

			configureSDBContext();

			new SDBConnectionSmokeTests(ctx, ss).checkDatabaseConnection();

			this.ds = new SDBDataSource(ctx).getDataSource();
			this.rdfServiceFactory = createRdfServiceFactory();
			this.rdfService = rdfServiceFactory.getRDFService();
			this.dataset = new RDFServiceDataset(this.rdfService);
			this.modelMaker = createModelMaker();
			ss.info("Initialized the content data structures for SDB");
		} catch (SQLException e) {
			throw new RuntimeException(
					"Failed to set up the content data structures for SDB", e);
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

		return new LoggingRDFServiceFactory(new RDFServiceFactorySDB(ds,
				storeDesc));
	}

	/**
	 * Tests whether an SDB store has been formatted and populated for use.
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

	private ModelMaker createModelMaker() {
		return addContentDecorators(new ModelMakerWithPersistentEmptyModels(
				new MemoryMappingModelMaker(new RDFServiceModelMaker(
						this.rdfService), SMALL_CONTENT_MODELS)));
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

	/**
	 * Use models from the short-term RDFService, since there is less contention
	 * for the database connections that way. The exceptions are the
	 * memory-mapped models. By keeping them, we also keep their sub-models.
	 * 
	 * Set up the Union models again also, so they will reference the short-term
	 * models.
	 */
	@Override
	public OntModelCache getShortTermOntModels(RDFService shortTermRdfService,
			OntModelCache longTermOntModelCache) {
		ModelMakerOntModelCache shortCache = new ModelMakerOntModelCache(
				addContentDecorators(new ModelMakerWithPersistentEmptyModels(
						new RDFServiceModelMaker(shortTermRdfService))));

		MaskingOntModelCache combinedCache = new MaskingOntModelCache(
				shortCache, longTermOntModelCache,
				Arrays.asList(MEMORY_MAPPED_CONTENT_MODELS));

		return new UnionModelsOntModelsCache(combinedCache, CONTENT_UNIONS);
	}

	@Override
	public String toString() {
		return "ContentTripleSourceSDB[" + ToString.hashHex(this) + "]";
	}

	@Override
	public void shutdown(Application application) {
		if (this.modelMaker != null) {
			this.modelMaker.close();
		}
		if (this.dataset != null) {
			this.dataset.close();
		}
		if (this.rdfService != null) {
			this.rdfService.close();
		}
		if (ds != null) {
			String driverClassName = ds.getDriverClass();
			ds.close();
			attemptToDeregisterJdbcDriver(driverClassName);
			cleanupAbandonedConnectionThread(driverClassName);
		}
	}

	private void attemptToDeregisterJdbcDriver(String driverClassName) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		for (Enumeration<Driver> drivers = DriverManager.getDrivers(); drivers
				.hasMoreElements();) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				// This driver was registered by the webapp's ClassLoader, so
				// deregister it:
				try {
					DriverManager.deregisterDriver(driver);
				} catch (SQLException ex) {
					log.error("Error deregistering JDBC driver {" + driver
							+ "}", ex);
				}
			} else {
				// driver was not registered by the webapp's ClassLoader and may
				// be in use elsewhere
			}
		}
	}

	/**
	 * The MySQL driver leaves a thread running after it is deregistered.
	 * Versions after 5.1.23 provide AbandonedConnectionCleanupThread.shutdown()
	 * to stop this thread.
	 * 
	 * Using reflection to invoke this method means that we don't have a
	 * hard-coded dependency to MySQL.
	 */
	private void cleanupAbandonedConnectionThread(String driverClassName) {
		if (!driverClassName.contains("mysql")) {
			return;
		}
		try {
			Class.forName("com.mysql.jdbc.AbandonedConnectionCleanupThread")
					.getMethod("shutdown").invoke(null);
		} catch (Exception e) {
			log.info("Failed to shutdown MySQL connection cleanup thread: " + e);
		}
	}
}
