/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSDBModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Primarily sets up webapp DAO factories.
 */
public class WebappDaoSDBSetup extends JenaDataSourceSetupBase 
        implements javax.servlet.ServletContextListener {
    
    private static final Log log = LogFactory.getLog(WebappDaoSDBSetup.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        
        try {
            long startTime = System.currentTimeMillis();
            setUpJenaDataSource(ctx, ss);
            log.info((System.currentTimeMillis() - startTime) / 1000 + 
                    " seconds to set up SDB store");
        } catch (SQLException sqle) {   
            // SQL exceptions are fatal and should halt startup
            log.error("Error using SQL database; startup aborted.", sqle);
            ss.fatal(this, "Error using SQL database; startup aborted.", sqle);
        } catch (Throwable t) {
            log.error("Throwable in " + this.getClass().getName(), t);
            ss.fatal(this, "Throwable in " + this.getClass().getName(), t);
        }
        
    } 

    private void setUpJenaDataSource(ServletContext ctx, StartupStatus ss) throws SQLException {
        
        BasicDataSource bds = getApplicationDataSource(ctx);
        if( bds == null ){
            ss.fatal(this, "A DataSource must be setup before "+ WebappDaoSDBSetup.class.getName() +
                    "is run. Make sure that JenaPersistentDataSourceSetup runs before "+
                    WebappDaoSDBSetup.class.getName() );
            return;
        }
        
        //Get the OntModelSelectors        
        OntModelSelectorImpl baseOms = 
            (OntModelSelectorImpl) ModelContext.getBaseOntModelSelector(ctx);     
        OntModelSelectorImpl inferenceOms = 
            (OntModelSelectorImpl) ModelContext.getInferenceOntModelSelector(ctx);
        OntModelSelectorImpl unionOms = 
            (OntModelSelectorImpl) ModelContext.getUnionOntModelSelector(ctx);                     
        
        ///////////////////////////////////////////////////////////////
        // Check for namespace mismatch
        
        checkForNamespaceMismatch( baseOms.getApplicationMetadataModel(), ctx );
        ctx.setAttribute("defaultNamespace", getDefaultNamespace(ctx));
        
        ///////////////////////////////////////////////////////////////
        // first startup?
        
        if (isFirstStartup()) {
            loadDataFromFilesystem(baseOms, ctx);
        }
        
        log.info("Setting up DAO factories");        

        ///////////////////////////////////////////////////////////////
        //create assertions webapp DAO factory
        
        StoreDesc storeDesc = getApplicationStoreDesc(ctx);
        WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
        config.setDefaultNamespace(getDefaultNamespace(ctx));
        WebappDaoFactory baseWadf = new WebappDaoFactorySDB(
                baseOms, bds, storeDesc, config,
                WebappDaoFactorySDB.SDBDatasetMode.ASSERTIONS_ONLY);
        ctx.setAttribute("assertionsWebappDaoFactory",baseWadf);
        
        ///////////////////////////////////////////////////////////////
        //create inference webapp DAO factory
        
        OntModel inferenceUnion = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM,
                ModelFactory.createUnion(
                        inferenceOms.getABoxModel(), 
                        inferenceOms.getTBoxModel()));
        inferenceOms.setFullModel(inferenceUnion);
        ModelContext.setInferenceOntModel(inferenceOms.getFullModel(), ctx);
        WebappDaoFactory infWadf = new WebappDaoFactorySDB(
                inferenceOms, bds, storeDesc, config, 
                WebappDaoFactorySDB.SDBDatasetMode.INFERENCES_ONLY);
        ctx.setAttribute("deductionsWebappDaoFactory", infWadf);
        
        ///////////////////////////////////////////////////////////////
        //create default union webapp DAO factory
        
        OntModel masterUnion = ModelFactory.createOntologyModel(
                DB_ONT_MODEL_SPEC, makeDBModel(
                        bds, WebappDaoFactorySDB.UNION_GRAPH,
                                DB_ONT_MODEL_SPEC, TripleStoreType.SDB, ctx));
        unionOms.setFullModel(masterUnion);
        ctx.setAttribute("jenaOntModel", masterUnion);  
        WebappDaoFactory wadf = new WebappDaoFactorySDB(
                unionOms, bds, storeDesc, config);
        ctx.setAttribute("webappDaoFactory",wadf);                           
        
        makeModelMakerFromConnectionProperties(TripleStoreType.RDB, ctx);
        VitroJenaModelMaker vjmm = getVitroJenaModelMaker();
        setVitroJenaModelMaker(vjmm, ctx);
        makeModelMakerFromConnectionProperties(TripleStoreType.SDB, ctx);
        VitroJenaSDBModelMaker vsmm = getVitroJenaSDBModelMaker();
        setVitroJenaSDBModelMaker(vsmm, ctx);
                
        //bdc34: I have no reason for vsmm vs vjmm.  
        //I don't know what are the implications of this choice.        
        setVitroModelSource( new VitroModelSource(vsmm,ctx), ctx);
        
        log.info("DAOs set up");
    }


    /**
     * If we find a "portal1" portal (and we should), its URI should use the
     * default namespace.
     */
    private void checkForNamespaceMismatch(OntModel model, ServletContext ctx) {
        String expectedNamespace = getDefaultNamespace(ctx);

        List<Resource> portals = getPortal1s(model);

        if(!portals.isEmpty() && noPortalForNamespace(
                portals, expectedNamespace)) {
            // There really should be only one portal 1, but if there happen to 
            // be multiple, just arbitrarily pick the first in the list.
            Resource portal = portals.get(0); 
            String oldNamespace = portal.getNameSpace();
            renamePortal(portal, expectedNamespace, model);
            StartupStatus ss = StartupStatus.getBean(ctx);
            ss.warning(this, "\nThe default namespace has been changed \n" +  
                             "from " + oldNamespace + 
                             "\nto " + expectedNamespace + ".\n" +
                             "The application will function normally, but " +
                             "any individuals in the \n" + oldNamespace + " " + 
                             "namespace will need to have their URIs \n" +
                             "changed in order to be served as linked data. " +
                             "You can use the Ingest Tools \nto change the " +
                             "URIs for a batch of resources.");
        }
    }
    
    private List<Resource> getPortal1s(Model model) {
        List<Resource> portals = new ArrayList<Resource>();
        try {
            model.enterCriticalSection(Lock.READ);
            ResIterator portalIt = model.listResourcesWithProperty(
                    RDF.type, PORTAL);
            while (portalIt.hasNext()) {
                Resource portal = portalIt.nextResource();
                if ("portal1".equals(portal.getLocalName())) {
                    portals.add(portal);
                }
            }
        } finally {
            model.leaveCriticalSection();
        }
        return portals;
    }
    
    private boolean noPortalForNamespace(List<Resource> portals, 
                                         String expectedNamespace) {
        for (Resource portal : portals) {
            if(expectedNamespace.equals(portal.getNameSpace())) {
                return false;
            }
        }
        return true;
    }

    private void renamePortal(Resource portal, String namespace, Model model) {
        model.enterCriticalSection(Lock.WRITE);
        try {
            ResourceUtils.renameResource(
                    portal, namespace + portal.getLocalName());
        } finally {
            model.leaveCriticalSection();
        }
    }
    

    /* ===================================================================== */
    
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to do.
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
    
    private void loadDataFromFilesystem(OntModelSelector baseOms, 
                                        ServletContext ctx) {
        Long startTime = System.currentTimeMillis();
        log.info("Initializing models from RDF files");    
        
        readOntologyFilesInPathSet(USER_ABOX_PATH, ctx, baseOms.getABoxModel());
        readOntologyFilesInPathSet(USER_TBOX_PATH, ctx, baseOms.getTBoxModel());
        readOntologyFilesInPathSet(
                USER_APPMETA_PATH, ctx, baseOms.getApplicationMetadataModel());
        
        log.debug(((System.currentTimeMillis() - startTime) / 1000)
                + " seconds to read RDF files ");
    }
   
  

 
 }

