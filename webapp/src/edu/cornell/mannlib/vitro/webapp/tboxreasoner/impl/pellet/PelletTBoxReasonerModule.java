/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.pellet;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;

import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerModule;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerStatus;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.BasicTBoxReasonerDriver;

/**
 * Configure a Pellet reasoner on the TBox.
 */
public class PelletTBoxReasonerModule implements TBoxReasonerModule {
	private static final Log log = LogFactory
			.getLog(PelletTBoxReasonerModule.class);

	private PelletTBoxReasoner reasoner;
	private BasicTBoxReasonerDriver driver;

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		ServletContext ctx = application.getServletContext();

		ContextModelAccess contextModels = ModelAccess.on(ctx);
		OntModel tboxAssertionsModel = contextModels
				.getOntModel(TBOX_ASSERTIONS);
		Model tboxInferencesModel = contextModels.getOntModel(TBOX_INFERENCES)
				.getBaseModel();
		OntModel tboxUnionModel = contextModels.getOntModel(TBOX_UNION);

		reasoner = new PelletTBoxReasoner(ReasonerConfiguration.DEFAULT);
		driver = new BasicTBoxReasonerDriver(tboxAssertionsModel,
				tboxInferencesModel, tboxUnionModel, reasoner,
				ReasonerConfiguration.DEFAULT);

		ss.info("Pellet reasoner connected for the TBox");

		waitForTBoxReasoning();
	}

	@Override
	public TBoxReasonerStatus getStatus() {
		if (driver == null) {
			throw new IllegalStateException(
					"PelletTBoxReasonerModule has not been started.");
		}
		return driver.getStatus();
	}

	@Override
	public List<Restriction> listRestrictions() {
		if (reasoner == null) {
			throw new IllegalStateException(
					"PelletTBoxReasonerModule has not been started.");
		}
		return reasoner.listRestrictions();
	}

	@Override
	public void shutdown(Application application) {
		driver.shutdown();
	}

	@Override
	public  void waitForTBoxReasoning() {
		int sleeps = 0;
		// sleep at least once to make sure the TBox reasoning gets started
		while ((0 == sleeps)
				|| ((sleeps < 1000) && getStatus().isReasoning())) {
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
}
