/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**  
* Performs knowledge base updates to the tbox to align with a new ontology version
*   
*/ 
public class TBoxUpdater {

	private OntModel oldTboxAnnotationsModel;
	private OntModel newTboxAnnotationsModel;
	private OntModel siteModel;
	private OntologyChangeLogger logger;  
	private OntologyChangeRecord record;

	/**
	 * 
	 * Constructor 
	 *  
	 * @param   oldTboxAnnotationsModel - previous version of the annotations in the ontology
	 * @param   newTboxAnnotationsModel - new version of the annotations in the ontology
	 * @param   siteModel    - the knowledge base to be updated
	 * @param   logger       - for writing to the change log
	 *                         and the error log.
	 * @param   record       - for writing to the additions model 
	 *                         and the retractions model.
	 *                    
	 */
	public TBoxUpdater(OntModel oldTboxAnnotationsModel,
			           OntModel newTboxAnnotationsModel,
			           OntModel siteModel,
		               OntologyChangeLogger logger,
		               OntologyChangeRecord record) {
		
		this.oldTboxAnnotationsModel = oldTboxAnnotationsModel;
		this.newTboxAnnotationsModel = newTboxAnnotationsModel;
		this.siteModel = siteModel;
		this.logger = logger;
		this.record = record;
	}
	
