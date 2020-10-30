/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS_FIRSTTIME;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS_FIRSTTIME;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA_FIRSTTIME;

import java.util.ArrayList;
import java.util.List;
import java.io.StringWriter;

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


        log.info("Checken ob Firsttime geladen werden soll..-.");


    	Model applicationMetadataModel = models.getOntModel(APPLICATION_METADATA);
		if (applicationMetadataModel.isEmpty()) {
            log.info("Ja firsttime sollte geladen werden");
			firstTimeStartup = true;
            initializeApplicationMetadata(ctx, applicationMetadataModel);
            
            // backup copy from firsttime files
            OntModel applicationMetadataModelFirsttime = models.getOntModel(APPLICATION_METADATA_FIRSTTIME);
            applicationMetadataModelFirsttime.add(applicationMetadataModel);
            
		} else {
            log.info("Nein firsttime sollte nicht geladen werden aber besser nochmal testen");

            // check if some of the firsttime files have changed since the first start up and
            // if they changes, apply these changes
            applyFirstTimeChanges(ctx);

        	checkForNamespaceMismatch( applicationMetadataModel, ctx );
		}

        OntModel baseABoxModel = models.getOntModel(ABOX_ASSERTIONS);
        if (firstTimeStartup) {
            RDFFilesLoader.loadFirstTimeFiles(ctx, "abox", baseABoxModel, true);

            // backup copy from firsttime files
            OntModel baseABoxModelFirsttime = models.getOntModel(ABOX_ASSERTIONS_FIRSTTIME);
            baseABoxModelFirsttime.add(baseABoxModel);
        }
        RDFFilesLoader.loadEveryTimeFiles(ctx, "abox", baseABoxModel);

        OntModel baseTBoxModel = models.getOntModel(TBOX_ASSERTIONS);
        if (firstTimeStartup) {
            RDFFilesLoader.loadFirstTimeFiles(ctx, "tbox", baseTBoxModel, true);

            // backup copy from firsttime files
            OntModel baseTBoxModelFirsttime = models.getOntModel(TBOX_ASSERTIONS_FIRSTTIME);
            baseTBoxModelFirsttime.add(baseTBoxModel);
        }
        RDFFilesLoader.loadEveryTimeFiles(ctx, "tbox", baseTBoxModel);
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
        RDFFilesLoader.loadFirstTimeFiles(ctx, "applicationMetadata", temporaryAMModel, true);
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

    private void applyFirstTimeChanges(ServletContext ctx) {
        // get configuration models from the firsttime start up (backup state)
        ContextModelAccess models = ModelAccess.on(ctx);
        OntModel applicationMetadataModel = models.getOntModel(APPLICATION_METADATA_FIRSTTIME);
        OntModel baseABoxModel = models.getOntModel(ABOX_ASSERTIONS_FIRSTTIME);
        OntModel baseTBoxModel = models.getOntModel(TBOX_ASSERTIONS_FIRSTTIME);

        // check if ApplicationMetadataModel is the same in file and configuration models
        OntModel testApplicationMetadataModel = VitroModelFactory.createOntologyModel();
        RDFFilesLoader.loadFirstTimeFiles(ctx, "applicationMetadata", testApplicationMetadataModel, true);
        setPortalUriOnFirstTime(testApplicationMetadataModel, ctx); // muss das gemacht werden?
    

        if ( applicationMetadataModel.isIsomorphicWith(testApplicationMetadataModel) ) {
            log.info("\n \n Test des ApplicationMetadataModel Models ergabt, das sie GLEICH sind");
        } else {
            log.info("\n \n Test des ApplicationMetadataModel Models ergabt, das sie UNTERSCHIEDLICH sind");

            log.info("ist ApplicationMetadataModel empty: " + applicationMetadataModel.isEmpty());
            log.info("ist testApplicationMetadataModel empty: " + testApplicationMetadataModel.isEmpty());
            
            OntModel userTriplestoreApplicationMetadataModel = models.getOntModel(APPLICATION_METADATA);

            // ToDo there is a special case with this triple: "<https://some.othernamespace.edu/vivo/individual/portal1> <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#rootTab> []  ."
            // it is always different but should no be changed and can maybe removed completely from the software

            applyChanges(applicationMetadataModel, testApplicationMetadataModel, userTriplestoreApplicationMetadataModel, "applicationMetadata");
        }

        // check if abox model is the same in file and configuration models
        OntModel testBaseABoxModel = VitroModelFactory.createOntologyModel();
        RDFFilesLoader.loadFirstTimeFiles(ctx, "abox", testBaseABoxModel, true);

        if ( baseABoxModel.isIsomorphicWith(testBaseABoxModel) ) {
            log.info("\n \n Test des ABox Models ergabt, das sie GLEICH sind");
        } else {
            log.info("\n \n Test des ABox Models ergabt, das sie UNTERSCHIEDLICH sind");

            log.info("ist ABox alt empty: " + baseABoxModel.isEmpty());
            log.info("ist ABox neu empty: " + testBaseABoxModel.isEmpty());
            
            OntModel userTriplestoreABoxModel = models.getOntModel(ABOX_ASSERTIONS);

            applyChanges(baseABoxModel, testBaseABoxModel, userTriplestoreABoxModel, "abox");
        }

        // check if tbox model is the same in file and configuration models
        OntModel testBaseTBoxModel = VitroModelFactory.createOntologyModel();
        RDFFilesLoader.loadFirstTimeFiles(ctx, "tbox", testBaseTBoxModel, true);

        if ( baseTBoxModel.isIsomorphicWith(testBaseTBoxModel) ) {
            log.info("\n \n Test des TBox Models ergabt, das sie GLEICH sind");
        } else {
            log.info("\n \n Test des TBox Models ergabt, das sie UNTERSCHIEDLICH sind");
            log.info("ist tbox alt empty: " + baseTBoxModel.isEmpty());
            log.info("ist tbox neu empty: " + testBaseTBoxModel.isEmpty());

            OntModel userTriplestoreTBoxModel = models.getOntModel(TBOX_ASSERTIONS);

            applyChanges(baseTBoxModel, testBaseTBoxModel, userTriplestoreTBoxModel, "tbox");
        }
    }

    private void applyChanges(Model baseModel, Model newModel, Model userModel, String modelIdString) {
            StringWriter out = new StringWriter();
            StringWriter out2 = new StringWriter();
            Model difOldNew = baseModel.difference(newModel);
            Model difNewOld = newModel.difference(baseModel);
            
            difOldNew.write(out, "TTL"); 
            log.info("Unterschied für " + modelIdString + " (alt zu neu): " + out.toString());
            difNewOld.write(out2, "TTL"); 
            log.info("Unterschied für " + modelIdString + " (neu zu alt): " + out2.toString());

            if (difOldNew.isEmpty() && difNewOld.isEmpty()) {
                // if there is no difference, nothing needs to be done
                log.info("Bei dem model " + modelIdString + " gibt es laut difference in beide Richtungen KEINEN Unterschied");
            } else {
                // if there is a difference, we need to remove the triples in difOldNew and 
                // add the triples in difNewOld to the back up firsttime model
                //OntModel userTriplestoreTBoxModel = models.getOntModel(TBOX_ASSERTIONS);

                if (!difOldNew.isEmpty()) {
                    // before we remove the triples, we need to compare values in back up firsttime with user's triplestore
                    log.info("Bei dem Model" + modelIdString + " gibt es laut difference EINEN Unterschied von alt zu neu");

                    // if the triples which should be removed are still in user´s triplestore, remove them
                    if (userModel.containsAny(difOldNew)) {
                        log.info("Im User Triplestore sind noch welche von den entfernten Triples (alt zu neu), diese werden jetzt entfernt");
                        userModel.remove(difOldNew);

                        // testing, remove me!!!!!!!!!!!
                        if (userModel.containsAny(difOldNew)) {
                            log.info("ERROR: user triple store enthält trotzdem noch diese Triple");
                        }
                    }

                    // remove the triples from the back up firsttime model for the next check
                    baseModel.remove(difOldNew);

                } else if (!difNewOld.isEmpty()) {
                    log.info("Bei dem Model " + modelIdString + " gibt es laut difference EINEN Unterschied von neu zu alt");

                    // if the triples which should be added are not already in user´s triplestore, add them
                    if (!userModel.containsAll(difNewOld)) {
                        log.info("Im User Triplestore sind noch welche von den neuen Triples nicht enthalten (alt zu neu), diese werden jetzt hinzugefügt");
                        // but only the triples that are no already there
                        Model tmp = difNewOld.difference(userModel);
                        userModel.add(tmp);

                        // testing, remove me
                        if (!userModel.containsAll(difNewOld)) {
                            log.info("ERROR: user triple store enthält trotzdem noch nicht alle von den Triplen");
                        }
                    }

                    // add the triples from the back up firsttime model for the next check
                    baseModel.add(difNewOld);
                }
            }
    }

    /* ===================================================================== */

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to do.
    }

 }

