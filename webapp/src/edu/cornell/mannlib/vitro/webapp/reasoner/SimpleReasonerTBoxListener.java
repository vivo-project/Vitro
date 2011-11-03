/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;


/**
 * Route notification of changes to TBox to the incremental ABox reasoner.
 * The incremental ABox reasoner handles only subclass, superclass
 * and equivalent class axioms.
 *  
 */

public class SimpleReasonerTBoxListener extends StatementListener {

	private SimpleReasoner simpleReasoner = null;

	public SimpleReasonerTBoxListener(SimpleReasoner simpleReasoner) {
		this.simpleReasoner = simpleReasoner;
	}
	
	@Override
	public void addedStatement(Statement stmt) {
		//simpleReasoner.startBatchMode();
		simpleReasoner.addedTBoxStatement(stmt);
		//simpleReasoner.endBatchMode();
	}
	
	@Override
	public void removedStatement(Statement stmt) {
		//simpleReasoner.startBatchMode();
		simpleReasoner.removedTBoxStatement(stmt);
		//simpleReasoner.endBatchMode();
	}
}