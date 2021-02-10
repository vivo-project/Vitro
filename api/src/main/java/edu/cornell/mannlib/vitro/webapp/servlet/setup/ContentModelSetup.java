/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS_FIRSTTIME_BACKUP;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS_FIRSTTIME_BACKUP;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA_FIRSTTIME_BACKUP;

import java.util.ArrayList;
import java.util.List;
import java.io.StringWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.RDFNode;

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

			// backup copy from firsttime files
			OntModel applicationMetadataModelFirsttime = models.getOntModel(APPLICATION_METADATA_FIRSTTIME_BACKUP);
			applicationMetadataModelFirsttime.add(applicationMetadataModel);
            
		} else {
			// check if some of the firsttime files have changed since the first start up and
			// if they changed, apply these changes to the user models
			applyFirstTimeChanges(ctx);

        	checkForNamespaceMismatch( applicationMetadataModel, ctx );
		}

        OntModel baseABoxModel = models.getOntModel(ABOX_ASSERTIONS);
        if (firstTimeStartup) {
            RDFFilesLoader.loadFirstTimeFiles(ctx, "abox", baseABoxModel, true);

            // backup copy from firsttime files
            OntModel baseABoxModelFirsttime = models.getOntModel(ABOX_ASSERTIONS_FIRSTTIME_BACKUP);
            baseABoxModelFirsttime.add(baseABoxModel);
        }
        RDFFilesLoader.loadEveryTimeFiles(ctx, "abox", baseABoxModel);

        OntModel baseTBoxModel = models.getOntModel(TBOX_ASSERTIONS);
        if (firstTimeStartup) {
            RDFFilesLoader.loadFirstTimeFiles(ctx, "tbox", baseTBoxModel, true);

            // backup copy from firsttime files
            OntModel baseTBoxModelFirsttime = models.getOntModel(TBOX_ASSERTIONS_FIRSTTIME_BACKUP);
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

    /*
     * Check if the firsttime files have changed since the firsttime startup for all ContentModels,
     * if so, then apply the changes but not overwrite the whole user model
     */
    private void applyFirstTimeChanges(ServletContext ctx) {

        applyFirstTimeChanges(ctx, "applicationMetadata", APPLICATION_METADATA_FIRSTTIME_BACKUP, APPLICATION_METADATA);

        applyFirstTimeChanges(ctx, "abox", ABOX_ASSERTIONS_FIRSTTIME_BACKUP, ABOX_ASSERTIONS);

        applyFirstTimeChanges(ctx, "tbox", TBOX_ASSERTIONS_FIRSTTIME_BACKUP, TBOX_ASSERTIONS);
    }


    /*
     * Check if the firsttime files have changed since the firsttime startup for one ContentModel,
     * if so, then apply the changes but not overwrite the whole user model
     */
    private void applyFirstTimeChanges(ServletContext ctx, String modelPath, String firsttimeBackupModelUri, String userModelUri) {
        log.info("Reload firsttime files on start-up if changed: '" + modelPath +"', URI: '" +userModelUri+ "'");
        ContextModelAccess models = ModelAccess.on(ctx);
        OntModel firsttimeBackupModel = models.getOntModel(firsttimeBackupModelUri);

        // compare firsttime files with configuration models
        log.debug("compare firsttime files with configuration models (backup from first start) for " + modelPath);
        OntModel firsttimeFilesModel = VitroModelFactory.createOntologyModel();
        RDFFilesLoader.loadFirstTimeFiles(ctx, modelPath, firsttimeFilesModel, true);

        // special initialization for application metadata model
        if (firsttimeBackupModelUri.equals(APPLICATION_METADATA_FIRSTTIME_BACKUP)) {
            setPortalUriOnFirstTime(firsttimeFilesModel, ctx);
        }

        if ( firsttimeBackupModel.isIsomorphicWith(firsttimeFilesModel) ) {
            log.debug("They are the same, so do nothing: '" + modelPath + "'");
        } else {
            log.debug("They differ: '" + modelPath + "', compare values in configuration models with user's triplestore");     
            OntModel userModel = models.getOntModel(userModelUri);

            // double check the statements (blank notes, etc.) and apply the changes
            boolean updatedFiles = applyChanges(firsttimeBackupModel, firsttimeFilesModel, userModel, modelPath);
            if (updatedFiles) log.info("The model was updated, " + modelPath);
        }
    }

    /*
	 * This method is designed to compare configuration models (baseModel) with firsttime files (newModel):
	 * if they are the same, stopFirstTime
	 * else, if they differ, compare values in configuration models (baseModel) with user's triplestore
	 *     if they are the same, update user's triplestore with value in new firsttime files
	 *     else, if they differ, leave user's triplestore statement alone
	 * finally, overwrite the configuration models with content of the updated firstime files
     * 
     * @param baseModel The backup firsttime model (from the first startup)
     * @param newModel The current state of the firsttime files in the directory
     * @param userModel The current state of the user model
     * @param modelIdString Just an string for the output for better debugging (tbox, abox, applicationMetadata)
     */
    private boolean applyChanges(Model baseModel, Model newModel, Model userModel, String modelIdString) {
        boolean updatedFiles = false;
        StringWriter out = new StringWriter();
        StringWriter out2 = new StringWriter();
        Model difOldNew = baseModel.difference(newModel);
        Model difNewOld = newModel.difference(baseModel);

        // special case for "rootTab" triple, do not need an update (is it still used in general? if not remove this case)
        if(modelIdString.equals("applicationMetadata")) {
            
            Property p = userModel.createProperty("http://vitro.mannlib.cornell.edu/ns/vitro/0.7#", "rootTab");
            difOldNew.removeAll(null, p, null);
            difNewOld.removeAll(null, p, null);
        }

        if (difOldNew.isEmpty() && difNewOld.isEmpty()) {
            // if there is no difference, nothing needs to be done
            log.debug("For the " + modelIdString + " model, there is no difference in both directions. So do nothing.");
        } else {
            // if there is a difference, we need to remove the triples in difOldNew and 
            // add the triples in difNewOld to the back up firsttime model

            if (!difOldNew.isEmpty()) {
                difOldNew.write(out, "TTL"); 
                log.debug("Difference for " + modelIdString + " (old -> new), these triples should be removed: " + out);

                // Check if the UI-changes Overlap with the changes made in the fristtime-files 
                checkUiChangesOverlapWithFileChanges(baseModel, userModel, difOldNew);

                // before we remove the triples, we need to compare values in back up firsttime with user's triplestore
                // if the triples which should be removed are still in user´s triplestore, remove them
                if (userModel.containsAny(difOldNew)) {
                    log.debug("Some of these triples are in the user triples store, so they will be removed now");
                    userModel.remove(difOldNew);
                    updatedFiles = true;
                }

                // remove the triples from the back up firsttime model for the next check
                baseModel.remove(difOldNew);

            }
            if (!difNewOld.isEmpty()) {
                difNewOld.write(out2, "TTL"); 
                log.debug("Difference for " + modelIdString + " (new -> old), these triples should be added: " + out2);

                // Check if the UI-changes Overlap with the changes made in the fristtime-files 
                checkUiChangesOverlapWithFileChanges(baseModel, userModel, difNewOld);

                // before we add the triples, we need to compare values in back up firsttime with user's triplestore
                // if the triples which should be added are not already in user´s triplestore, add them
                if (!userModel.containsAll(difNewOld)) {
                    log.debug("Some of these triples are not in the user triples store, so they will be added now");
                    // but only the triples that are no already there
                    Model tmp = difNewOld.difference(userModel);
                    userModel.add(tmp);
                    updatedFiles = true;
                }

                // add the triples from the back up firsttime model for the next check
                baseModel.add(difNewOld);
            }
        }
        return updatedFiles;
    }

    /**
     * Check if the UI-changes Overlap with the changes made in the fristtime-files, if they overlap these changes are not applied to the user-model (UI)
     * 
     * @param baseModel firsttime backup model
     * @param userModel current state in the system (user/UI-model)
     * @param changesModel the changes between firsttime-files and firttime-backup
     */
    private void checkUiChangesOverlapWithFileChanges(Model baseModel, Model userModel, Model changesModel) {
        log.debug("Beginn check if subtractions from Backup-firsttime model to current state of firsttime-files were changed in user-model (via UI)");
        Model changesUserModel = userModel.difference(baseModel);
        List<Statement> changedInUIandFileStatements = new ArrayList<Statement>();

        if(!changesUserModel.isEmpty())
        {

            StringWriter out3 = new StringWriter();
            changesUserModel.write(out3, "TTL"); 
            log.debug("There were changes in the user-model via UI which have also changed in the firsttime files, the following triples will not be updated");

            // iterate all statements and check if the ones which should be removed were not changed via the UI
            StmtIterator iter = changesUserModel.listStatements();
            while (iter.hasNext()) {
                Statement stmt      = iter.nextStatement();  // get next statement
                Resource  subject   = stmt.getSubject();     // get the subject
                Property predicate  = stmt.getPredicate();    // get the predicate
                RDFNode   object    = stmt.getObject();      // get the object			

                StmtIterator iter2 = changesModel.listStatements();

                while (iter2.hasNext()) {
                    Statement stmt2      = iter2.nextStatement();  // get next statement
                    Resource  subject2   = stmt2.getSubject();     // get the subject
                    Property predicate2  = stmt2.getPredicate();    // get the predicate
                    RDFNode   object2    = stmt2.getObject();      // get the object

                    // if subject and predicate are equal but the object differs and the language tag is the same, do not update these triples
                    // this case indicates an change in the UI, which should not be overwriten from the firsttime files
                    if(subject.equals(subject2) && predicate.equals(predicate2) && !object.equals(object2) ) {
                        // if object is an literal, check the language tag
                        if (object.isLiteral() && object2.isLiteral()) {
                            // if the langauge tag is the same, remove this triple from the update list
                            if(object.asLiteral().getLanguage().equals(object2.asLiteral().getLanguage())) {
                                log.debug("This two triples changed UI and files: \n UI: " + stmt + " \n file: " +stmt2);
                                changedInUIandFileStatements.add(stmt2);
                            }
                        } else {
                            log.debug("This two triples changed UI and files: \n UI: " + stmt + " \n file: " +stmt2);
                            changedInUIandFileStatements.add(stmt2);
                        }
                    }
                }
            }
            // remove triples which were changed in the user model (UI) from the list
            changesModel.remove(changedInUIandFileStatements);
            } else {
                log.debug("There were no changes in the user-model via UI compared to the backup-firsttime-model");
        }
    }

    /* ===================================================================== */

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to do.
    }

 }

