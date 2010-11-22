/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner.support;

import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;

/**
 * Route notification of changes to TBox to the incremental ABox reasoner.
 * The incremental ABox reasoner needs to handle only subclass, superclass
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
		
		simpleReasoner.addedTBoxStatement(stmt);

	}
	
	@Override
	public void removedStatement(Statement stmt) {
	
		simpleReasoner.removedTBoxStatement(stmt);
	}
	
}
