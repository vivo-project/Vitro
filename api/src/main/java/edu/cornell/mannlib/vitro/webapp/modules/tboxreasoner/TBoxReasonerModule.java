/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner;

import java.util.List;

import org.apache.jena.ontology.Restriction;

import edu.cornell.mannlib.vitro.webapp.modules.Application;

/**
 * A wrapper around the TBox reasoner
 */
public interface TBoxReasonerModule extends Application.Module {
	/**
	 * What is the TBox reasoner doing now?
	 */
	TBoxReasonerStatus getStatus();

	/**
	 * What restrictions are currently in the reasoner's internal model?
	 */
	List<Restriction> listRestrictions();

	/**
	 * Wait until the TBox reasoner becomes quiet.
	 */
	void waitForTBoxReasoning();
}
