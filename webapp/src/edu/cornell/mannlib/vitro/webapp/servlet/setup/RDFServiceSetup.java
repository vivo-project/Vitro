/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils.WhichService.CONFIGURATION;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
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

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceFactorySingle;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.sdb.RDFServiceFactorySDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.tdb.RDFServiceFactoryTDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql.RDFServiceSparql;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

public class RDFServiceSetup extends JenaDataSourceSetupBase 
implements javax.servlet.ServletContextListener {
	private static final Log log = LogFactory.getLog(RDFServiceSetup.class);
	private static final String DIRECTORY_TDB = "tdbModels";

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // nothing to do   
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {    
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);    
        try {
            String endpointURI = ConfigurationProperties.getBean(sce).getProperty(
                    "VitroConnection.DataSource.endpointURI");
            String updateEndpointURI = ConfigurationProperties.getBean(sce).getProperty(
                    "VitroConnection.DataSource.updateEndpointURI");
            if (endpointURI != null) {
                useEndpoint(endpointURI, updateEndpointURI, ctx);
            } else {
                useSDB(ctx, ss);
            }
            
            //experimental
            //RDFServiceFactory factory = RDFServiceUtils.getRDFServiceFactory(ctx);
            //RDFServiceUtils.setRDFServiceFactory(ctx, new SameAsFilteringRDFServiceFactory(factory));
            
            useTDBForConfigurationModels(ctx);
            
        } catch (Exception e) {
            ss.fatal(this, "Exception in RDFServiceSetup", e);
        }        
    }
    
	private void useTDBForConfigurationModels(ServletContext ctx) throws IOException {
        String vitroHome = ConfigurationProperties.getBean(ctx).getProperty(
                "vitro.home") ;
        String directoryPath = vitroHome + File.separatorChar + DIRECTORY_TDB;
		RDFServiceFactory factory = new RDFServiceFactoryTDB(directoryPath);
        RDFServiceUtils.setRDFServiceFactory(ctx, factory, CONFIGURATION);
	}

	private void useEndpoint(String endpointURI, String updateEndpointURI, ServletContext ctx) {
    	
    	RDFService rdfService = null;
    	if (updateEndpointURI == null)  {
    		rdfService = new RDFServiceSparql(endpointURI);
    	} else {
    		rdfService = new RDFServiceSparql(endpointURI, updateEndpointURI);
    	}
    	
        RDFServiceFactory rdfServiceFactory = new RDFServiceFactorySingle(rdfService);
        RDFServiceUtils.setRDFServiceFactory(ctx, rdfServiceFactory);
        
        if (updateEndpointURI != null) {
            log.info("Using read endpoint at " + endpointURI);
            log.info("Using update endpoint at " + updateEndpointURI);
        } else {
            log.info("Using endpoint at " + endpointURI);
        }
    }

    private void useSDB(ServletContext ctx, StartupStatus ss) throws SQLException {
        DataSource ds = JenaPersistentDataSourceSetup.getApplicationDataSource();
        if( ds == null ){
            ss.fatal(this, "A DataSource must be setup before SDBSetup "+
                    "is run. Make sure that JenaPersistentDataSourceSetup runs before "+
                    "SDBSetup.");
            return;
        }
        
        // union default graph
        SDB.getContext().set(SDB.unionDefaultGraph, true) ;

        StoreDesc storeDesc = makeStoreDesc(ctx);
        Store store = connectStore(ds, storeDesc);
        
        if (!isSetUp(store)) {            
            JenaDataSourceSetupBase.thisIsFirstStartup();
            setupSDB(ctx, store);
        }
        
        //RDFService rdfService = new RDFServiceSDB(ds, storeDesc);
        //RDFServiceFactory rdfServiceFactory = new RDFServiceFactorySingle(rdfService);
        
        RDFServiceFactory rdfServiceFactory = new RDFServiceFactorySDB(ds, storeDesc);
        RDFServiceUtils.setRDFServiceFactory(ctx, rdfServiceFactory);
        
        log.info("SDB store ready for use");
        
    }
    
    
    /**
     * Tests whether an SDB store has been formatted and populated for use.
     * @param store
     * @return
     */
    private boolean isSetUp(Store store) throws SQLException {
        if (!(StoreUtils.isFormatted(store))) {
            return false;
        }
        
        // even if the store exists, it may be empty
        
        try {
            return (SDBFactory.connectNamedModel(
                    store, 
                    ModelNames.TBOX_ASSERTIONS))
                            .size() > 0;    
        } catch (Exception e) { 
            return false;
        }
    }
    
    public static StoreDesc makeStoreDesc(ServletContext ctx) {
        String layoutStr = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.sdb.layout", "layout2/hash");
        String dbtypeStr = ConfigurationProperties.getBean(ctx).getProperty(
                "VitroConnection.DataSource.dbtype", "MySQL");
       return new StoreDesc(
                LayoutType.fetch(layoutStr),
                DatabaseType.fetch(dbtypeStr) );
    }

    public static Store connectStore(DataSource bds, StoreDesc storeDesc)
            throws SQLException {
        SDBConnection conn = new SDBConnection(bds.getConnection());
        return SDBFactory.connectStore(conn, storeDesc);
    }

    protected static void setupSDB(ServletContext ctx, Store store) {
        log.info("Initializing SDB store");
        store.getTableFormatter().create();
        store.getTableFormatter().truncate();
    }

}
