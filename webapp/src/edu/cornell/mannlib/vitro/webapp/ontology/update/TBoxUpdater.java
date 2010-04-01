/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**  
* Performs knowledge base updates to the tbox to align with a new ontology version
*   
*/ 
public class TBoxUpdater {

	private OntModel oldTboxModel;
	private OntModel newTboxModel;
	private OntModel siteModel;
	private OntologyChangeLogger logger;  
	private OntologyChangeRecord record;

	/**
	 * 
	 * Constructor 
	 *  
	 * @param   oldTboxModel - previous version of the ontology
	 * @param   newTboxModel - new version of the ontology
	 * @param   siteModel    - the knowledge base to be updated
	 * @param   logger       - for writing to the change log
	 *                         and the error log.
	 * @param   record       - for writing to the additions model 
	 *                         and the retractions model.
	 *                    
	 */
	public TBoxUpdater(OntModel oldTboxModel,
			           OntModel newTboxModel,
			           OntModel siteModel,
		               OntologyChangeLogger logger,
		               OntologyChangeRecord record) {
		
		this.oldTboxModel = oldTboxModel;
		this.newTboxModel = newTboxModel;
		this.siteModel = siteModel;
		this.logger = logger;
		this.record = record;
	}
	
	/**
	 * 
	 * Update a knowledge base to align with changes vitro annotation property default 
	 * values in a new version of the ontology. The two versions of the ontology and the
	 * knowledge base to be updated are provided in the class constructor and are
	 * referenced via class level variables. 
	 *                    
	 *  Writes to the change log file, the error log file, and the incremental change
	 *  knowledge base.                  
	 */
	public void updateVitroPropertyDefaultValues() throws IOException {
					
    /*	   For each statement in the previous version of the ontology, look for a statement
		   with the same subject and predicate in the current version, and if the object of
		   the statement has changed then examine the site model to see if it needs to be
		   updated. If the site model has *not* changed the object of the statement as it appears
		   in the previous version, then update it to be the value as it appears in the current
		   version. 
		  
		   If the site has updated the value of a class property for which the default has
		   changed, log a message suggesting that they review the setting.
    */		  
		  
		  StmtIterator iter = oldTboxModel.listStatements();
		  
		  int stmtCount = 0;
		  
		  while (iter.hasNext()) {
			  
			 stmtCount++;
			 Statement stmt = iter.next();
			 Resource subject = stmt.getSubject();
			 Property predicate = stmt.getPredicate();
			 RDFNode oldObject = stmt.getObject();
			 
			 NodeIterator objects = newTboxModel.listObjectsOfProperty(subject, predicate);
			 
			 if ((objects == null) || (objects.toList().size() == 0) ) {
				 logger.logError("Error: found a statement for subject = " + subject.getURI() +
						 " and property = "  + predicate.getURI() +
						 " in the old version but not the new version of the ontology.");
				 continue;
				 			 
			 } else if (objects.toList().size() > 1) {
				 logger.logError("Error: found " + objects.toList().size() +
						 " statements with subject = " + subject.getURI() + 
						 " and property = " + predicate.getURI() +
						 " in the new version of the ontology. (maximum of one is expected)");
				 continue;
			 }
			 
			 RDFNode newObject = objects.next();
			 
			 if (!newObject.equals(oldObject)) {
				 objects = siteModel.listObjectsOfProperty(subject,predicate);

				 if (objects.toList().size() > 1) {
					 logger.logError("Warning: found " + objects.toList().size() +
							 " statements with subject = " + subject.getURI() + 
							 " and property = " + predicate.getURI() +
							 " in the site model (maximum of one is expected). +" +
							 " did not perform any update on this property");
					 continue;
				 }

				 RDFNode siteObject = objects.next();
				 
				 if (!siteObject.equals(oldObject)) {
	        	    try {
						siteModel.removeAll(subject, predicate, null);
					} catch (Exception e) {
						logger.logError("Error removing statement for subject = " + subject.getURI() + 
							            "and property = " + predicate.getURI() +
							            "from the knowledge base:\n" + e.getMessage());
					}

	        	    try {
	    				siteModel.add(subject, predicate, newObject);
	    				
						logger.log("Changed the value of property "  + predicate.getURI() +
								" of class = " + subject.getURI() + 
								" from " +
								 (newObject.isLiteral() ? ((Resource)oldObject).getURI() : ((Literal)oldObject).getLexicalForm()) +								
								" to " + 
								 (newObject.isLiteral() ? ((Resource)newObject).getURI() : ((Literal)newObject).getLexicalForm()) +
								 " in the knowledge base:\n");
					} catch (Exception e) {
						logger.logError("Error trying to change the value of property " + predicate.getURI() +
								" of class = " + subject.getURI() + " in the knowledge base:\n" + e.getMessage());
					}
				 }
			 }		  
		  }
		}
}
