/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Safety Net:
 * 
 * Insure that the inferred TBox is the same as it was before we started messing
 * with things.
 * 
 * KLUGE -- this shouldn't go into production.
 * 
 * KLUGE -- in production, startup_listeners shouldn't mention this.
 */
public class TBoxReasonerSmokeTest implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		OntModel savedInferencesModel = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM);

		try (InputStream in = new FileInputStream(locateSavedInferencesFile())) {
			savedInferencesModel.read(in, null, "N3");
		} catch (IOException e) {
			ss.fatal(this, "Can't read saved inferences", e);
		}

		OntModel tboxInferencesModel = ModelAccess.on(sce.getServletContext())
				.getOntModel(ModelNames.TBOX_INFERENCES);

		if (savedInferencesModel.isIsomorphicWith(tboxInferencesModel)) {
			ss.info(this, "TBox inferences matches saved.");
		} else {
			ss.fatal(this, "TBox inferences does not match saved.");
		}
	}

	private File locateSavedInferencesFile() {
		String homeDirPath = ApplicationUtils.instance().getHomeDirectory()
				.getPath().toString();
		Path savedInferencesPath = Paths.get(homeDirPath, "rdf", "tbox",
				"savedInferences.n3");
		return savedInferencesPath.toFile();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to do
	}

}
