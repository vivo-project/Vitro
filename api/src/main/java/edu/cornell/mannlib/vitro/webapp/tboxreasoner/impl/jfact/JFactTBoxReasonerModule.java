/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.jfact;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;

import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.Restriction;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ContextModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.modules.ComponentStartupStatus;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerModule;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerStatus;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl.BasicTBoxReasonerDriver;

/**
 * Configure a JFact reasoner on the TBox.
 * 
 * Create a JFactTBoxReasoner and pass it as the strategy to a
 * BasicTBoxReasonerDriver.
 */
public class JFactTBoxReasonerModule implements TBoxReasonerModule {
	private static final Log log = LogFactory
			.getLog(JFactTBoxReasonerModule.class);

	private JFactTBoxReasoner reasoner;
	private BasicTBoxReasonerDriver driver;

	@Override
	public void startup(Application application, ComponentStartupStatus ss) {
		ServletContext ctx = application.getServletContext();

		ContextModelAccess contextModels = ModelAccess.on(ctx);
		reasoner = new JFactTBoxReasoner();
		driver = new BasicTBoxReasonerDriver(
				contextModels.getOntModel(TBOX_ASSERTIONS), contextModels
						.getOntModel(TBOX_INFERENCES).getBaseModel(),
				contextModels.getOntModel(TBOX_UNION), reasoner,
				ReasonerConfiguration.DEFAULT);

		ss.info("JFact reasoner connected for the TBox");

		waitForTBoxReasoning();
	}

	@Override
	public TBoxReasonerStatus getStatus() {
		if (driver == null) {
			throw new IllegalStateException(
					"JFactTBoxReasonerModule has not been started.");
		}
		return driver.getStatus();
	}

	@Override
	public List<Restriction> listRestrictions() {
		if (reasoner == null) {
			throw new IllegalStateException(
					"JFactTBoxReasonerModule has not been started.");
		}
		return reasoner.listRestrictions();
	}

	@Override
	public void waitForTBoxReasoning() {
		int sleeps = 0;
		// sleep at least once to make sure the TBox reasoning gets started
		while ((0 == sleeps) || ((sleeps < 1000) && getStatus().isReasoning())) {
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
	public void shutdown(Application application) {
		driver.shutdown();
	}
}
