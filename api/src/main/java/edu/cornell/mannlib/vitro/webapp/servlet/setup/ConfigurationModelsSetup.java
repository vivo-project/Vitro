/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY_DISPLAY;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.DISPLAY_TBOX;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;

/**
 * Set up the models that use the CONFIGURATION RDFService. They are all mapped
 * to memory-based models.
 */
public class ConfigurationModelsSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(ConfigurationModelsSetup.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		try {
			setupModel(ctx, DISPLAY, "display");
			setupModel(ctx, DISPLAY_TBOX, "displayTbox");
			setupModel(ctx, DISPLAY_DISPLAY, "displayDisplay");
			setupModel(ctx, USER_ACCOUNTS, "auth");
			ss.info(this, "Set up the display models and the user accounts model.");
		} catch (Exception e) {
			ss.fatal(this, e.getMessage(), e.getCause());
		}
	}

	private void setupModel(ServletContext ctx, String modelUri, String modelPath) {
		try {
			OntModel ontModel = ModelAccess.on(ctx).getOntModel(modelUri);
			if (ontModel.isEmpty()) {
				loadFirstTimeFiles(ctx, modelPath, ontModel);
				// backup firsttime files
				OntModel baseModelFirsttime = ModelAccess.on(ctx).getOntModel(modelUri + "FirsttimeBackup");
				baseModelFirsttime.add(ontModel);
			} else {
				// Check if the firsttime files have changed since the firsttime startup,
				// if so, then apply the changes but not overwrite the whole user model
				applyFirstTimeChanges(ctx, modelPath, modelUri, ontModel);
			}

			loadEveryTimeFiles(ctx, modelPath, ontModel);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create the '" + modelPath + "' model (" + modelUri + ").", e);
		}
	}

	private void loadFirstTimeFiles(ServletContext ctx, String modelPath, OntModel baseModel) {
		RDFFilesLoader.loadFirstTimeFiles(ctx, modelPath, baseModel, baseModel.isEmpty());
	}

	private void loadEveryTimeFiles(ServletContext ctx, String modelPath, OntModel memoryModel) {
		RDFFilesLoader.loadEveryTimeFiles(ctx, modelPath, memoryModel);
	}

	/*
	 * Check if the firsttime files have changed since the firsttime startup, if so,
	 * then apply the changes but not overwrite the whole user model
	 */
	private void applyFirstTimeChanges(ServletContext ctx, String modelPath, String modelUri, OntModel userModel) {

		log.info("Reload firsttime files on start-up if changed: '" + modelPath +"', URI: '" + modelUri + "'");
		boolean updatedFiles = false;

		// get configuration models from the firsttime start up (backup state)
		OntModel baseModelFirsttimeBackup = ModelAccess.on(ctx).getOntModel(modelUri + "FirsttimeBackup");

		// compare firsttime files with configuration models
		log.debug("compare firsttime files with configuration models (backup from first start) for " + modelPath);

		OntModel baseModelFirsttime = VitroModelFactory.createOntologyModel();
		RDFFilesLoader.loadFirstTimeFiles(ctx, modelPath, baseModelFirsttime, true);

		if (baseModelFirsttime.isIsomorphicWith(baseModelFirsttimeBackup)) {
			log.debug("They are the same, so do nothing: '" + modelPath + "'");
		} else {
			log.debug("They differ:" + modelPath + ", compare values in configuration models with user's triplestore");

			updatedFiles = applyChanges(baseModelFirsttimeBackup, baseModelFirsttime, userModel, modelPath);
			if (updatedFiles)
				log.info("The model was updated, " + modelPath);
		}
	}

	/*
	 * This method is designed to compare configuration models (baseModel) with firsttime files (newModel):
	 * if they are the same, stop
	 * else, if they differ, compare values in configuration models (baseModel) with user's triplestore
	 *     if they are the same, update user's triplestore with value in new firsttime files
	 *     else, if they differ, leave user's triplestore statement alone
	 * finally, overwrite the configuration models with content of the updated firstime files
	 * 
	 * @param baseModel The backup firsttime model (from the first startup)
	 * @param newModel The current state of the firsttime files in the directory
	 * @param userModel The current state of the user model
	 * @param modelIdString Just an string for the output for better debugging
	 * (display, displayTbox, displayDisplay, auth)
	 */
	private boolean applyChanges(Model baseModel, Model newModel, Model userModel, String modelIdString) {
		boolean updatedFiles = false;
		StringWriter out = new StringWriter();
		StringWriter out2 = new StringWriter();
		Model difOldNew = baseModel.difference(newModel);
		Model difNewOld = newModel.difference(baseModel);

		// remove special cases for display, problem with blank nodes
		if (modelIdString.equals("display")) {

			removeBlankTriples(difOldNew);
			removeBlankTriples(difNewOld);
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
				
				// remove the triples from the backup firsttime model for the next check
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
			removeBlankTriples(changesUserModel);

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
	
	/**
	 * Remove all triples where subject or object is blank (Anon)
	 */
	private void removeBlankTriples(Model model) {
		StmtIterator iter = model.listStatements();
		List<Statement> removeStatement = new ArrayList<Statement>();
		while (iter.hasNext()) {
			Statement stmt      = iter.nextStatement();  // get next statement
			Resource  subject   = stmt.getSubject();     // get the subject
			RDFNode   object    = stmt.getObject();      // get the object			

			if(subject.isAnon() || object.isAnon())
			{
				removeStatement.add(stmt);
			}
		}
		model.remove(removeStatement);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down.
	}

}
