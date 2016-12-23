/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Sets up the content models, OntModelSelectors and webapp DAO factories.
 * 
 * Why the firstTimeStartup flag? Because you can't ask a large SDB model
 * whether it is empty. SDB translates  this into a call to size(), which
 * in turn becomes find(null, null, null) and a count, and this gives an
 * OutOfMemoryError because it tries to read the entire model into memory.
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
        ContextModelAccess models = ModelAccess.on(ctx);
        boolean firstTimeStartup = false;
    	
    	Model applicationMetadataModel = models.getOntModel(APPLICATION_METADATA);
		if (applicationMetadataModel.isEmpty()) {
			firstTimeStartup = true;
        	initializeApplicationMetadata(ctx, applicationMetadataModel);
		} else {
        	checkForNamespaceMismatch( applicationMetadataModel, ctx );
		}

        OntModel baseABoxModel = models.getOntModel(ABOX_ASSERTIONS);
        if (firstTimeStartup) {
        	RDFFilesLoader.loadFirstTimeFiles("abox", baseABoxModel, true);
        }
        RDFFilesLoader.loadEveryTimeFiles("abox", baseABoxModel);
        
        OntModel baseTBoxModel = models.getOntModel(TBOX_ASSERTIONS);
        if (firstTimeStartup) {
        	RDFFilesLoader.loadFirstTimeFiles("tbox", baseTBoxModel, true);
        }
    	RDFFilesLoader.loadEveryTimeFiles("tbox", baseTBoxModel);
    }

	private long secondsSince(long startTime) {
		return (System.currentTimeMillis() - startTime) / 1000;
	}

    /* ===================================================================== */

	/**
	 * We need to read the RDF files and change the Portal from a blank node to
	 * one with a URI in the default namespace.
	 * 
	 * Do this before adding the data to the RDFService-backed model, to avoid
	 * warnings about editing a blank node.
	 */
	private void initializeApplicationMetadata(ServletContext ctx,
			Model applicationMetadataModel) {
		OntModel temporaryAMModel = VitroModelFactory.createOntologyModel();
    	RDFFilesLoader.loadFirstTimeFiles("applicationMetadata", temporaryAMModel, true);
    	setPortalUriOnFirstTime(temporaryAMModel, ctx);
    	applicationMetadataModel.add(temporaryAMModel);
	}

	/**
	 * If we are loading the application metadata for the first time, set the
	 * URI of the Portal based on the default namespace.
	 */
	private void setPortalUriOnFirstTime(Model model, ServletContext ctx) {
		// Only a single portal is permitted in the initialization data
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
    private void checkForNamespaceMismatch(Model model, ServletContext ctx) {
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

