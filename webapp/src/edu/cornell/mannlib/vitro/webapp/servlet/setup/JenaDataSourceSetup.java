/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

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
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.DatasetStore;
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
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.jena.InitialJenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.NamespaceMapperJena;

public class JenaDataSourceSetup extends JenaDataSourceSetupBase implements javax.servlet.ServletContextListener {
	
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
            memModel.addSubModel((new JenaBaseDaoCon()).getConstModel()); // add the vitro tbox to the model
            
            OntModel inferenceModel = ontModelFromContextAttribute(sce.getServletContext(), "inferenceOntModel");
            
            OntModel userAccountsModel = ontModelFromContextAttribute(sce.getServletContext(), "userAccountsOntModel");            
            if (userAccountsModel.size() == 0) {
        		checkMainModelForUserAccounts(memModel, userAccountsModel);
        	}
            
            OntModel unionModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC,ModelFactory.createUnion(memModel, inferenceModel));

            OntModelSelectorImpl baseOms = new OntModelSelectorImpl();
            baseOms.setApplicationMetadataModel(memModel);
            baseOms.setTBoxModel(memModel);
            baseOms.setFullModel(memModel);
            OntModelSelectorImpl inferenceOms = new OntModelSelectorImpl();
            inferenceOms.setABoxModel(inferenceModel);
            inferenceOms.setTBoxModel(inferenceModel);
            inferenceOms.setFullModel(inferenceModel);
            OntModelSelectorImpl unionOms = new OntModelSelectorImpl();
            unionOms.setApplicationMetadataModel(unionModel);
            unionOms.setTBoxModel(unionModel);
            unionOms.setFullModel(unionModel);
        	baseOms.setUserAccountsModel(userAccountsModel);
        	inferenceOms.setUserAccountsModel(userAccountsModel);
        	unionOms.setUserAccountsModel(userAccountsModel);       
            
        	OntModel displayModel = ontModelFromContextAttribute(sce.getServletContext(),"displayOntModel");
        	baseOms.setDisplayModel(displayModel);
        	inferenceOms.setDisplayModel(displayModel);
        	unionOms.setDisplayModel(displayModel);
        			
        	checkForNamespaceMismatch( memModel, defaultNamespace );

        	// SDB initialization
			StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL) ;
			BasicDataSource bds = makeDataSourceFromConfigurationProperties();
        	this.setApplicationDataSource(bds, sce.getServletContext());
        	SDBConnection conn = new SDBConnection(bds.getConnection()) ; 
        	Store store = SDBFactory.connectStore(conn, storeDesc);
        	try {
        		// a test query to see if the store is formatted
        		SDBFactory.connectDefaultModel(store).contains(OWL.Thing, RDF.type, OWL.Nothing); 
        	} catch (Exception e) { // unformatted store
        		log.debug("Non-SDB system detected. Setting up SDB store");
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
            	
            	JenaModelUtils modelUtils = new JenaModelUtils();
            	
            	Model aboxAssertions = SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_DB_MODEL);
            	aboxAssertions.add(modelUtils.extractABox(baseOms.getABoxModel()));
            
            	Model aboxInferences = SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_INF_MODEL);
            	aboxInferences.add(modelUtils.extractABox(inferenceOms.getABoxModel()));
            	
            	Model tboxAssertions = SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL);
            	tboxAssertions.add(modelUtils.extractTBox(baseOms.getABoxModel()));
            	
            	Model tboxInferences = SDBFactory.connectNamedModel(store, JenaDataSourceSetupBase.JENA_TBOX_INF_MODEL);
            	tboxInferences.add(modelUtils.extractTBox(inferenceOms.getABoxModel())); 
            	 
            	// The code below, which sets up the OntModelSelectors, controls whether each
            	// model is maintained in memory, in the DB, or both, while the application
            	// is running.
        	}

        	
            sce.getServletContext().setAttribute("kbStore", store);
            
            //store.getTableFormatter().dropIndexes();
            //store.getTableFormatter().addIndexes();
        	       	
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
            
        	Dataset dataset = DatasetStore.create(store);
        	//String queryStr = "CONSTRUCT { ?s ?p ?o } \n" +
        	//                  "WHERE { GRAPH ?g { ?s ?p ?o } } ";
        	//Query query = QueryFactory.create(queryStr);
        	//QueryExecution qe = QueryExecutionFactory.create(query, dataset);
        	//log.info("Test query returned " + qe.execConstruct().size() + " statements");
        	
            sce.getServletContext().setAttribute("baseOntModel", memModel);
            WebappDaoFactory baseWadf = new WebappDaoFactorySDB(baseOms, dataset, defaultNamespace, null, null);
            //WebappDaoFactory baseWadf = new WebappDaoFactoryJena(baseOms, defaultNamespace, null, null);
            sce.getServletContext().setAttribute("assertionsWebappDaoFactory",baseWadf);
            
            sce.getServletContext().setAttribute("inferenceOntModel", inferenceModel);
            WebappDaoFactory infWadf = new WebappDaoFactorySDB(inferenceOms, dataset, defaultNamespace, null, null);
            //WebappDaoFactory infWadf = new WebappDaoFactoryJena(inferenceOms, defaultNamespace, null, null);
            sce.getServletContext().setAttribute("deductionsWebappDaoFactory", infWadf);
            
            sce.getServletContext().setAttribute("jenaOntModel", unionModel);  
            WebappDaoFactory wadf = new WebappDaoFactorySDB(unionOms, dataset, defaultNamespace, null, null);
            //WebappDaoFactory wadf = new WebappDaoFactoryJena(unionOms, defaultNamespace, null, null);
            sce.getServletContext().setAttribute("webappDaoFactory",wadf);
            
            sce.getServletContext().setAttribute("unionOntModelSelector", unionOms);
            sce.getServletContext().setAttribute("baseOntModelSelector", baseOms);
            
            ApplicationBean appBean = getApplicationBeanFromOntModel(memModel,wadf);
            if (appBean != null) {
            	sce.getServletContext().setAttribute("applicationBean", appBean);
            }
            
            if (isEmpty(memModel)) {
            	loadDataFromFilesystem(memModel, sce.getServletContext());
            }
            
            if (userAccountsModel.size() == 0) {
            	readOntologyFilesInPathSet(AUTHPATH, sce.getServletContext(), userAccountsModel);
	            if (userAccountsModel.size() == 0) {
	            	createInitialAdminUser(userAccountsModel);
	            }
            }                        
            
            ensureEssentialInterfaceData(memModel, sce, wadf);        
            
        	NamespaceMapper namespaceMapper = new NamespaceMapperJena(unionModel, unionModel, defaultNamespace);
        	sce.getServletContext().setAttribute("NamespaceMapper", namespaceMapper);
        	memModel.getBaseModel().register(namespaceMapper);
        	       	
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
            Iterator portalIt = model.listIndividuals(PORTAL);
            while (portalIt.hasNext()) {
                portalURIs.add( ((Individual)portalIt.next()).getURI() );                
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
       ClosableIterator appIt = ontModel.listIndividuals(ResourceFactory.createResource(VitroVocabulary.APPLICATION));
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
        ClosableIterator portalIt = memModel.listIndividuals(memModel.getResource(VitroVocabulary.PORTAL));
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
    	ClosableIterator closeIt = model.listStatements();
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
    
}
