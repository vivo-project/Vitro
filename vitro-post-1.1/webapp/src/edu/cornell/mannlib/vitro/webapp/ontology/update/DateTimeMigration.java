/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

/**  
* Performs knowledge base updates to the abox to align with the new
* 1.2 Date/Time representation.
*   
*/ 
public class DateTimeMigration {

	private OntModel aboxModel;
	private OntologyChangeLogger logger;  
	private OntologyChangeRecord record;

	/**
	 * Constructor 
	 *  
	 * @param   aboxModel    - the knowledge base to be updated
	 * @param   logger       - for writing to the change log
	 *                         and the error log.
	 * @param   record       - for writing to the additions model 
	 *                         and the retractions model.                  
	 */
	public DateTimeMigration(OntModel aboxModel,OntologyChangeLogger logger, OntologyChangeRecord record) {
		
		this.aboxModel = aboxModel;
		this.logger = logger;
		this.record = record;
	}
	
	/**
	 * 
	 * Update a knowledge base to align with changes in the Date/Time class
	 * and property definitions in the transition from version 1.1 to 1.2.
	 *  
	 */
	public void updateABox() {
				
		aboxModel.enterCriticalSection(Lock.WRITE);
		
		try {
			
	       Model additions = ModelFactory.createDefaultModel();
	       Model retractions = ModelFactory.createDefaultModel();
	       

	    
		   aboxModel.remove(retractions);
		   record.recordRetractions(retractions);
		   aboxModel.add(additions);
		   record.recordAdditions(additions);
		   
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}	
}