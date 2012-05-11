/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.util.StoreUtils;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

public class SDBSetup extends JenaDataSourceSetupBase 
implements javax.servlet.ServletContextListener {
    private static final Log log = LogFactory.getLog(SDBSetup.class);

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // nothing to do
        
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {        
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);    
        try {
            setupSDB(ctx, ss);
            log.info("SDB store ready for use");
        } catch (SQLException e) {
            ss.fatal(this, "Exception in setupSDB", e);
        }        
    }

    private void setupSDB(ServletContext ctx, StartupStatus ss) throws SQLException {
        BasicDataSource bds = getApplicationDataSource(ctx);
        if( bds == null ){
            ss.fatal(this, "A DataSource must be setup before SDBSetup "+
                    "is run. Make sure that JenaPersistentDataSourceSetup runs before "+
                    "SDBSetup.");
            return;
        }
        
        // union default graph
        SDB.getContext().set(SDB.unionDefaultGraph, true) ;

        StoreDesc storeDesc = makeStoreDesc(ctx);
        setApplicationStoreDesc(storeDesc, ctx);     
        
        Store store = connectStore(bds, storeDesc);
        setApplicationStore(store, ctx);
        
        if (!isSetUp(store)) {            
            JenaPersistentDataSourceSetup.thisIsFirstStartup();
            setupSDB(ctx, store);
        }else{
            migrateToSDBFromExistingRDBStore(ctx, store); 
        }
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
                    JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL))
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

    public static Store connectStore(BasicDataSource bds, StoreDesc storeDesc)
            throws SQLException {
        SDBConnection conn = new SDBConnection(bds.getConnection());
        return SDBFactory.connectStore(conn, storeDesc);
    }

    protected static void setupSDB(ServletContext ctx, Store store) {
        setupSDB(ctx, store, ModelFactory.createDefaultModel(),
                ModelFactory.createDefaultModel());
    }

    protected static void setupSDB(ServletContext ctx, Store store,
            Model memModel, Model inferenceModel) {
        log.info("Initializing SDB store");
        
        store.getTableFormatter().create();
        store.getTableFormatter().truncate();

        store.getTableFormatter().dropIndexes(); // improve load performance

        try {

            // This is a one-time copy of stored KB data - from a Jena RDB store
            // to a Jena SDB store. In the process, we will also separate out
            // the TBox from the Abox; these are in the same graph in pre-1.2
            // VIVO versions and will now be stored and maintained in separate
            // models. Access to the Jena RDB data is through the
            // OntModelSelectors that have been set up earlier in the current
            // session by JenaPersistentDataSourceSetup.java. In the code
            // below, note that the current getABoxModel() methods on the
            // OntModelSelectors return a graph with both ABox and TBox data.

            OntModel submodels = ModelFactory
                    .createOntologyModel(MEM_ONT_MODEL_SPEC);
            readOntologyFilesInPathSet(SUBMODELS, ctx, submodels);

            Model tboxAssertions = SDBFactory.connectNamedModel(store,
                    JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL);
            // initially putting the results in memory so we have a
            // cheaper way of computing the difference when we copy the ABox
            Model memTboxAssertions = ModelFactory.createDefaultModel();
            getTBoxModel(memModel, submodels, memTboxAssertions);
            tboxAssertions.add(memTboxAssertions);

            Model tboxInferences = SDBFactory.connectNamedModel(store,
                    JenaDataSourceSetupBase.JENA_TBOX_INF_MODEL);
            // initially putting the results in memory so we have a
            // cheaper way of computing the difference when we copy the ABox
            Model memTboxInferences = ModelFactory.createDefaultModel();
            getTBoxModel(inferenceModel, submodels, memTboxInferences);
            tboxInferences.add(memTboxInferences);

            Model aboxAssertions = SDBFactory.connectNamedModel(store,
                    JenaDataSourceSetupBase.JENA_DB_MODEL);
            copyDifference(memModel, memTboxAssertions, aboxAssertions);

            Model aboxInferences = SDBFactory.connectNamedModel(store,
                    JenaDataSourceSetupBase.JENA_INF_MODEL);
            copyDifference(inferenceModel, memTboxInferences, aboxInferences);

            // Set up the application metadata model
            Model applicationMetadataModel = SDBFactory.connectNamedModel(
                    store,
                    JenaDataSourceSetupBase.JENA_APPLICATION_METADATA_MODEL);
            getAppMetadata(memModel, applicationMetadataModel);
            log.info("During initial SDB setup, created an application "
                    + "metadata model of size "
                    + applicationMetadataModel.size());

            // remove application metadata from ABox model
            aboxAssertions.remove(applicationMetadataModel);
            aboxInferences.remove(applicationMetadataModel);

            // Make sure the reasoner takes into account the newly-set-up data.
            SimpleReasonerSetup.setRecomputeRequired(ctx);

        } finally {
            log.info("Adding indexes to SDB database tables.");
            store.getTableFormatter().addIndexes();
            log.info("Indexes created.");
        }
    }

    private void migrateToSDBFromExistingRDBStore(ServletContext ctx,
            Store store) {
        Model rdbAssertionsModel = makeDBModelFromConfigurationProperties(
                JENA_DB_MODEL, DB_ONT_MODEL_SPEC, ctx);
        Model rdbInferencesModel = makeDBModelFromConfigurationProperties(
                JENA_INF_MODEL, DB_ONT_MODEL_SPEC, ctx);
        setupSDB(ctx, store, rdbAssertionsModel, rdbInferencesModel);
    }

    /*
     * Copy all statements from model 1 that are not in model 2 to model 3.
     */
    private static void copyDifference(Model model1, Model model2, Model model3) {

        StmtIterator iter = model1.listStatements();

        while (iter.hasNext()) {
            Statement stmt = iter.next();
            if (!model2.contains(stmt)) {
                model3.add(stmt);
            }
        }

        return;
    }

    private static void getTBoxModel(Model fullModel, Model submodels,
            Model tboxModel) {

        JenaModelUtils modelUtils = new JenaModelUtils();

        Model tempModel = ModelFactory.createUnion(fullModel, submodels);
        Model tempTBoxModel = modelUtils.extractTBox(tempModel);

        // copy intersection of tempTBoxModel and fullModel to tboxModel.
        StmtIterator iter = tempTBoxModel.listStatements();

        while (iter.hasNext()) {
            Statement stmt = iter.next();
            if (fullModel.contains(stmt)) {
                tboxModel.add(stmt);
            }
        }

        return;
    }

}
