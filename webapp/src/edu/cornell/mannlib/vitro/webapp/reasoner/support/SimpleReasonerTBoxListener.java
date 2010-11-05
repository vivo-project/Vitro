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

		/*
		 * If added that B is a subclass of A
		 * 
		 * find all individuals who are typed as B and assert that
		 * they are of type A.  (list null, rdf:type B)
		 * 
		 * (incremental will have taken care of this: or a subclass of B
		 * and assert that they are type A.)
		 * 
		 * 
		 * 
		 */
		
		/*
		 * If added that B is equivalent to A, same as saying
		 * that B is subclass of A and A is subclass of B, invoke
		 * logic above for both cases.
		 * 
		 * 
		 */
		
		
		
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
	
		/*
		 * If removed that B is a subclass of A
		 * 
		 * For each individual that is typed as B (list null, rdf:type B) 
		 *  retract that it is of type A -- UNLESS the individual is of some
		 *  type C that is a subclass of A OR is it asserted directly that
		 *  the individual is of type A. 
		 * 
		 * 
		 * 
		 */
		
		/*
		 * Invoke the logic above in both directions. 
		 * 
		 */
		
		
		
		
		try {
			log.debug("stmt = " + stmt.toString());
			// call method in SimpleReasoner
			
		} catch (Exception e) {
			// don't stop the edit if there's an exception
	
			log.error("Exception while retracting inferences: ", e);
		}
	}
	
}
