/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoCon;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.jena.InitialJenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.NamespaceMapperJena;

public class JenaDataSourceSetupSDB extends JenaDataSourceSetupBase implements javax.servlet.ServletContextListener {
    
    private static final Log log = LogFactory.getLog(JenaDataSourceSetup.class.getName());
    
    public void contextInitialized(ServletContextEvent sce) {
        try {

            // JenaPersistentDataSourceSetup should have already set this up - it just sets
            // up things related to the DB.
            // TODO: I would like to make this code (before the sdb try/catch conditional so
            // that it is not executed in a post-sdb-conversion environment.
            OntModel memModel = (OntModel) sce.getServletContext().getAttribute("jenaOntModel");
            
            if (memModel == null) {
                memModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
                log.warn("WARNING: no database connected.  Changes will disappear after context restart.");
                sce.getServletContext().setAttribute("jenaOntModel",memModel);
            }  
            
            OntModel inferenceModel = ontModelFromContextAttribute(sce.getServletContext(), "inferenceOntModel");
            
            OntModel userAccountsModel = ontModelFromContextAttribute(sce.getServletContext(), "userAccountsOntModel");            
            if (userAccountsModel.size() == 0) {
                checkMainModelForUserAccounts(memModel, userAccountsModel);
            }

            OntModelSelectorImpl baseOms = new OntModelSelectorImpl();
            
            OntModelSelectorImpl inferenceOms = new OntModelSelectorImpl();
            
            OntModelSelectorImpl unionOms = new OntModelSelectorImpl();
            
            baseOms.setUserAccountsModel(userAccountsModel);
            inferenceOms.setUserAccountsModel(userAccountsModel);
            unionOms.setUserAccountsModel(userAccountsModel);       
            
            OntModel displayModel = ontModelFromContextAttribute(sce.getServletContext(),"displayOntModel");
            baseOms.setDisplayModel(displayModel);
            inferenceOms.setDisplayModel(displayModel);
            unionOms.setDisplayModel(displayModel);
                    
            checkForNamespaceMismatch( memModel, defaultNamespace );

            // SDB setup
            StoreDesc storeDesc = makeStoreDesc();
            setApplicationStoreDesc(storeDesc, sce.getServletContext());

            BasicDataSource bds = makeDataSourceFromConfigurationProperties();
            this.setApplicationDataSource(bds, sce.getServletContext());
            
            Store store = connectStore(bds, storeDesc);
            setApplicationStore(store, sce.getServletContext());
            
            if (!isSetUp(store)) {
                log.debug("Non-SDB system detected. Setting up SDB store");
                setupSDB(sce.getServletContext(), store, memModel, inferenceModel);
            }
            
            // The code below, which sets up the OntModelSelectors, controls whether each
            // model is maintained in memory, in the DB, or both while the application
            // is running.         
                    
            // Populate the three OntModelSelectors (BaseOntModel=assertions, InferenceOntModel=inferences
            // and JenaOntModel=union of assertions and inferences) with the post-SDB-conversion models.
 
            // ABox assertions
            Model aboxAssertions = makeDBModel(bds, JenaDataSourceSetupBase.JENA_DB_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB);
            Model listenableAboxAssertions = ModelFactory.createUnion(aboxAssertions, ModelFactory.createDefaultModel());
            baseOms.setABoxModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, listenableAboxAssertions));
            
