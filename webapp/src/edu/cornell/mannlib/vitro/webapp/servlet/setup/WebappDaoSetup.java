/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.DISPLAY_ONT_MODEL;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SpecialBulkUpdateHandlerGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.jena.InitialJenaModelUtils;

/**
 * Primarily sets up webapp DAO factories.
 */
public class WebappDaoSetup extends JenaDataSourceSetupBase 
        implements javax.servlet.ServletContextListener {
    
    private static final Log log = LogFactory.getLog(WebappDaoSetup.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        
        try {
            long startTime = System.currentTimeMillis();
            setUpJenaDataSource(ctx, ss);
            log.info((System.currentTimeMillis() - startTime) / 1000 + 
                    " seconds to set up models and DAO factories");  
        } catch (Throwable t) {
            log.error("Throwable in " + this.getClass().getName(), t);
            ss.fatal(this, "Throwable in " + this.getClass().getName(), t);
        }
        
    } 

    private void setUpJenaDataSource(ServletContext ctx, StartupStatus ss) {
        OntModelSelectorImpl baseOms = new OntModelSelectorImpl();     
        OntModelSelectorImpl inferenceOms = new OntModelSelectorImpl();       
        OntModelSelectorImpl unionOms = new OntModelSelectorImpl();
        
        OntModel userAccountsModel = ontModelFromContextAttribute(
                ctx, "userAccountsOntModel");     
        baseOms.setUserAccountsModel(userAccountsModel);
        inferenceOms.setUserAccountsModel(userAccountsModel);
        unionOms.setUserAccountsModel(userAccountsModel);       
        
        OntModel displayModel = ontModelFromContextAttribute(
                ctx,DISPLAY_ONT_MODEL);
        baseOms.setDisplayModel(displayModel);
        inferenceOms.setDisplayModel(displayModel);
        unionOms.setDisplayModel(displayModel);
                
        RDFServiceFactory rdfServiceFactory = RDFServiceUtils.getRDFServiceFactory(ctx);
        RDFService rdfService = rdfServiceFactory.getRDFService();
        Dataset dataset = new RDFServiceDataset(rdfService);
        setStartupDataset(dataset, ctx);
        
        // ABox assertions
        baseOms.setABoxModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, dataset.getNamedModel(JenaDataSourceSetupBase.JENA_DB_MODEL)));
        
        // ABox inferences
        inferenceOms.setABoxModel(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, dataset.getNamedModel(JenaDataSourceSetupBase.JENA_INF_MODEL)));
        
        // TBox assertions
        try {
            Model tboxAssertionsDB = dataset.getNamedModel(
                    JENA_TBOX_ASSERTIONS_MODEL);
            OntModel tboxAssertions = ModelFactory.createOntologyModel(
                    MEM_ONT_MODEL_SPEC);
            
            if (tboxAssertionsDB != null) {
                long startTime = System.currentTimeMillis();
                log.info("Copying cached tbox assertions into memory");
                tboxAssertions.add(tboxAssertionsDB);
                log.info((System.currentTimeMillis() - startTime)/ 1000 + " seconds to load tbox assertions");
                tboxAssertions.getBaseModel().register(new ModelSynchronizer(tboxAssertionsDB));
            }
                        
            baseOms.setTBoxModel(tboxAssertions);
        } catch (Throwable e) {
            log.error("Unable to load tbox assertion cache from DB", e);
            throw new RuntimeException(e);
        }
        
        // TBox inferences
        try {
            Model tboxInferencesDB = dataset.getNamedModel(JENA_TBOX_INF_MODEL);
            OntModel tboxInferences = ModelFactory.createOntologyModel(
                    MEM_ONT_MODEL_SPEC);
            
            if (tboxInferencesDB != null) {
                long startTime = System.currentTimeMillis();
                log.info(
                        "Copying cached tbox inferences into memory");
                tboxInferences.add(tboxInferencesDB);
                System.out.println((System.currentTimeMillis() - startTime)
                        / 1000 + " seconds to load tbox inferences");
                
                tboxInferences.getBaseModel().register(new ModelSynchronizer(
                		tboxInferencesDB));
            }
            inferenceOms.setTBoxModel(tboxInferences);
        } catch (Throwable e) {
            log.error("Unable to load tbox inference cache from DB", e);
            throw new RuntimeException(e);
        }
                              
        // union ABox
        Model m = ModelFactory.createUnion(
                baseOms.getABoxModel(), inferenceOms.getABoxModel());
        m = ModelFactory.createModelForGraph(
                new SpecialBulkUpdateHandlerGraph(
                        m.getGraph(), 
                        baseOms.getABoxModel().getGraph().getBulkUpdateHandler()));
        OntModel unionABoxModel = ModelFactory.createOntologyModel(
                MEM_ONT_MODEL_SPEC, m);
        unionOms.setABoxModel(unionABoxModel);
        
        // union TBox
        m = ModelFactory.createUnion(baseOms.getTBoxModel(), inferenceOms.getTBoxModel());
        m = ModelFactory.createModelForGraph(
                new SpecialBulkUpdateHandlerGraph(
                        m.getGraph(), 
                        baseOms.getTBoxModel().getGraph().getBulkUpdateHandler()));
        OntModel unionTBoxModel = ModelFactory.createOntologyModel(
                MEM_ONT_MODEL_SPEC, m);       
        unionOms.setTBoxModel(unionTBoxModel);
                  
        
        // Application metadata model is cached in memory.
        try {
            
            Model applicationMetadataModelDB = dataset.getNamedModel(
                    JENA_APPLICATION_METADATA_MODEL);
            OntModel applicationMetadataModel = 
                    ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
            
            long startTime = System.currentTimeMillis();
            System.out.println(
                    "Copying cached application metadata model into memory");
            applicationMetadataModel.add(applicationMetadataModelDB);
            System.out.println((System.currentTimeMillis() - startTime) 
                    / 1000 + " seconds to load application metadata model " +
                    "assertions of size " + applicationMetadataModel.size());
            applicationMetadataModel.getBaseModel().register(
                    new ModelSynchronizer(applicationMetadataModelDB));
            
            if (applicationMetadataModel.size()== 0 /* isFirstStartup() */) {
                JenaDataSourceSetupBase.thisIsFirstStartup();
                applicationMetadataModel.add(
                        InitialJenaModelUtils.loadInitialModel(
                                ctx, getDefaultNamespace(ctx)));
            }
            
            baseOms.setApplicationMetadataModel(applicationMetadataModel);
            inferenceOms.setApplicationMetadataModel(
                    baseOms.getApplicationMetadataModel());
            unionOms.setApplicationMetadataModel(
                    baseOms.getApplicationMetadataModel());
            
        } catch (Throwable e) {
            log.error("Unable to load application metadata model cache from DB"
                    , e);
            throw new RuntimeException(e);
        }
        
        checkForNamespaceMismatch( baseOms.getApplicationMetadataModel(), ctx );
        
        if (isFirstStartup()) {
            loadDataFromFilesystem(baseOms, ctx);
        }
        
        log.info("Setting up union models and DAO factories");
        
        // create TBox + ABox union models and set up webapp DAO factories
        Model baseDynamicUnion = ModelFactory.createUnion(baseOms.getABoxModel(), 
                baseOms.getTBoxModel());
        baseDynamicUnion = ModelFactory.createModelForGraph(
                new SpecialBulkUpdateHandlerGraph(
                        baseDynamicUnion.getGraph(), 
                        baseOms.getABoxModel().getGraph().getBulkUpdateHandler()) );
        OntModel baseUnion = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM, baseDynamicUnion);
        baseOms.setFullModel(baseUnion);
        ModelContext.setBaseOntModel(baseOms.getFullModel(), ctx);
        WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
        config.setDefaultNamespace(getDefaultNamespace(ctx));
        WebappDaoFactory baseWadf = new WebappDaoFactorySDB(
                rdfService, baseOms, config,
                WebappDaoFactorySDB.SDBDatasetMode.ASSERTIONS_ONLY);
        ctx.setAttribute("assertionsWebappDaoFactory",baseWadf);
        
        OntModel inferenceUnion = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM,
                ModelFactory.createUnion(
                        inferenceOms.getABoxModel(), 
                        inferenceOms.getTBoxModel()));
        inferenceOms.setFullModel(inferenceUnion);
        ModelContext.setInferenceOntModel(inferenceOms.getFullModel(), ctx);
        WebappDaoFactory infWadf = new WebappDaoFactorySDB(
                rdfService, inferenceOms, config, 
                WebappDaoFactorySDB.SDBDatasetMode.INFERENCES_ONLY);
        ctx.setAttribute("deductionsWebappDaoFactory", infWadf);
        
        OntModel masterUnion = ModelFactory.createOntologyModel(
                DB_ONT_MODEL_SPEC, dataset.getDefaultModel());
        unionOms.setFullModel(masterUnion);
        ctx.setAttribute("jenaOntModel", masterUnion);  
        WebappDaoFactory wadf = new WebappDaoFactorySDB(
                rdfService, unionOms, config);
        ctx.setAttribute("webappDaoFactory",wadf);

        ModelContext.setOntModelSelector(unionOms, ctx);
        ModelContext.setUnionOntModelSelector(unionOms, ctx);          
                                           // assertions and inferences
        ModelContext.setBaseOntModelSelector(baseOms, ctx);            
                                           // assertions
        ModelContext.setInferenceOntModelSelector(inferenceOms, ctx);  
                                           // inferences       
        
        ctx.setAttribute("defaultNamespace", getDefaultNamespace(ctx));
        
        makeModelMakerFromConnectionProperties(TripleStoreType.RDB, ctx);
        VitroJenaModelMaker vjmm = getVitroJenaModelMaker();
        setVitroJenaModelMaker(vjmm, ctx);
        makeModelMakerFromConnectionProperties(TripleStoreType.SDB, ctx);
        RDFServiceModelMaker vsmm = new RDFServiceModelMaker(rdfServiceFactory);
        setVitroJenaSDBModelMaker(vsmm, ctx);
                
        //bdc34: I have no reason for vsmm vs vjmm.  
        //I don't know what are the implications of this choice.        
        setVitroModelSource( new VitroModelSource(vsmm,ctx), ctx);
        
        log.info("Model makers set up");
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

