/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import com.hp.hpl.jena.rdf.model.Statement;

/**
 * What calls can the ConfiguredReasonerListener make to drive the TBox reasoner?
 */
public interface TBoxReasonerDriver {
	void runSynchronizer();

	void addStatement(Statement stmt);

	void removeStatement(Statement stmt);

	void deleteDataProperty(Statement stmt);

	void deleteObjectProperty(Statement stmt);

}