            // ABox inferences
            Model aboxInferences = makeDBModel(bds, JenaDataSourceSetupBase.JENA_INF_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB);
            Model listenableAboxInferences = ModelFactory.createUnion(aboxInferences, ModelFactory.createDefaultModel());
            inferenceOms.setABoxModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, listenableAboxInferences));

            
            // Since the TBox models are in memory, they do not have time out issues like the
            // ABox models do (and so don't need the extra step to make them listenable).
            // TBox assertions
            try {
                Model tboxAssertionsDB = makeDBModel(bds, JENA_TBOX_ASSERTIONS_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB);
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
                Model tboxInferencesDB = makeDBModel(bds, JENA_TBOX_INF_MODEL, DB_ONT_MODEL_SPEC, TripleStoreType.SDB);
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
                         
            // add the vitro ontologies to the tbox models
            OntModel vitroTBoxModel = (new JenaBaseDaoCon()).getConstModel();
            baseOms.getTBoxModel().addSubModel(vitroTBoxModel);
            inferenceOms.getTBoxModel().addSubModel(vitroTBoxModel);
            unionOms.getTBoxModel().addSubModel(vitroTBoxModel);
            
            // create TBox + ABox union models and set up webapp DAO factories
            OntModel baseUnion = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                    ModelFactory.createUnion(baseOms.getABoxModel(), baseOms.getTBoxModel()));
            baseOms.setFullModel(baseUnion);
            ModelContext.setBaseOntModel(baseOms.getFullModel(), sce.getServletContext());
            WebappDaoFactory baseWadf = new WebappDaoFactorySDB(
                    baseOms,
                    bds,
                    storeDesc, 
                    defaultNamespace, 
                    null, 
                    null, 
                    WebappDaoFactorySDB.SDBDatasetMode.ASSERTIONS_ONLY);
            sce.getServletContext().setAttribute("assertionsWebappDaoFactory",baseWadf);
            
            OntModel inferenceUnion = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                    ModelFactory.createUnion(inferenceOms.getABoxModel(), inferenceOms.getTBoxModel()));
            inferenceOms.setFullModel(inferenceUnion);
            ModelContext.setInferenceOntModel(inferenceOms.getFullModel(), sce.getServletContext());
            WebappDaoFactory infWadf = new WebappDaoFactorySDB(
                    inferenceOms, 
                    bds, 
                    storeDesc, 
                    defaultNamespace, 
                    null, 
                    null, 
                    WebappDaoFactorySDB.SDBDatasetMode.INFERENCES_ONLY);
            sce.getServletContext().setAttribute("deductionsWebappDaoFactory", infWadf);
            
            OntModel masterUnion = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,
                    ModelFactory.createUnion(unionABoxModel, unionTBoxModel));
            unionOms.setFullModel(masterUnion);
            sce.getServletContext().setAttribute("jenaOntModel", masterUnion);  
            WebappDaoFactory wadf = new WebappDaoFactorySDB(unionOms, bds, storeDesc, defaultNamespace, null, null);
            //WebappDaoFactory wadf = new WebappDaoFactorySDB(unionOms, dataset, defaultNamespace, null, null);
            sce.getServletContext().setAttribute("webappDaoFactory",wadf);
            
            sce.getServletContext().setAttribute("unionOntModelSelector", unionOms);          //assertions and inferences
            sce.getServletContext().setAttribute("baseOntModelSelector", baseOms);            //assertions
            sce.getServletContext().setAttribute("inferenceOntModelSelector", inferenceOms);  //inferences
            
            // use "full" models as the legacy application metadata models
            baseOms.setApplicationMetadataModel(baseOms.getFullModel());
            inferenceOms.setApplicationMetadataModel(inferenceOms.getFullModel());
            unionOms.setApplicationMetadataModel(unionOms.getFullModel());
            
            ApplicationBean appBean = getApplicationBeanFromOntModel(unionOms.getFullModel(),wadf);
            if (appBean != null) {
                sce.getServletContext().setAttribute("applicationBean", appBean);
            }
            
            if (isEmpty(unionOms.getFullModel())) {
                loadDataFromFilesystem(unionOms.getFullModel(), sce.getServletContext());
            }
            
            if (userAccountsModel.size() == 0) {
                readOntologyFilesInPathSet(AUTHPATH, sce.getServletContext(), userAccountsModel);
                if (userAccountsModel.size() == 0) {
                    createInitialAdminUser(userAccountsModel);
                }
            }                        
            
            ensureEssentialInterfaceData(unionOms.getFullModel(), sce, wadf);        
            
            NamespaceMapper namespaceMapper = new NamespaceMapperJena(masterUnion, masterUnion, defaultNamespace);
            sce.getServletContext().setAttribute("NamespaceMapper", namespaceMapper);
            unionOms.getFullModel().getBaseModel().register(namespaceMapper);
            
            sce.getServletContext().setAttribute("defaultNamespace", defaultNamespace);
            
            makeModelMakerFromConnectionProperties(TripleStoreType.RDB);
            VitroJenaModelMaker vjmm = getVitroJenaModelMaker();
            setVitroJenaModelMaker(vjmm,sce);
            makeModelMakerFromConnectionProperties(TripleStoreType.SDB);
            VitroJenaSDBModelMaker vsmm = getVitroJenaSDBModelMaker();
            setVitroJenaSDBModelMaker(vsmm,sce);
                     
        } catch (Throwable t) {
            log.error("Throwable in " + this.getClass().getName(), t);
            // printing the error because Tomcat doesn't print context listener
            // errors the same way it prints other errors at startup
            t.printStackTrace();
            throw new Error(this.getClass().getName() + "failed");
        }
    } 

    
    private void checkForNamespaceMismatch(OntModel model, String defaultNamespace) {
        String defaultNamespaceFromDeployProperites = ConfigurationProperties.getProperty("Vitro.defaultNamespace");
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
    
    
    public void contextDestroyed(ServletContextEvent sce) {
    }

    private ApplicationBean getApplicationBeanFromOntModel(OntModel ontModel,WebappDaoFactory wadf) {
       ClosableIterator<Individual> appIt = ontModel.listIndividuals(
               ResourceFactory.createResource(VitroVocabulary.APPLICATION));
        try {
              if (appIt.hasNext()) {
                  Individual appInd = (Individual) appIt.next();
                  ApplicationBean appBean = new ApplicationBean();
                  try {
                      appBean.setMaxPortalId(Integer.decode( ((Literal)appInd.getPropertyValue(ResourceFactory.createProperty(VitroVocabulary.APPLICATION_MAXPORTALID))).getLexicalForm()));
                  } catch (Exception e) { /* ignore bad value */ }
                  try {
                      appBean.setMinSharedPortalId(Integer.decode( ((Literal)appInd.getPropertyValue(ResourceFactory.createProperty(VitroVocabulary.APPLICATION_MINSHAREDPORTALID))).getLexicalForm()));
                  } catch (Exception e) { /* ignore bad value */ }
                  try {
                     appBean.setMaxSharedPortalId(Integer.decode( ((Literal)appInd.getPropertyValue(ResourceFactory.createProperty(VitroVocabulary.APPLICATION_MAXSHAREDPORTALID))).getLexicalForm()));
                  } catch (Exception e) { /* ignore bad value */}
                  if( ! wadf.getApplicationDao().isFlag1Active() ){
                      appBean.setMaxPortalId(1);
                  }
                 return appBean;
             } else {
                 return null;
             }
         } finally {
             appIt.close();
         }
    }
    
    private void ensureEssentialInterfaceData(OntModel memModel, ServletContextEvent sce, WebappDaoFactory wadf) {
        Model essentialInterfaceData = null;
        ClosableIterator<Individual> portalIt = memModel.listIndividuals(
                memModel.getResource(VitroVocabulary.PORTAL));
        try {
            if (!portalIt.hasNext()) {
                log.debug("Loading initial site configuration");
                essentialInterfaceData = InitialJenaModelUtils.loadInitialModel(sce.getServletContext(), defaultNamespace);
                if (essentialInterfaceData.size() == 0) {
                    essentialInterfaceData = InitialJenaModelUtils.basicPortalAndRootTab(defaultNamespace);
                    essentialInterfaceData.add(InitialJenaModelUtils.basicClassgroup(wadf.getDefaultNamespace()));
                }
                //JenaModelUtils.makeClassGroupsFromRootClasses(wadf,memModel,essentialInterfaceData);       
                memModel.add(essentialInterfaceData);
            } else {
                //Set the default namespace to the namespace of the first portal object we find.
                //This will keep existing applications from dying when the default namespace
                //config option is missing.
                Individual portal = (Individual) portalIt.next();
                if (portal.getNameSpace() != null) {
                    defaultNamespace = portal.getNameSpace();
                }
            }
        } finally {
            portalIt.close();
        }
    }
    
    private void checkMainModelForUserAccounts(OntModel mainModel, OntModel userAccountsModel) {
        Model extractedUserData = ((new JenaModelUtils()).extractUserAccountsData(mainModel));
        if (extractedUserData.size() > 0) {
            userAccountsModel.enterCriticalSection(Lock.WRITE);
            try {
                userAccountsModel.add(extractedUserData);
            } finally {
                userAccountsModel.leaveCriticalSection();
            }
            mainModel.enterCriticalSection(Lock.WRITE);
            try {
                mainModel.remove(extractedUserData);
            } finally {
                mainModel.leaveCriticalSection();
            }
        }
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
        ClosableIterator<Statement> closeIt = model.listStatements();
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
    
    private void loadDataFromFilesystem(OntModel ontModel, ServletContext ctx) {
        OntModel initialDataModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
        Long startTime = System.currentTimeMillis();
        log.debug("Reading ontology files");    
        readOntologyFilesInPathSet(USERPATH, ctx, initialDataModel);
        readOntologyFilesInPathSet(SYSTEMPATH, ctx, initialDataModel);
        log.debug(((System.currentTimeMillis()-startTime)/1000)+" seconds to read ontology files ");
        ontModel.add(initialDataModel);
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
    
    public static StoreDesc makeStoreDesc() {
        String layoutStr = ConfigurationProperties.getProperty(
                "VitroConnection.DataSource.sdb.layout","layout2/hash");
        String dbtypeStr = ConfigurationProperties.getProperty(
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
    
    public static void setupSDB(ServletContext ctx, 
                                Store store, 
                                Model memModel, 
                                Model inferenceModel) {
        
        store.getTableFormatter().create();
        store.getTableFormatter().truncate();
        
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
        
        Model tboxAssertions = SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL);
        // initially putting the results in memory so we have a
        // cheaper way of computing the difference when we copy the ABox
        Model memTboxAssertions = ModelFactory.createDefaultModel();
        getTBoxModel(memModel, submodels, memTboxAssertions);
        tboxAssertions.add(memTboxAssertions);
        
        Model tboxInferences = SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_TBOX_INF_MODEL);
        // initially putting the results in memory so we have a
        // cheaper way of computing the difference when we copy the ABox
        Model memTboxInferences = ModelFactory.createDefaultModel();
        getTBoxModel(inferenceModel, submodels, memTboxInferences);
        tboxInferences.add(memTboxInferences);
        
        Model aboxAssertions = SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_DB_MODEL);
        copyDifference(memModel, memTboxAssertions, aboxAssertions);
    
        Model aboxInferences = SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_INF_MODEL);
        copyDifference(inferenceModel, memTboxInferences, aboxInferences);
        
        // Make sure the reasoner takes into account the newly-set-up data.
        SimpleReasonerSetup.setRecomputeRequired(ctx);

    }
    
    /**
     * Tests whether an SDB store has been formatted for use.
     * @param store
     * @return
     */
    private boolean isSetUp(Store store) {
        try {
            // a test query to see if the store is formatted
            SDBFactory.connectDefaultModel(store).contains(
                    OWL.Thing, RDF.type, OWL.Nothing);
            return true;
        } catch (Exception e) { // unformatted store
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
