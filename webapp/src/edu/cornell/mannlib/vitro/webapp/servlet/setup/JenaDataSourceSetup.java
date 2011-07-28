/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
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
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoCon;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.ontology.update.KnowledgeBaseUpdater;
import edu.cornell.mannlib.vitro.webapp.utils.jena.InitialJenaModelUtils;

public class JenaDataSourceSetup extends JenaDataSourceSetupBase implements javax.servlet.ServletContextListener {
    
    private static final Log log = LogFactory.getLog(JenaDataSourceSetup.class);
    
    @Override
	public void contextInitialized(ServletContextEvent sce) {
    	
    	ServletContext ctx = sce.getServletContext();
        
		if (AbortStartup.isStartupAborted(ctx)) {
            return;
        }
        
        try {
        	long startTime = System.currentTimeMillis();
        	setUpJenaDataSource(ctx);
        	log.info((System.currentTimeMillis() - startTime) / 1000 + 
        			" seconds to set up SDB store");
        } catch (SQLException sqle) {   
            // SQL exceptions are fatal and should halt startup
            AbortStartup.abortStartup(ctx);
            log.error("Error using SQL database; startup aborted.", sqle);
            // print to catalina.out for good measure
            System.out.println("Error using SQL database; startup aborted.");
            sqle.printStackTrace();
            throw new Error(this.getClass().getName() + "failed");
        } catch (Throwable t) {
            log.error("Throwable in " + this.getClass().getName(), t);
            // printing the error because Tomcat doesn't print context listener
            // errors the same way it prints other errors at startup
            t.printStackTrace();
            throw new Error(this.getClass().getName() + "failed");
        }
        
    } 

