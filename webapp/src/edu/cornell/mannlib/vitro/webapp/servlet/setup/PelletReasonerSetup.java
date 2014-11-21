/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.pellet.PelletListener;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;

/**
 * Start the Pellet reasoner on the TBox.
 */
public class PelletReasonerSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(PelletReasonerSetup.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext ctx = sce.getServletContext();
		StartupStatus ss = StartupStatus.getBean(ctx);

		ContextModelAccess contextModels = ModelAccess.on(ctx);
		OntModel tboxAssertionsModel = contextModels
				.getOntModel(TBOX_ASSERTIONS);
		OntModel tboxInferencesModel = contextModels
				.getOntModel(TBOX_INFERENCES);
		OntModel tboxUnionModel = contextModels.getOntModel(TBOX_UNION);
		WebappDaoFactory wadf = contextModels.getWebappDaoFactory();

		if (!tboxAssertionsModel.getProfile().NAMESPACE()
				.equals(OWL.NAMESPACE.getNameSpace())) {
			ss.fatal(this, "Not connecting Pellet reasoner "
					+ "- the TBox assertions model is not an OWL model");
			return;
		}

		// Set various Pellet options for incremental consistency checking, etc.
		// PelletOptions.DL_SAFE_RULES = true;
		// PelletOptions.USE_COMPLETION_QUEUE = true;
		// PelletOptions.USE_TRACING = true;
		// PelletOptions.TRACK_BRANCH_EFFECTS = true;
		// PelletOptions.USE_INCREMENTAL_CONSISTENCY = true;
		// PelletOptions.USE_INCREMENTAL_DELETION = true;

		PelletListener pelletListener = new PelletListener(tboxUnionModel,
				tboxAssertionsModel, tboxInferencesModel,
				ReasonerConfiguration.DEFAULT);
		sce.getServletContext().setAttribute("pelletListener", pelletListener);
		sce.getServletContext().setAttribute("pelletOntModel",
				pelletListener.getPelletModel());

		if (wadf instanceof WebappDaoFactoryJena) {
			((WebappDaoFactoryJena) wadf).setPelletListener(pelletListener);
		}

		ss.info(this, "Pellet reasoner connected for the TBox");

		waitForTBoxReasoning(sce);
	}

	public static void waitForTBoxReasoning(ServletContextEvent sce) {
		PelletListener pelletListener = (PelletListener) sce
				.getServletContext().getAttribute("pelletListener");
		if (pelletListener == null) {
			return;
		}
		int sleeps = 0;
		// sleep at least once to make sure the TBox reasoning gets started
		while ((0 == sleeps)
				|| ((sleeps < 1000) && pelletListener.isReasoning())) {
			if (((sleeps - 1) % 10) == 0) { // print message at 10 second
											// intervals
				log.info("Waiting for initial TBox reasoning to complete");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// This should never happen.
				e.printStackTrace();
			}
			sleeps++;
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// Nothing to tear down
	}

}
