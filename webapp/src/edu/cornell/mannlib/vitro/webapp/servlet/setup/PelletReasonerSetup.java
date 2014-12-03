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
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.BasicTBoxReasonerDriver;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasonerDriver;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasoner;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.pellet.PelletTBoxReasoner;

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
		Model tboxInferencesModel = contextModels
				.getOntModel(TBOX_INFERENCES).getBaseModel();
		OntModel tboxUnionModel = contextModels.getOntModel(TBOX_UNION);
		WebappDaoFactory wadf = contextModels.getWebappDaoFactory();

		TBoxReasoner reasoner = new PelletTBoxReasoner(
				ReasonerConfiguration.DEFAULT);
		TBoxReasonerDriver driver = new BasicTBoxReasonerDriver(
				tboxAssertionsModel, tboxInferencesModel, tboxUnionModel,
				reasoner, ReasonerConfiguration.DEFAULT);

		sce.getServletContext().setAttribute("tboxReasoner", driver);
		sce.getServletContext().setAttribute("tboxReasonerWrapper", reasoner);

		if (wadf instanceof WebappDaoFactoryJena) {
			((WebappDaoFactoryJena) wadf).setTBoxReasonerDriver(driver);
		}

		ss.info(this, "Pellet reasoner connected for the TBox");

		waitForTBoxReasoning(sce);
	}

	public static void waitForTBoxReasoning(ServletContextEvent sce) {
		TBoxReasonerDriver driver = (TBoxReasonerDriver) sce.getServletContext().getAttribute("tboxReasoner");
		if (driver == null) {
			return;
		}
		int sleeps = 0;
		// sleep at least once to make sure the TBox reasoning gets started
		while ((0 == sleeps)
				|| ((sleeps < 1000) && driver.isReasoning())) {
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