    private void setUpJenaDataSource(ServletContext ctx) throws SQLException {
                   	
        OntModelSelectorImpl baseOms = new OntModelSelectorImpl();     
        OntModelSelectorImpl inferenceOms = new OntModelSelectorImpl();       
        OntModelSelectorImpl unionOms = new OntModelSelectorImpl();
        
        OntModel userAccountsModel = ontModelFromContextAttribute(
        		ctx, "userAccountsOntModel");     
        baseOms.setUserAccountsModel(userAccountsModel);
        inferenceOms.setUserAccountsModel(userAccountsModel);
        unionOms.setUserAccountsModel(userAccountsModel);       
        
        OntModel displayModel = ontModelFromContextAttribute(
        		ctx,"displayOntModel");
        baseOms.setDisplayModel(displayModel);
        inferenceOms.setDisplayModel(displayModel);
        unionOms.setDisplayModel(displayModel);
                
        // SDB setup
        
        // union default graph
        SDB.getContext().set(SDB.unionDefaultGraph, true) ;

        StoreDesc storeDesc = makeStoreDesc(ctx);
        setApplicationStoreDesc(storeDesc, ctx);

        BasicDataSource bds = makeDataSourceFromConfigurationProperties(ctx);
        this.setApplicationDataSource(bds, ctx);
        
        Store store = connectStore(bds, storeDesc);
        setApplicationStore(store, ctx);
        
        if (!isSetUp(store)) {
            log.info("Initializing SDB store");
            if (isFirstStartup()) {
            	setupSDB(ctx, store);	
            } else {
            	migrateToSDBFromExistingRDBStore(ctx, store); 
            }
        }
        
        // The code below, which sets up the OntModelSelectors, controls whether each
        // model is maintained in memory, in the DB, or both while the application
        // is running.         
                
        // Populate the three OntModelSelectors (BaseOntModel=assertions, InferenceOntModel=inferences
        // and JenaOntModel=union of assertions and inferences) with the post-SDB-conversion models.

        // ABox assertions
        Model aboxAssertions = makeDBModel(bds, JenaDataSourceSetupBase.JENA_DB_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB, ctx);
        Model listenableAboxAssertions = ModelFactory.createUnion(aboxAssertions, ModelFactory.createDefaultModel());
        baseOms.setABoxModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, listenableAboxAssertions));
        
        // ABox inferences
        Model aboxInferences = makeDBModel(bds, JenaDataSourceSetupBase.JENA_INF_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB, ctx);
        Model listenableAboxInferences = ModelFactory.createUnion(aboxInferences, ModelFactory.createDefaultModel());
        inferenceOms.setABoxModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, listenableAboxInferences));
      
        // Since the TBox models are in memory, they do not have time out issues like the
        // ABox models do (and so don't need the extra step to make them listenable).
        // TBox assertions
        try {
            Model tboxAssertionsDB = makeDBModel(bds, JENA_TBOX_ASSERTIONS_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB, ctx);
            OntModel tboxAssertions = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            
            if (tboxAssertionsDB != null) {
                long startTime = System.currentTimeMillis();
                System.out.println("Copying cached tbox assertions into memory");
                tboxAssertions.add(tboxAssertionsDB);
                System.out.println((System.currentTimeMillis()-startTime)/1000+" seconds to load tbox assertions");
            }

            tboxAssertions.getBaseModel().register(new ModelSynchronizer(tboxAssertionsDB));
            baseOms.setTBoxModel(tboxAssertions);
        } catch (Throwable e) {
            log.error("Unable to load tbox assertion cache from DB", e);
        }
        
        // TBox inferences
        try {
            Model tboxInferencesDB = makeDBModel(bds, JENA_TBOX_INF_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB, ctx);
            OntModel tboxInferences = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            
            if (tboxInferencesDB != null) {
                long startTime = System.currentTimeMillis();
                System.out.println("Copying cached tbox inferences into memory");
                tboxInferences.add(tboxInferencesDB);
                System.out.println((System.currentTimeMillis()-startTime)/1000+" seconds to load tbox inferences");
            }
            
            tboxInferences.getBaseModel().register(new ModelSynchronizer(tboxInferencesDB));
            inferenceOms.setTBoxModel(tboxInferences);
        } catch (Throwable e) {
            log.error("Unable to load tbox inference cache from DB", e);
        }
                              
        // union ABox
        OntModel unionABoxModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC,ModelFactory.createUnion(baseOms.getABoxModel(), inferenceOms.getABoxModel()));
        unionOms.setABoxModel(unionABoxModel);
        
        // union TBox
        OntModel unionTBoxModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC,ModelFactory.createUnion(baseOms.getTBoxModel(), inferenceOms.getTBoxModel()));       
        unionOms.setTBoxModel(unionTBoxModel);
                  
        
        // Application metadata model is cached in memory.
        try {
            
            Model applicationMetadataModelDB = makeDBModel(bds, JENA_APPLICATION_METADATA_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB, ctx);
            OntModel applicationMetadataModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            
            long startTime = System.currentTimeMillis();
            System.out.println("Copying cached application metadata model into memory");
            applicationMetadataModel.add(applicationMetadataModelDB);
            System.out.println((System.currentTimeMillis()-startTime)/1000+" seconds to load application metadata model assertions of size " + applicationMetadataModel.size());
            applicationMetadataModel.getBaseModel().register(new ModelSynchronizer(applicationMetadataModelDB));
            
            if (isFirstStartup()) {
            	applicationMetadataModel.add(InitialJenaModelUtils.loadInitialModel(ctx, defaultNamespace));
            	
            } else if (applicationMetadataModelDB.size() == 0) {
                repairAppMetadataModel(applicationMetadataModel, aboxAssertions, aboxInferences);
            }
            
            baseOms.setApplicationMetadataModel(applicationMetadataModel);
            inferenceOms.setApplicationMetadataModel(baseOms.getApplicationMetadataModel());
            unionOms.setApplicationMetadataModel(baseOms.getApplicationMetadataModel());
            
        } catch (Throwable e) {
            log.error("Unable to load application metadata model cache from DB", e);
        }
        
        checkForNamespaceMismatch( baseOms.getApplicationMetadataModel(), defaultNamespace, ctx );
        
        if (isFirstStartup()) {
        	loadDataFromFilesystem(baseOms, ctx);
        }
        
        log.info("Adding vitro application ontology");
        
        // add the vitroontologies to the tbox models
        OntModel vitroTBoxModel = (new JenaBaseDaoCon()).getConstModel();
        baseOms.getTBoxModel().addSubModel(vitroTBoxModel);
        inferenceOms.getTBoxModel().addSubModel(vitroTBoxModel);
        unionOms.getTBoxModel().addSubModel(vitroTBoxModel);
        
        log.info("Setting up union models and DAO factories");
        
        // create TBox + ABox union models and set up webapp DAO factories
        OntModel baseUnion = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                ModelFactory.createUnion(baseOms.getABoxModel(), baseOms.getTBoxModel()));
        baseOms.setFullModel(baseUnion);
        ModelContext.setBaseOntModel(baseOms.getFullModel(), ctx);
        WebappDaoFactory baseWadf = new WebappDaoFactorySDB(
                baseOms,
                bds,
                storeDesc, 
                defaultNamespace, 
                null, 
                null, 
                WebappDaoFactorySDB.SDBDatasetMode.ASSERTIONS_ONLY);
        ctx.setAttribute("assertionsWebappDaoFactory",baseWadf);
        
        OntModel inferenceUnion = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                ModelFactory.createUnion(inferenceOms.getABoxModel(), inferenceOms.getTBoxModel()));
        inferenceOms.setFullModel(inferenceUnion);
        ModelContext.setInferenceOntModel(inferenceOms.getFullModel(), ctx);
        WebappDaoFactory infWadf = new WebappDaoFactorySDB(
                inferenceOms, 
                bds, 
                storeDesc, 
                defaultNamespace, 
                null, 
                null, 
                WebappDaoFactorySDB.SDBDatasetMode.INFERENCES_ONLY);
        ctx.setAttribute("deductionsWebappDaoFactory", infWadf);
        
        OntModel masterUnion = ModelFactory.createOntologyModel(
        		DB_ONT_MODEL_SPEC, makeDBModel(
        				bds, WebappDaoFactorySDB.UNION_GRAPH,
        						DB_ONT_MODEL_SPEC, TripleStoreType.SDB, ctx));
        //OntModel masterUnion = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
        //        ModelFactory.createUnion(unionABoxModel, unionTBoxModel));
        unionOms.setFullModel(masterUnion);
        ctx.setAttribute("jenaOntModel", masterUnion);  
        WebappDaoFactory wadf = new WebappDaoFactorySDB(unionOms, bds, storeDesc, defaultNamespace, null, null);
        ctx.setAttribute("webappDaoFactory",wadf);

        ModelContext.setUnionOntModelSelector(unionOms, ctx);          // assertions and inferences
        ModelContext.setBaseOntModelSelector(baseOms, ctx);            // assertions
        ModelContext.setInferenceOntModelSelector(inferenceOms, ctx);  // inferences       
        
        //log.info("Setting up namespace mapper");
        
        //NamespaceMapper namespaceMapper = new NamespaceMapperJena(masterUnion, masterUnion, defaultNamespace);
        //ctx.setAttribute("NamespaceMapper", namespaceMapper);
        //unionOms.getFullModel().getBaseModel().register(namespaceMapper);
        
        ctx.setAttribute("defaultNamespace", defaultNamespace);
        
        log.info("SDB store ready for use");
        
        makeModelMakerFromConnectionProperties(TripleStoreType.RDB, ctx);
        VitroJenaModelMaker vjmm = getVitroJenaModelMaker();
        setVitroJenaModelMaker(vjmm, ctx);
        makeModelMakerFromConnectionProperties(TripleStoreType.SDB, ctx);
        VitroJenaSDBModelMaker vsmm = getVitroJenaSDBModelMaker();
        setVitroJenaSDBModelMaker(vsmm, ctx);
        
        log.info("Model makers set up");
    }
    
    private void checkForNamespaceMismatch(OntModel model, String defaultNamespace, ServletContext ctx) {
		String defaultNamespaceFromDeployProperites = ConfigurationProperties
				.getBean(ctx).getProperty("Vitro.defaultNamespace");
        if( defaultNamespaceFromDeployProperites == null ){            
            log.error("Could not get namespace from deploy.properties.");
        }               
        
        List<String> portalURIs = new ArrayList<String>();
        try {
            model.enterCriticalSection(Lock.READ);
            Iterator<Individual> portalIt = model.listIndividuals(PORTAL);
            while (portalIt.hasNext()) {
                portalURIs.add( portalIt.next().getURI() );                
            }
        } finally {
            model.leaveCriticalSection();
        }
        if( portalURIs.size() > 0 ){
            for( String portalUri : portalURIs){
                if( portalUri != null && ! portalUri.startsWith(defaultNamespaceFromDeployProperites)){
                    log.error("Namespace mismatch between db and deploy.properties.");
                    log.error("Vivo will not start up correctly because the default namespace specified in deploy.properties does not match the namespace of " +
                            "a portal in the database. Namespace from deploy.properties: \"" + defaultNamespaceFromDeployProperites + 
                            "\" Namespace from an existing portal: \"" + portalUri + "\" To get the application to start with this " +
                            "database change the default namespace in deploy.properties " + portalUri.substring(0, portalUri.lastIndexOf("/")+1) + 
                            "  Another possibility is that deploy.properties does not specify the intended database.");
                }
            }
        }
    }


    /* ====================================================================== */
    
    
    @Override
	public void contextDestroyed(ServletContextEvent sce) {
    	// Nothing to do.
    }
    
    private OntModel ontModelFromContextAttribute(ServletContext ctx, String attribute) {
        OntModel ontModel;
        Object attributeValue = ctx.getAttribute(attribute);
        if (attributeValue != null && attributeValue instanceof OntModel) {
            ontModel = (OntModel) attributeValue;
        } else {
            ontModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            ctx.setAttribute(attribute, ontModel);
        }
        return ontModel;
    }
    
    private boolean isEmpty(Model model) {
        ClosableIterator<Statement> closeIt = model.listStatements(
                null, RDF.type, ResourceFactory.createResource(
                        VitroVocabulary.PORTAL));
        try {
            if (closeIt.hasNext()) {
                return false;
            } else {
                return true;
            }
        } finally {
            closeIt.close();
        }
    }
    
    private void loadDataFromFilesystem(OntModelSelector baseOms, ServletContext ctx) {
        Long startTime = System.currentTimeMillis();
        log.debug("Initializing models from RDF files");    
        readOntologyFilesInPathSet(USER_ABOX_PATH, ctx, baseOms.getABoxModel());
        readOntologyFilesInPathSet(USER_TBOX_PATH, ctx, baseOms.getTBoxModel());
        readOntologyFilesInPathSet(
        		USER_APPMETA_PATH, ctx, baseOms.getApplicationMetadataModel());
        //readOntologyFilesInPathSet(SYSTEMPATH, ctx, initialDataModel);
        log.debug(((System.currentTimeMillis()-startTime)/1000)
        		+ " seconds to read RDF files ");
    }

    private static void getTBoxModel(Model fullModel, Model submodels, Model tboxModel) {
  
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
    
    private static void getAppMetadata(Model source, Model target) {
             	
        String amdQuery = "DESCRIBE ?x WHERE { " +
                    "{?x a <" + VitroVocabulary.PORTAL +"> } UNION " +
                    "{?x a <" + VitroVocabulary.PROPERTYGROUP +"> } UNION " +
                    "{?x a <" + VitroVocabulary.CLASSGROUP +"> } } ";
        
		try {						
			Query q = QueryFactory.create(amdQuery, Syntax.syntaxARQ);
			QueryExecution qe = QueryExecutionFactory.create(q, source);
			qe.execDescribe(target);
	   	} catch (Exception e) {
			log.error("unable to create the application metadata model",e);
		}	
        
   		return;
    }
    
    private static void repairAppMetadataModel(Model applicationMetadataModel,
                                               Model aboxAssertions, 
                                               Model aboxInferences) {
     	
    	log.info("Moving application metadata from ABox to dedicated model");
        getAppMetadata(aboxAssertions, applicationMetadataModel);
        getAppMetadata(aboxInferences, applicationMetadataModel);
        aboxAssertions.remove(applicationMetadataModel);
        aboxInferences.remove(applicationMetadataModel);
        
   		return;
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
        SDBConnection conn = new SDBConnection(bds.getConnection()) ; 
        return SDBFactory.connectStore(conn, storeDesc);
    }
    
    public static void setupSDB(ServletContext ctx, Store store) {
    	setupSDB(ctx, 
    			 store, 
    			 ModelFactory.createDefaultModel(), 
    			 ModelFactory.createDefaultModel());
    }
    
    public static void setupSDB(ServletContext ctx, 
                                Store store, 
                                Model memModel, 
                                Model inferenceModel) {
        
        store.getTableFormatter().create();
        store.getTableFormatter().truncate();
        
        store.getTableFormatter().dropIndexes(); // improve load performance
        
        try {
        
            // This is a one-time copy of stored KB data - from a Jena RDB store
            // to a Jena SDB store. In the process, we will also separate out the
            // TBox from the Abox; these are in the same graph in pre 1.2 VIVO 
            // versions and will now be stored and maintained in separate models
            // Access to the Jena RDB data is through the OntModelSelectors that have
            // been set up earlier in the current session by
            // JenaPersistentDataSourceSetup.java
            // In the code below, note that the current getABoxModel() methods on 
            // the OntModelSelectors return a graph with both ABox and TBox data.
        
            OntModel submodels = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            readOntologyFilesInPathSet(SUBMODELS, ctx, submodels);
            
            Model tboxAssertions = SDBFactory.connectNamedModel(
                    store, JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL);
            // initially putting the results in memory so we have a
            // cheaper way of computing the difference when we copy the ABox
            Model memTboxAssertions = ModelFactory.createDefaultModel();
            getTBoxModel(memModel, submodels, memTboxAssertions);
            tboxAssertions.add(memTboxAssertions);
            
            Model tboxInferences = SDBFactory.connectNamedModel(
                    store, JenaDataSourceSetupBase.JENA_TBOX_INF_MODEL);
            // initially putting the results in memory so we have a
            // cheaper way of computing the difference when we copy the ABox
            Model memTboxInferences = ModelFactory.createDefaultModel();
            getTBoxModel(inferenceModel, submodels, memTboxInferences);
            tboxInferences.add(memTboxInferences);
            
            Model aboxAssertions = SDBFactory.connectNamedModel(
                    store, JenaDataSourceSetupBase.JENA_DB_MODEL);
            copyDifference(memModel, memTboxAssertions, aboxAssertions);
        
            Model aboxInferences = SDBFactory.connectNamedModel(
                    store, JenaDataSourceSetupBase.JENA_INF_MODEL);
            copyDifference(inferenceModel, memTboxInferences, aboxInferences);
            
            // Set up the application metadata model
            Model applicationMetadataModel = SDBFactory.connectNamedModel(
                    store, JenaDataSourceSetupBase.JENA_APPLICATION_METADATA_MODEL);
            getAppMetadata(memModel, applicationMetadataModel);
    	   	log.info("During initial SDB setup, created an application metadata model of size " 
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
            return (SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL)).size() > 0;    
        } catch (Exception e) { 
            return false;
        }
    }
    
    private static final String STOREDESC_ATTR = "storeDesc";
    private static final String STORE_ATTR = "kbStore";
    
    public static void setApplicationStoreDesc(StoreDesc storeDesc, 
                                          ServletContext ctx) {
        ctx.setAttribute(STOREDESC_ATTR, storeDesc);
    }
   
    public static StoreDesc getApplicationStoreDesc(ServletContext ctx) {
        return (StoreDesc) ctx.getAttribute(STOREDESC_ATTR);
    }
    
    public static void setApplicationStore(Store store,
                                           ServletContext ctx) {
        ctx.setAttribute(STORE_ATTR, store);
    }
    
    public static Store getApplicationStore(ServletContext ctx) {
        return (Store) ctx.getAttribute(STORE_ATTR);
    }
 }

