/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary.DISPLAY_ONT_MODEL;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelectorImpl;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase.TripleStoreType;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.utils.jena.InitialJenaModelUtils;

/**
 * Setup the ABox, TBox, inference and Union models.  
 * Also setup the OntModelSelectors. 
 */
public class ModelSetup extends JenaDataSourceSetupBase 
implements javax.servlet.ServletContextListener {
    private static final Log log = LogFactory.getLog(ModelSetup.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {        
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        
        BasicDataSource bds = getApplicationDataSource(ctx);
        if( bds == null ){
            ss.fatal(this, "A DataSource must be setup before ModelSetup "+
                    "is run. Make sure that JenaPersistentDataSourceSetup runs before "+
                    "ModelSetup.");
            return;
        }
                
        setupModels(ctx,ss,bds);        
    }

    private void setupModels(ServletContext ctx, StartupStatus ss, BasicDataSource bds){
        log.info("Setting up model makers and union models");
        
        ///////////////////////////////////////////////////////////////
        //set up the OntModelSelectors
        
        OntModelSelectorImpl baseOms = new OntModelSelectorImpl();     
        OntModelSelectorImpl inferenceOms = new OntModelSelectorImpl();       
        OntModelSelectorImpl unionOms = new OntModelSelectorImpl();
        
        //Put OntModelSelectorImpl objs into the context
        ModelContext.setOntModelSelector(unionOms, ctx);
        ModelContext.setUnionOntModelSelector(unionOms, ctx);          
                                           // assertions and inferences
        ModelContext.setBaseOntModelSelector(baseOms, ctx);            
                                           // assertions
        ModelContext.setInferenceOntModelSelector(inferenceOms, ctx);  
                                           // inferences    
        
        //add userAccountsModel to OntModelSelectors
        OntModel userAccountsModel = ontModelFromContextAttribute(
                ctx, "userAccountsOntModel");     
        baseOms.setUserAccountsModel(userAccountsModel);
        inferenceOms.setUserAccountsModel(userAccountsModel);
        unionOms.setUserAccountsModel(userAccountsModel);       
        
        //add display to OntModelSelectors
        OntModel displayModel = ontModelFromContextAttribute(
                ctx,DISPLAY_ONT_MODEL);
        baseOms.setDisplayModel(displayModel);
        inferenceOms.setDisplayModel(displayModel);
        unionOms.setDisplayModel(displayModel);
        
        // The code below, which sets up the OntModelSelectors, controls whether 
        // each model is maintained in memory, in the DB, or both while the 
        // application is running.         
                
        // Populate the three OntModelSelectors (BaseOntModel = assertions, 
        // InferenceOntModel = inferences and JenaOntModel = union of assertions
        // and inferences) with the post-SDB-conversion models.

        // ABox assertions
        Model aboxAssertions = makeDBModel(
                bds, JenaDataSourceSetupBase.JENA_DB_MODEL, DB_ONT_MODEL_SPEC, 
                TripleStoreType.SDB, ctx);
        Model listenableAboxAssertions = ModelFactory.createUnion(
                aboxAssertions, ModelFactory.createDefaultModel());
        baseOms.setABoxModel(
                ModelFactory.createOntologyModel(
                        OntModelSpec.OWL_MEM, listenableAboxAssertions));
        
        // ABox inferences
        Model aboxInferences = makeDBModel(
                bds, JenaDataSourceSetupBase.JENA_INF_MODEL, DB_ONT_MODEL_SPEC, 
                TripleStoreType.SDB, ctx);
        Model listenableAboxInferences = ModelFactory.createUnion(
                aboxInferences, ModelFactory.createDefaultModel());
        inferenceOms.setABoxModel(ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM, listenableAboxInferences));
      
        
        // Since the TBox models are in memory, they do not have timeout issues 
        // like the like the ABox models do (and so don't need the extra step 
        // to make them listenable.)
        
        // TBox assertions
        try {
            Model tboxAssertionsDB = makeDBModel(
                    bds, JENA_TBOX_ASSERTIONS_MODEL, DB_ONT_MODEL_SPEC, 
                    TripleStoreType.SDB, ctx);
            OntModel tboxAssertions = ModelFactory.createOntologyModel(
                    MEM_ONT_MODEL_SPEC);
            
            if (tboxAssertionsDB != null) {
                long startTime = System.currentTimeMillis();
                System.out.println(
                        "Copying cached tbox assertions into memory");
                tboxAssertions.add(tboxAssertionsDB);
                System.out.println((System.currentTimeMillis() - startTime)
                        / 1000 + " seconds to load tbox assertions");
            }

            tboxAssertions.getBaseModel().register(new ModelSynchronizer(
                    tboxAssertionsDB));
            baseOms.setTBoxModel(tboxAssertions);
        } catch (Throwable e) {
            log.error("Unable to load tbox assertion cache from DB", e);
        }
        
        // TBox inferences
        try {
            Model tboxInferencesDB = makeDBModel(
                    bds, JENA_TBOX_INF_MODEL, DB_ONT_MODEL_SPEC, 
                    TripleStoreType.SDB, ctx);
            OntModel tboxInferences = ModelFactory.createOntologyModel(
                    MEM_ONT_MODEL_SPEC);
            
            if (tboxInferencesDB != null) {
                long startTime = System.currentTimeMillis();
                System.out.println(
                        "Copying cached tbox inferences into memory");
                tboxInferences.add(tboxInferencesDB);
                System.out.println((System.currentTimeMillis() - startTime)
                        / 1000 + " seconds to load tbox inferences");
            }
            
            tboxInferences.getBaseModel().register(new ModelSynchronizer(
                    tboxInferencesDB));
            inferenceOms.setTBoxModel(tboxInferences);
        } catch (Throwable e) {
            log.error("Unable to load tbox inference cache from DB", e);
        }
                              
        // union ABox
        OntModel unionABoxModel = ModelFactory.createOntologyModel(
                MEM_ONT_MODEL_SPEC,ModelFactory.createUnion(
                        baseOms.getABoxModel(), inferenceOms.getABoxModel()));
        unionOms.setABoxModel(unionABoxModel);
        
        // union TBox
        OntModel unionTBoxModel = ModelFactory.createOntologyModel(
                MEM_ONT_MODEL_SPEC,ModelFactory.createUnion(
                        baseOms.getTBoxModel(), inferenceOms.getTBoxModel()));       
        unionOms.setTBoxModel(unionTBoxModel);
                  
        
        // Application metadata model is cached in memory.
        try {
            
            Model applicationMetadataModelDB = makeDBModel(
                    bds, JENA_APPLICATION_METADATA_MODEL, DB_ONT_MODEL_SPEC, 
                    TripleStoreType.SDB, ctx);
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
            
            if (isFirstStartup()) {
                applicationMetadataModel.add(
                        InitialJenaModelUtils.loadInitialModel(
                                ctx, getDefaultNamespace(ctx)));
                
            } else if (applicationMetadataModelDB.size() == 0) {
                repairAppMetadataModel(
                        applicationMetadataModel, aboxAssertions, 
                        aboxInferences);
            }
            
            baseOms.setApplicationMetadataModel(applicationMetadataModel);
            inferenceOms.setApplicationMetadataModel(
                    baseOms.getApplicationMetadataModel());
            unionOms.setApplicationMetadataModel(
                    baseOms.getApplicationMetadataModel());
            
        } catch (Throwable e) {
            log.error("Unable to load application metadata model cache from DB"
                    , e);
        }
        
        // create TBox + ABox union models
        OntModel baseUnion = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM,
                ModelFactory.createUnion(baseOms.getABoxModel(), 
                        baseOms.getTBoxModel()));
        baseOms.setFullModel(baseUnion);
        ModelContext.setBaseOntModel(baseOms.getFullModel(), ctx);
        
        log.info("Model makers and union set up");
    }    

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // nothing to do.
        
    }

}
