/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
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
import com.hp.hpl.jena.vocabulary.RDF;
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
	private ChangeLogger logger;  
	private ChangeRecord record;
	private boolean detailLogs = false;
	
    private static final String classGroupURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#ClassGroup";
	private Resource classGroupClass = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createResource(classGroupURI);
    private static final String inClassGroupURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#inClassGroup";
	private Property inClassGroupProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createProperty(inClassGroupURI);

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
		               ChangeLogger logger,
		               ChangeRecord record) {
		
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
	public void updateDefaultAnnotationValues() throws IOException {
				
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
			 			 
			 NodeIterator newObjects = newTboxAnnotationsModel.listObjectsOfProperty(subject, predicate);
			 
			 if ((newObjects == null) || (!newObjects.hasNext()) ) {
				 // first check to see if the site has a local value change
				 // that should override the deletion
				 List<RDFNode> siteObjects = siteModel.listObjectsOfProperty(subject, predicate).toList();
				
				 if (siteObjects.size() > 1) {
					 /*
					 logger.log("WARNING: found " + siteObjects.size() +
					 		 " statements with subject = " + subject.getURI() + 
							 " and property = " + predicate.getURI() +
							 " in the site database (maximum of one is expected)");
					 */
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
						 " in the new version of the annotations ontology (maximum of one is expected)");
				 continue; 
			 }
			 
			 // If a subject-property pair occurs in the old annotation TBox and the new annotations 
			 // TBox, but not in the site model, then it is considered an erroneous deletion and
			 // the value from the new TBox is added into the site model.
			 // sjm: 7-16-2010. We want this here now to add back in annotations mistakenly dropped
			 // in the .9 to 1.0 migration, but I'm not sure we would want this here permanently.
			 // Shouldn't a site be allowed to delete annotations if they want to?
			 
			 NodeIterator siteObjects = siteModel.listObjectsOfProperty(subject,predicate);
			 
			 if (siteObjects == null || !siteObjects.hasNext()) {
	        	    try {
	    				additions.add(subject, predicate, newObject);
	    			
						if (detailLogs) {
							   logger.log( "adding Statement: subject = " + subject.getURI() +
								           " property = " + predicate.getURI() +
				                           " object = " + (newObject.isLiteral() ?  ((Literal)newObject).getLexicalForm() 
				                		                                          : ((Resource)newObject).getURI()));	
							}
					} catch (Exception e) {
						logger.logError("Error trying to add statement with property " + predicate.getURI() +
								" of class = " + subject.getURI() + " in the knowledge base:\n" + e.getMessage());
					}				 
				 
				   continue;
			 }
			 
			 			 			 
			 if (!newObject.equals(oldObject)) {

				 RDFNode siteObject = siteObjects.next();

		         i = 1;
				 while (siteObjects.hasNext()) {
					 i++; 
					 siteObjects.next();
				 } 

				 if (i > 1) {
					 /*
					 logger.log("WARNING: found " + i +
							 " statements with subject = " + subject.getURI() + 
							 " and property = " + predicate.getURI() +
							 " in the site annotations model (maximum of one is expected) "); 
					 */
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
	    				
	    				if (detailLogs) {
						   logger.log("Changed the value of property "  + predicate.getURI() +
								" of subject = " + subject.getURI() + 
								" from " +
								 (oldObject.isResource() ? ((Resource)oldObject).getURI() : ((Literal)oldObject).getLexicalForm()) +								
								" to " + 
								 (newObject.isResource() ? ((Resource)newObject).getURI() : ((Literal)newObject).getLexicalForm()) +
								 " in the knowledge base:\n");
	    				}
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
		
		   long numAdded = actualAdditions.size();
		   long numRemoved = actualRetractions.size();
		   
		   // log summary of changes
		   if (numAdded > 0) {
	           logger.log("Updated the default vitro annotation value for " + 
	        		   numAdded + " statements in the knowledge base");
		   }
		   
           if (numRemoved > 0) {
	           logger.log("Removed " + numRemoved +
	        		      " outdated vitro annotation property setting" + ((numRemoved > 1) ? "s" : "") + " from the knowledge base");
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
				
					if (detailLogs) {
					   logger.log( "adding Statement: subject = " + stmt.getSubject().getURI() +
						           " property = " + stmt.getPredicate().getURI() +
		                           " object = " + (stmt.getObject().isLiteral() ?  ((Literal)stmt.getObject()).getLexicalForm() 
		                		                                          : ((Resource)stmt.getObject()).getURI()));	
					}
				}
			}
			
			siteModel.add(newAnnotationSettingsToAdd);
			record.recordAdditions(newAnnotationSettingsToAdd);
            
			// log the additions - summary
			if (newAnnotationSettingsToAdd.size() > 0) {
				boolean plural = (newAnnotationSettingsToAdd.size() > 1);
	            logger.log("Added " + newAnnotationSettingsToAdd.size() + " new annotation property setting" + (plural ? "s" : "") + " to the knowledge base. This includes only " +
	                         "existing annotation properties applied to existing classes where they weren't applied before, or existing " +
	                         "properties applied to new classes.");
			}
		   
	} finally {
		siteModel.leaveCriticalSection();
	}
}
	
/**
 * 
 * Update a knowledge base to align with changes to the vitro annotation model  
 * in a new version of the ontology. The two versions of the ontology and the
 * knowledge base to be updated are provided in the class constructor and are
 * referenced via class level variables. 
 *                    
 * Currently, this method only handles deletions of a ClassGroup
 *                    
 *  Writes to the change log file, the error log file, and the incremental change
 *  knowledge base.                  
 *  
 */	
public void updateAnnotationModel() throws IOException {
		
	   // for each ClassGroup in the old vitro annotations model: if it is not in 
	   // the new vitro annotations model and the site has no classes asserted to 
	   // be in that class group then delete it.
	   
	   removeObsoleteAnnotations();
	   
	   siteModel.enterCriticalSection(Lock.WRITE);
	   
	   try {	
		    Model retractions = ModelFactory.createDefaultModel();
		    
			StmtIterator iter = oldTboxAnnotationsModel.listStatements((Resource) null, RDF.type, classGroupClass);
	  	
			while (iter.hasNext()) {  
			  Statement stmt = iter.next();
						  
			  if (!newTboxAnnotationsModel.contains(stmt) && !usesGroup(siteModel, stmt.getSubject())) {
				  long pre = retractions.size();
				  retractions.add(siteModel.listStatements(stmt.getSubject(),(Property) null,(RDFNode)null));
				  long post = retractions.size();
				  if ((post - pre) > 0) {
				     logger.log("Removed the " + stmt.getSubject().getURI() + " ClassGroup from the annotations model");
				  }  
			  }
			}
	    	
			if (retractions.size() > 0) {
			   siteModel.remove(retractions);
			   record.recordRetractions(retractions);
		    }   
		        
		} finally {
			siteModel.leaveCriticalSection();
		}

	   // If we were going to handle add, this is the logic:
	   // for each ClassGroup in new old vitro annotations model: if it is not in 
	   // the old vitro annotations and it is not in the site model, then 
	   // add it.
	   
}

public boolean usesGroup(Model model, Resource theClassGroup) throws IOException {
	   
   model.enterCriticalSection(Lock.READ);
     
   try {	   
	   return (model.contains((Resource) null, inClassGroupProp, theClassGroup) ? true : false);
   } finally {
	   model.leaveCriticalSection();
   }
}

public void removeObsoleteAnnotations() throws IOException {
		
	Resource subj1 = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createResource("http://vivoweb.org/ontology/florida#StatewideGoalAndFocusArea");
	Resource obj1 = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createResource("http://vivoweb.org/ontology#vitroClassGrouptopics");
	
	Property subj2 = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createProperty("http://vivoweb.org/ontology/florida#divisionOfSponsoredResearchNumber");
	Resource obj2 = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createResource("http://vivoweb.org/ontology#vitroPropertyGroupidentifiers");
	
	Property subj3 = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createProperty("http://vivoweb.org/ontology/florida#statewideGoalAndFocusArea");
	Resource obj3 = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createResource("http://vivoweb.org/ontology#vitroPropertyGroupoutreach");
	
	Property inPropertyGroupProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createProperty("http://vitro.mannlib.cornell.edu/ns/vitro/0.7#inPropertyGroup");
	   
    siteModel.enterCriticalSection(Lock.WRITE);
   
    try {	
	    Model retractions = ModelFactory.createDefaultModel();
	    
        if (siteModel.contains(subj1, inClassGroupProp, obj1) ) {
        	retractions.add(subj1, inClassGroupProp, obj1);
        	logger.log("Removed statement " + ABoxUpdater.stmtString(subj1, inClassGroupProp, obj1) + " from the knowledge base (assumed to be obsolete)");
        }

        if (siteModel.contains(subj2, inPropertyGroupProp, obj2) ) {
        	retractions.add(subj2, inPropertyGroupProp, obj2);
        	logger.log("Removed statement " + ABoxUpdater.stmtString(subj2, inPropertyGroupProp, obj2) + " from the knowledge base (assumed to be obsolete)");
        }

        if (siteModel.contains(subj3, inPropertyGroupProp, obj3) ) {
        	retractions.add(subj3, inPropertyGroupProp, obj3);
        	logger.log("Removed statement " + ABoxUpdater.stmtString(subj3, inPropertyGroupProp, obj3) + " from the knowledge base (assumed to be obsolete)");
        }
    	
		if (retractions.size() > 0) {
		   siteModel.remove(retractions);
		   record.recordRetractions(retractions);
	    }   
	        
	 } finally {
		siteModel.leaveCriticalSection();
	 }
}

}