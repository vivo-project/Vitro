/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoCon;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SearchReindexingListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.TripleStoreType;
import edu.cornell.mannlib.vitro.webapp.utils.NamespaceMapper;
import edu.cornell.mannlib.vitro.webapp.utils.jena.InitialJenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.NamespaceMapperJena;

public class JenaDataSourceSetup extends JenaDataSourceSetupBase implements javax.servlet.ServletContextListener {
	
	private static final Log log = LogFactory.getLog(JenaDataSourceSetup.class.getName());
	
    public void contextInitialized(ServletContextEvent sce) {
        
        if (AbortStartup.isStartupAborted(sce.getServletContext())) {
            return;
        }
        
        try {
            
            String tripleStoreTypeStr = 
                ConfigurationProperties.getProperty(
                        "VitroConnection.DataSource.tripleStoreType", "RDB");
            
            //FIXME improve
            if ("SDB".equals(tripleStoreTypeStr)) {
                (new JenaDataSourceSetupSDB()).contextInitialized(sce);
                return;
            }
            
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

            SimpleOntModelSelector baseOms = new SimpleOntModelSelector(memModel);
            SimpleOntModelSelector inferenceOms = new SimpleOntModelSelector(inferenceModel);
            SimpleOntModelSelector unionOms = new SimpleOntModelSelector(unionModel);                  
        	baseOms.setUserAccountsModel(userAccountsModel);
        	inferenceOms.setUserAccountsModel(userAccountsModel);
        	unionOms.setUserAccountsModel(userAccountsModel);       
            
        	OntModel displayModel = ontModelFromContextAttribute(sce.getServletContext(),"displayOntModel");
        	baseOms.setDisplayModel(displayModel);
        	inferenceOms.setDisplayModel(displayModel);
        	unionOms.setDisplayModel(displayModel);
        			
        	checkForNamespaceMismatch( memModel, defaultNamespace );
        	
            sce.getServletContext().setAttribute("baseOntModel", memModel);
            WebappDaoFactory baseWadf = new WebappDaoFactoryJena(
                    baseOms, defaultNamespace, null, null);
            sce.getServletContext().setAttribute("assertionsWebappDaoFactory",baseWadf);
            sce.getServletContext().setAttribute("baseOntModelSelector", baseOms);
            
            sce.getServletContext().setAttribute("inferenceOntModel", inferenceModel);
            WebappDaoFactory infWadf = new WebappDaoFactoryJena(
                    inferenceOms, defaultNamespace, null, null);
            sce.getServletContext().setAttribute("deductionsWebappDaoFactory", infWadf);
            sce.getServletContext().setAttribute("inferenceOntModelSelector", inferenceOms);
            
            sce.getServletContext().setAttribute("jenaOntModel", unionModel);  
            WebappDaoFactory wadf = new WebappDaoFactoryJena(
                    unionOms, baseOms, inferenceOms,  defaultNamespace, null, null);
            sce.getServletContext().setAttribute("webappDaoFactory",wadf);
            sce.getServletContext().setAttribute("unionOntModelSelector", unionOms);
            
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

