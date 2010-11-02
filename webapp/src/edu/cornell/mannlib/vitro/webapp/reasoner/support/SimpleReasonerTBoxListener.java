package edu.cornell.mannlib.vitro.webapp.reasoner.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static final Log log = LogFactory.getLog(SimpleReasonerTBoxListener.class);
	private SimpleReasoner simpleReasoner = null;


	public SimpleReasonerTBoxListener(SimpleReasoner simpleReasoner) {
		this.simpleReasoner = simpleReasoner;
	}
	
	@Override
	public void addedStatement(Statement stmt) {

		try {
			log.debug("stmt = " + stmt.toString());
            // call method in SimpleReasoner
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while adding incremental inferences: ", e);
		}
	}
	
	@Override
	public void removedStatement(Statement stmt) {
	
		try {
			log.debug("stmt = " + stmt.toString());
			// call method in SimpleReasoner
			
		} catch (Exception e) {
			// don't stop the edit if there's an exception
	
			log.error("Exception while retracting inferences: ", e);
		}
	}
	
}
