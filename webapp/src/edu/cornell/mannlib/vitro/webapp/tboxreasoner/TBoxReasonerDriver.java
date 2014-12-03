/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerStatus;


/**
 * What calls can the ConfiguredReasonerListener make to drive the TBox
 * reasoner?
 */
public interface TBoxReasonerDriver {

	void runSynchronizer(TBoxChanges changeSet);
	
	TBoxReasonerStatus getStatus();
}
