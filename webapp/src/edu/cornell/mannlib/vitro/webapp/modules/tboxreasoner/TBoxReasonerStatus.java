/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner;

/**
 * What is the current state of the TBox reasoner?
 */
public interface TBoxReasonerStatus {
	/**
	 * Is reasoning in progress based on changes to the TBox?
	 */
	boolean isReasoning();
	
	/**
	 * Is the TBox free of inconsistency?
	 */
	boolean isConsistent();

	/**
	 * Did the reasoner fail in its most recent attempt?
	 */
	boolean isInErrorState();

	/**
	 * A description of the error state, or an empty string (never null).
	 */
	String getExplanation();

}
