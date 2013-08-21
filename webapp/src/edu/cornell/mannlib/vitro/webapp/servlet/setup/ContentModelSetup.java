/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode.ASSERTIONS_ONLY;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.FactoryID;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryConfig;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SpecialBulkUpdateHandlerGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Sets up the content models, OntModelSelectors and webapp DAO factories.
 */
public class ContentModelSetup extends JenaDataSourceSetupBase 
        implements javax.servlet.ServletContextListener {
    
    private static final Log log = LogFactory.getLog(ContentModelSetup.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        StartupStatus ss = StartupStatus.getBean(ctx);
        
        long begin = System.currentTimeMillis();
        setUpJenaDataSource(ctx);
        ss.info(this, secondsSince(begin) + " seconds to set up models and DAO factories");  
    } 

    private void setUpJenaDataSource(ServletContext ctx) {
    	RDFServiceFactory rdfServiceFactory = RDFServiceUtils.getRDFServiceFactory(ctx);
    	RDFService rdfService = rdfServiceFactory.getRDFService();
    	Dataset dataset = new RDFServiceDataset(rdfService);
    	setStartupDataset(dataset, ctx);
    	
    	OntModel applicationMetadataModel = createdMemoryMappedModel(dataset, JENA_APPLICATION_METADATA_MODEL, "application metadata model");
		if (applicationMetadataModel.size()== 0) {
			JenaDataSourceSetupBase.thisIsFirstStartup();
		}

    	ModelAccess models = ModelAccess.on(ctx);
        OntModel baseABoxModel = createNamedModelFromDataset(dataset, JENA_DB_MODEL);
        OntModel inferenceABoxModel = createNamedModelFromDataset(dataset, JENA_INF_MODEL);
        OntModel baseTBoxModel = createdMemoryMappedModel(dataset, JENA_TBOX_ASSERTIONS_MODEL, "tbox assertions");
        OntModel inferenceTBoxModel = createdMemoryMappedModel(dataset, JENA_TBOX_INF_MODEL, "tbox inferences");
        OntModel unionABoxModel = createCombinedBulkUpdatingModel(baseABoxModel, inferenceABoxModel);
        OntModel unionTBoxModel = createCombinedBulkUpdatingModel(baseTBoxModel, inferenceTBoxModel);


        if (isFirstStartup()) {
        	RDFFilesLoader.loadFirstTimeFiles(ctx, "abox", baseABoxModel, true);
        	RDFFilesLoader.loadFirstTimeFiles(ctx, "tbox", baseTBoxModel, true);

        	RDFFilesLoader.loadFirstTimeFiles(ctx, "applicationMetadata", applicationMetadataModel, true);
        	setPortalUriOnFirstTime(applicationMetadataModel, ctx);
        } else {
        	checkForNamespaceMismatch( applicationMetadataModel, ctx );
        }
        
        log.info("Setting up full models");
        OntModel baseFullModel = createCombinedBulkUpdatingModel(baseABoxModel, baseTBoxModel);
        OntModel inferenceFullModel = createCombinedModel(inferenceABoxModel, inferenceTBoxModel);
        OntModel unionFullModel = ModelFactory.createOntologyModel(DB_ONT_MODEL_SPEC, dataset.getDefaultModel());

        models.setOntModel(ModelID.APPLICATION_METADATA, applicationMetadataModel);

        models.setOntModel(ModelID.BASE_ABOX, baseABoxModel);
        models.setOntModel(ModelID.BASE_TBOX, baseTBoxModel);
        models.setOntModel(ModelID.BASE_FULL, baseFullModel);
        models.setOntModel(ModelID.INFERRED_ABOX, inferenceABoxModel);
        models.setOntModel(ModelID.INFERRED_TBOX, inferenceTBoxModel);
        models.setOntModel(ModelID.INFERRED_FULL, inferenceFullModel);
        models.setOntModel(ModelID.UNION_ABOX, unionABoxModel);
        models.setOntModel(ModelID.UNION_TBOX, unionTBoxModel);
        models.setOntModel(ModelID.UNION_FULL, unionFullModel);
        
        
		log.info("Setting up DAO factories");
		
        WebappDaoFactoryConfig config = new WebappDaoFactoryConfig();
        config.setDefaultNamespace(getDefaultNamespace(ctx));
        
        OntModelSelector baseOms = models.getBaseOntModelSelector();
        WebappDaoFactory baseWadf = new WebappDaoFactorySDB(rdfService, baseOms, config, ASSERTIONS_ONLY);
        ModelAccess.on(ctx).setWebappDaoFactory(FactoryID.BASE, baseWadf);
        ModelAccess.on(ctx).setWebappDaoFactory(FactoryID.UNFILTERED_BASE, baseWadf);
        
        OntModelSelector unionOms = models.getUnionOntModelSelector();
        WebappDaoFactory wadf = new WebappDaoFactorySDB(rdfService, unionOms, config);
        ModelAccess.on(ctx).setWebappDaoFactory(FactoryID.UNION, wadf);
        ModelAccess.on(ctx).setWebappDaoFactory(FactoryID.UNFILTERED_UNION, wadf);

        log.info("Model makers set up");
        
        ctx.setAttribute("defaultNamespace", getDefaultNamespace(ctx));
    }

	private OntModel createNamedModelFromDataset(Dataset dataset, String name) {
    	return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, dataset.getNamedModel(name));
    }
    
	private OntModel createdMemoryMappedModel(Dataset dataset, String name, String label) {
		try {
			Model dbModel = dataset.getNamedModel(name);
			OntModel memoryModel = ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC);
			
			if (dbModel != null) {
			    long begin = System.currentTimeMillis();
				log.info("Copying cached " + label + " into memory");
			    memoryModel.add(dbModel);
			    log.info(secondsSince(begin) + " seconds to load " + label);
			    memoryModel.getBaseModel().register(new ModelSynchronizer(dbModel));
			}
			return memoryModel;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to load " + label + " from DB", e);
        }
	}

	private OntModel createCombinedModel(OntModel oneModel, OntModel otherModel) {
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, 
        		ModelFactory.createUnion(oneModel, otherModel));
	}

	private OntModel createCombinedBulkUpdatingModel(OntModel baseModel,
			OntModel otherModel) {
		BulkUpdateHandler bulkUpdateHandler = baseModel.getGraph().getBulkUpdateHandler();
		Graph unionGraph = ModelFactory.createUnion(baseModel, otherModel).getGraph();
		Model unionModel = ModelFactory.createModelForGraph(
				new SpecialBulkUpdateHandlerGraph(unionGraph, bulkUpdateHandler));
		return ModelFactory.createOntologyModel(MEM_ONT_MODEL_SPEC, unionModel);
	}

	private long secondsSince(long startTime) {
		return (System.currentTimeMillis() - startTime) / 1000;
	}

    /* ===================================================================== */

	/**
	 * If we are loading the application metadata for the first time, set the
	 * URI of the Portal based on the default namespace.
	 */
	private void setPortalUriOnFirstTime(OntModel model, ServletContext ctx) {
		// currently, only a single portal is permitted in the initialization
		// data
		Resource portalResource = null;
		ClosableIterator<Resource> portalResIt = model
				.listSubjectsWithProperty(RDF.type,
						model.getResource(VitroVocabulary.PORTAL));
		try {
			if (portalResIt.hasNext()) {
				Resource portalRes = portalResIt.next();
				if (portalRes.isAnon()) {
					portalResource = portalRes;
				}
			}
		} finally {
			portalResIt.close();
		}
		
		if (portalResource != null) {
			ResourceUtils.renameResource(portalResource, getDefaultNamespace(ctx) + "portal1");
		}
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
    
    private boolean noPortalForNamespace(List<Resource> portals, String expectedNamespace) {
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
            ResourceUtils.renameResource(portal, namespace + portal.getLocalName());
        } finally {
            model.leaveCriticalSection();
        }
    }
    
    /* ===================================================================== */
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to do.
    }    
 
 }