	/**
	 * 
	 * Update a knowledge base to align with changes to vitro annotation property default 
	 * values in a new version of the ontology. The two versions of the ontology and the
	 * knowledge base to be updated are provided in the class constructor and are
	 * referenced via class level variables. 
	 *                    
	 * If the default value (i.e. the value that is provided in the vivo-core 
	 * annotations files) of a vitro annotation property has been changed for a vivo
	 * core class, and that default value has not been changed in the site knowledge
	 * base, then update the value in the site knowledge base to be the new default.
	 * Also, if a new vitro annotation property setting (i.e. either an existing 
	 * setting applied to an existing class where it wasn't applied before, or
	 * an existing setting applied to a new class) has been applied to a vivo
	 * core class then copy that new property statement into the site model.
	 * If a property setting for a class exists in the old ontology but
	 * not in the new one, then that statement will be removed from the
	 * site knowledge base.
	 *                    
	 *  Writes to the change log file, the error log file, and the incremental change
	 *  knowledge base.                  
	 *  
	 *  Note: as specified, this method for now assumes that no new vitro annotation
	 *  properties have been introduced. This should be updated for future versions.
	 */
	public void updateVitroPropertyDefaultValues() throws IOException {
				
		siteModel.enterCriticalSection(Lock.WRITE);
		
		try {
			
	       Model additions = ModelFactory.createDefaultModel();
	       Model retractions = ModelFactory.createDefaultModel();
		
    	 //  Update defaults values for vitro annotation properties in the site model
         //  if the default has changed in the new version of the ontology AND if 
         //  the site hasn't overidden the previous default in their knowledge base.
    		    
		  StmtIterator iter = oldTboxAnnotationsModel.listStatements();
		  		  
		  int stmtCount = 0;
		  
		  while (iter.hasNext()) {
			  
			 stmtCount++;
			 Statement stmt = iter.next();
			 Resource subject = stmt.getSubject();
			 Property predicate = stmt.getPredicate();
			 RDFNode oldObject = stmt.getObject();
			 
			 if (! ( (RDFS.getURI().equals(predicate.getNameSpace())) || 
					 (VitroVocabulary.vitroURI.equals(predicate.getNameSpace())) 
					) ) {
				 // this annotation updater is only concerned with properties
				 // such as rdfs:comment and properties in the vitro application
				 // namespace
				 continue;
			 }
			 
			 if (VitroVocabulary.TAB_AUTOLINKEDTOTAB.equals(predicate.getURI())) {
				 continue;
				 // the tab autolinks are not like the other annotations
				 // and should never be updated automatically
			 }
			 			 
			 NodeIterator newObjects = newTboxAnnotationsModel.listObjectsOfProperty(subject, predicate);
			 
			 if ((newObjects == null) || (!newObjects.hasNext()) ) {
				 // first check to see if the site has a local value change
				 // that should override the deletion
				 List<RDFNode> siteObjects = siteModel.listObjectsOfProperty(subject, predicate).toList();
				 if (siteObjects.size() > 1) {
					 logger.logError("Error: found " + siteObjects.size() +
							 " statements with subject = " + subject.getURI() + 
							 " and property = " + predicate.getURI() +
							 " in the site database. (maximum of one is expected)");
				 }
				 if (siteObjects.size() > 0) {
					 RDFNode siteNode = siteObjects.get(0);
					 if (siteNode.equals(oldObject)) {
						 retractions.add(siteModel.listStatements(subject, predicate, (RDFNode) null));		 
					 }
				 }
				 continue;				 			 
			 }
			
			 RDFNode newObject = newObjects.next();
			 
			 int i = 1;
			 while (newObjects.hasNext()) {
                 i++;
                 newObjects.next();
             } 
			 
			 if (i > 1) {
				 logger.log("WARNING: found " + i +
						 " statements with subject = " + subject.getURI() + 
						 " and property = " + predicate.getURI() +
						 " in the new version of the annotations ontology. (maximum of one is expected)");
				 continue; 
			 }
			 			 			 
			 if (!newObject.equals(oldObject)) {
				 NodeIterator siteObjects = siteModel.listObjectsOfProperty(subject,predicate);
				 
				 if (siteObjects == null || !siteObjects.hasNext()) {
					 continue;
				 }
			
				 RDFNode siteObject = siteObjects.next();

		         i = 1;
				 while (siteObjects.hasNext()) {
					 i++; 
					 siteObjects.next();
				 } 

				 if (i > 1) {
					 logger.log("WARNING: found " + i +
							 " statements with subject = " + subject.getURI() + 
							 " and property = " + predicate.getURI() +
							 " in the site annotations model. (maximum of one is expected). "); 
					 continue; 
				 }
				 	 
				 if (siteObject.equals(oldObject)) {
	        	    try {
	        	    	StmtIterator it = siteModel.listStatements(subject, predicate, (RDFNode)null);
	        	    	while (it.hasNext()) {
	        	    	  retractions.add(it.next());	
	        	    	}
					} catch (Exception e) {
						logger.logError("Error removing statement for subject = " + subject.getURI() + 
							            "and property = " + predicate.getURI() +
							            "from the knowledge base:\n" + e.getMessage());
					}

	        	    try {
	    				additions.add(subject, predicate, newObject);
	    				
						logger.log("Changed the value of property "  + predicate.getURI() +
								" of subject = " + subject.getURI() + 
								" from " +
								 (oldObject.isResource() ? ((Resource)oldObject).getURI() : ((Literal)oldObject).getLexicalForm()) +								
								" to " + 
								 (newObject.isResource() ? ((Resource)newObject).getURI() : ((Literal)newObject).getLexicalForm()) +
								 " in the knowledge base:\n");
					} catch (Exception e) {
						logger.logError("Error trying to change the value of property " + predicate.getURI() +
								" of class = " + subject.getURI() + " in the knowledge base:\n" + e.getMessage());
					}
				 }
			 }		  
		   }
		     
		   Model actualAdditions = additions.difference(retractions);
		   siteModel.add(actualAdditions);
		   record.recordAdditions(actualAdditions);
		   Model actualRetractions = retractions.difference(additions);
		   siteModel.remove(actualRetractions);
		   record.recordRetractions(actualRetractions);
		
		   // log summary of changes
		   if (actualAdditions.size() > 0) {
	           logger.log("Updated the default vitro annotation value for " + 
	        		   actualAdditions.size() + " statements in the knowledge base.");
		   }
		   
           long numRemoved = actualRetractions.size() - actualAdditions.size();
           if (numRemoved > 0) {
	           logger.log("Removed " + numRemoved +
	        		      " superfluous vitro annotation property setting" + ((numRemoved > 1) ? "s" : "") + " from the knowledge base.");
           }
           
		    //	   Copy annotation property settings that were introduced in the new ontology
		    //     into the site model.
		    //		  

			Model newAnnotationSettings = newTboxAnnotationsModel.difference(oldTboxAnnotationsModel);
			Model newAnnotationSettingsToAdd = ModelFactory.createDefaultModel();
			StmtIterator newStmtIt = newAnnotationSettings.listStatements();
			while (newStmtIt.hasNext()) {
				Statement stmt = newStmtIt.next();
				if (!siteModel.contains(stmt)) {
					newAnnotationSettingsToAdd.add(stmt);
					
					// TODO remove this for production
					logger.log( "adding Statement: subject = " + stmt.getSubject().getURI() +
						    " property = " + stmt.getPredicate().getURI() +
		                    " object = " + (stmt.getObject().isLiteral() ?  ((Literal)stmt.getObject()).getLexicalForm() 
		                		                                          : ((Resource)stmt.getObject()).getURI()));	
				}
			}
			
			siteModel.add(newAnnotationSettingsToAdd);
			record.recordAdditions(newAnnotationSettingsToAdd);
            
			// log the additions
            //summary
			if (newAnnotationSettingsToAdd.size() > 0) {
				boolean plural = (newAnnotationSettingsToAdd.size() > 1);
	            logger.log("Added " + newAnnotationSettingsToAdd.size() + " new annotation property setting" + (plural ? "s" : "") + " to the knowledge base. This includes " +
	                         "existing annotation properties applied to existing classes where they weren't applied before, or existing " +
	                         "properties applied to new classes. No new annotation properties have been introduced.");
			}
		   
	} finally {
		siteModel.leaveCriticalSection();
	}
}
}