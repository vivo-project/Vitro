/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.ontology.update.AtomicOntologyChange.AtomicChangeType;

/**  
* Performs knowledge base updates to the abox to align with a new ontology version
*   
*/ 
public class ABoxUpdater {

	private OntModel oldTboxModel;
	private OntModel newTboxModel;
	private OntModel aboxModel;
	private OntModel newTBoxAnnotationsModel;
	private ChangeLogger logger;  
	private ChangeRecord record;
	private OntClass OWL_THING = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createClass(OWL.Thing.getURI());

	/**
	 * 
	 * Constructor 
	 *  
	 * @param   oldTboxModel - previous version of the ontology
	 * @param   newTboxModel - new version of the ontology
	 * @param   aboxModel    - the knowledge base to be updated
	 * @param   logger       - for writing to the change log
	 *                         and the error log.
	 * @param   record       - for writing to the additions model 
	 *                         and the retractions model.
	 *                    
	 */
	public ABoxUpdater(OntModel oldTboxModel,
			           OntModel newTboxModel,
			           OntModel aboxModel,
			           OntModel newAnnotationsModel,
		               ChangeLogger logger,
		               ChangeRecord record) {
		
		this.oldTboxModel = oldTboxModel;
		this.newTboxModel = newTboxModel;
		this.aboxModel = aboxModel;
		this.newTBoxAnnotationsModel = newAnnotationsModel;
		this.logger = logger;
		this.record = record;
	}
	
	/**
	 * 
	 * Update a knowledge base to align with changes in the class definitions in 
	 * a new version of the ontology. The two versions of the ontology and the
	 * knowledge base to be updated are provided in the class constructor and
	 * are referenced via class level variables.
	 *  
	 * @param   changes - a list of AtomicOntologyChange objects, each representing
	 *                    one change in class definition in the new version of the
	 *                    ontology. 
	 *                    
	 *  Writes to the change log file, the error log file, and the incremental change
	 *  knowledge base.                  
	 */
	public void processClassChanges(List<AtomicOntologyChange> changes) throws IOException {
		
		Iterator<AtomicOntologyChange> iter = changes.iterator();
		
		while (iter.hasNext()) {
			AtomicOntologyChange change = iter.next();

			switch (change.getAtomicChangeType()){
			   case ADD:
				  addClass(change);
			      break;
			   case DELETE:
				  if ("Delete".equals(change.getNotes())) {
				     deleteIndividualsOfType(change);
				  } else {
					 renameClassToParent(change);
				  }
			      break;
			   case RENAME:
				  renameClass(change);
			      break;
			   default:
				  logger.logError("unexpected change type indicator: " + change.getAtomicChangeType());
		    }		
		}
	}

	/**
	 * 
	 * Update the knowledge base for a class rename in the ontology. All references to the
	 * old class URI in either the subject or the object position of a statement are
	 * changed to use the new class URI. 
	 *  
	 * @param   change - an AtomicOntologyChange object representing a class
	 *                   rename operation.
	 *                    
	 */
	public void renameClass(AtomicOntologyChange change) throws IOException {
		
		//logger.log("Processing a class rename from: " + change.getSourceURI() + " to " + change.getDestinationURI());
		aboxModel.enterCriticalSection(Lock.WRITE);
		
		try {
			
	       Model additions = ModelFactory.createDefaultModel();
	       Model retractions = ModelFactory.createDefaultModel();
	       
	       //TODO - look for these in the models and log error if not found
		   Resource oldClass = ResourceFactory.createResource(change.getSourceURI());
		   Resource newClass = ResourceFactory.createResource(change.getDestinationURI());	   
		   
		   // Change class references in the subjects of statements
		   
		   // BJL 2010-04-09 : In future versions we need to keep track of
		   // the difference between true direct renamings and "use-insteads."
		   // For now, the best behavior is to remove any remaining statements
		   // where the old class is the subject, *unless* the statements
		   // is part of the new annotations file (see comment below) or the
		   // predicate is vitro:autolinkedToTab.  In the latter case,
		   // the autolinking annotation should be rewritten using the 
		   // new class name.
		   
		   StmtIterator iter = aboxModel.listStatements(oldClass, (Property) null, (RDFNode) null);

		   int removeCount = 0;
		   while (iter.hasNext()) {
			   Statement oldStatement = iter.next();
			   removeCount++;
			   retractions.add(oldStatement);
		   }
		   
		   //log summary of changes
		   if (removeCount > 0) {
			   logger.log("Removed " + removeCount + " subject reference" + ((removeCount > 1) ? "s" : "") + " to the "  + oldClass.getURI() + " class");
		   }

		   // Change class references in the objects of rdf:type statements
		   iter = aboxModel.listStatements((Resource) null, RDF.type, oldClass);

		   int renameCount = 0;
		   while (iter.hasNext()) {
			   renameCount++;
			   Statement oldStatement = iter.next();
			   Statement newStatement = ResourceFactory.createStatement(oldStatement.getSubject(), RDF.type, newClass);
			   retractions.add(oldStatement);
			   additions.add(newStatement);
		   }
		   
		   //log summary of changes
		   if (renameCount > 0) {
			   logger.log("Retyped " + renameCount + " individual" + ((renameCount > 1) ? "s" : "") + " from type "  + oldClass.getURI() + " to type " + newClass.getURI());
		   }
		   
		   aboxModel.remove(retractions);
		   record.recordRetractions(retractions);
		   aboxModel.add(additions);
		   record.recordAdditions(additions);
		   
		} finally {
			aboxModel.leaveCriticalSection();
		}
		
	}

	/**
	 * 
	 * Examine the knowledge base for a class addition to the ontology and
	 * add messages to the change log indicating where manual review is 
	 * recommended. If the added class has a direct parent in the new ontology
	 * that is not OWL.Thing, and if the knowledge base contains individuals
	 * asserted to be in the parent class, then log a message recommending
	 * review of those individuals to see whether they are of the new
	 * class type.
	 *  
	 * @param   change - an AtomicOntologyChange object representing a class
	 *                   addition operation.
	 *                    
	 */
	public void addClass(AtomicOntologyChange change) throws IOException {
	   
		//logger.log("Processing a class addition of class " + change.getDestinationURI());
		
		OntClass addedClass = newTboxModel.getOntClass(change.getDestinationURI());
		
		if (addedClass == null) {
			logger.logError("didn't find the added class " + change.getDestinationURI() + " in the new model.");
			return;
		}
		
		List<OntClass> classList = addedClass.listSuperClasses(true).toList();
		List<OntClass> namedClassList = new ArrayList<OntClass>();
		for (OntClass ontClass : classList) { 
			if (!ontClass.isAnon()) {
				namedClassList.add(ontClass);
			}
		}
		if (namedClassList.isEmpty()) {
			namedClassList.add(OWL_THING);
		}
		
		Iterator<OntClass> classIter = namedClassList.iterator();
		
		while (classIter.hasNext()) {
			OntClass parentOfAddedClass = classIter.next();

			if (!parentOfAddedClass.equals(OWL.Thing)) {
				
				StmtIterator stmtIter = aboxModel.listStatements(null, RDF.type, parentOfAddedClass);
				
				int count = stmtIter.toList().size();
				if (count > 0) {
					
					String indList = "";
					while (stmtIter.hasNext()) {
						Statement stmt = stmtIter.next();
						indList += "\n\t" + stmt.getSubject().getURI(); 
					}
					
					if (count > 0) {
						//TODO - take out the detailed logging after our internal testing is completed.
				        logger.log("There " + ((count > 1) ? "are" : "is") + " " + count + " individual" + ((count > 1) ? "s" : "")  + " in the model that " + ((count > 1) ? "are" : "is") + " of type " + parentOfAddedClass.getURI() + "," +
				        		    " and a new subclass of that class has been added: " + addedClass.getURI() + ". " +
				        		    "Please review " + ((count > 1) ? "these" : "this") + " individual" + ((count > 1) ? "s" : "") + " to see whether " + ((count > 1) ? "they" : "it") + " should be of type: " +  addedClass.getURI() );
					}
				}				
			}			
		}
	}

	/**
	 * 
	 * Update a knowledge base to account for a class deletion in the ontology.
	 * All references to the deleted class URI in either the subject or the object
	 * position of a statement are changed to use the closest available parent of
	 * the deleted class from the previous ontology that remains in the new version
	 * of the ontology. Note that the closest available parent may be owl:Thing.
	 * If the deleted class has more than one closest available parent, then
	 * change individuals that were asserted to be of the deleted class to be 
	 * asserted to be of both parent classes. 
	 *  
	 * @param   change - an AtomicOntologyChange object representing a class
	 *                   delete operation.
	 *                    
	 */
	public void renameClassToParent(AtomicOntologyChange change) throws IOException {

		//logger.log("Processing a class migration to parent for deleted class " + change.getSourceURI());
		
		OntClass deletedClass = oldTboxModel.getOntClass(change.getSourceURI());
		
		if (deletedClass == null) {
			logger.log("WARNING: didn't find the deleted class " +  change.getSourceURI() + " in the old model. Skipping updates for this deletion");
			return;
		}

		List<OntClass> classList = deletedClass.listSuperClasses(true).toList();
		List<OntClass> namedClassList = new ArrayList<OntClass>();
		for (OntClass ontClass : classList) { 
			if (!ontClass.isAnon()) {
				namedClassList.add(ontClass);
			}
		}
		
		OntClass parent = (!namedClassList.isEmpty()) 
								? namedClassList.get(0) 
								: OWL_THING;
										
		OntClass replacementClass = newTboxModel.getOntClass(parent.getURI());
		
		while (replacementClass == null) {
			 parent = parent.getSuperClass();
			 
			 if (parent == null) {
				  replacementClass = OWL_THING; 
			 } else {
	    	      replacementClass = newTboxModel.getOntClass(parent.getURI());
			 }
		} 

	    //log summary of changes
	    logger.log("Class " + deletedClass.getURI() + " has been deleted. Any references to it in the knowledge base have been changed to " +   replacementClass.getURI());

		AtomicOntologyChange chg = new AtomicOntologyChange(deletedClass.getURI(), replacementClass.getURI(), AtomicChangeType.RENAME, change.getNotes());
		renameClass(chg);			
	}
	
	/**
	 * 
	 * Remove all instances of the given class from the abox of the knowledge base.
	 * 
	 * @param   change - an AtomicOntologyChange object representing a class
	 *                   delete operation.
	 *                    
	 */
	public void deleteIndividualsOfType(AtomicOntologyChange change) throws IOException {

		//logger.log("Processing a class deletion of class " + change.getSourceURI());
		
		OntClass deletedClass = oldTboxModel.getOntClass(change.getSourceURI());
		
		if (deletedClass == null) {
			logger.log("WARNING: didn't find the deleted class " +  change.getSourceURI() + " in the old model. Skipping updates for this deletion");
			return;
		}
		
		// Remove instances of the deleted class
		aboxModel.enterCriticalSection(Lock.WRITE);
	    try {
	       int count = 0;
	       int refCount = 0;
	       StmtIterator iter = aboxModel.listStatements((Resource) null, RDF.type, deletedClass);

    	   while (iter.hasNext()) {
			   count++;
			   Statement typeStmt = iter.next();   
			   refCount = deleteIndividual(typeStmt.getSubject());
		   }   
		   
		   if (count > 0) {
			   logger.log("Removed " + count + " individual" + (((count > 1) ? "s" : "") + " of type " + deletedClass.getURI()) + " (refs = " + refCount + ")");
		   }
		   		   
		} finally {
			aboxModel.leaveCriticalSection();
		}
	}
	
	protected int deleteIndividual(Resource individual) throws IOException {

	    Model retractions = ModelFactory.createDefaultModel();
	    int refCount = 0;
	    
		aboxModel.enterCriticalSection(Lock.WRITE);
	    try {			   
		   StmtIterator iter = aboxModel.listStatements(individual, (Property) null, (RDFNode) null);
			   
		   while (iter.hasNext()) {
			  Statement subjstmt = iter.next();
			  retractions.add(subjstmt);
		   }
			   
		   iter = aboxModel.listStatements((Resource) null, (Property) null, individual);
			   
		    while (iter.hasNext()) {
			    Statement objstmt = iter.next();
			    retractions.add(objstmt);
			    refCount++;
			}
		   
		   aboxModel.remove(retractions);
		   record.recordRetractions(retractions);		
		   
		} finally {
			aboxModel.leaveCriticalSection();
		}
		
		return refCount;
	}
	
	public void processPropertyChanges(List<AtomicOntologyChange> changes) throws IOException {
				
		Iterator<AtomicOntologyChange> propItr = changes.iterator();
		while(propItr.hasNext()){
			AtomicOntologyChange propChangeObj = propItr.next();
			switch (propChangeObj.getAtomicChangeType()){
			  case ADD: 
			   addProperty(propChangeObj);
			   break;
			case DELETE: 
			   deleteProperty(propChangeObj);
			   break;
			case RENAME: 
			   renameProperty(propChangeObj);
			   break;
			default: 
			   logger.logError("unexpected change type indicator: " + propChangeObj.getAtomicChangeType());
			   break;
		    }		
		}
	}
	
	private void addProperty(AtomicOntologyChange propObj) throws IOException{
		
		//logger.log("Processing a property addition of property " + propObj.getDestinationURI());
		
		OntProperty addedProperty = newTboxModel.getOntProperty 	(propObj.getDestinationURI());
		
		if (addedProperty == null) {
			logger.logError("Unable to find property " + propObj.getDestinationURI() + 	" in new TBox");
			return;
		}
		
		//  if the newly added property has an inverse in the new TBox, then for all  existing
		//  ABox statements involving that inverse (if the inverse is new also there won't be
		//  any) add the corresponding statement with the new property.
		
		OntProperty inverseOfAddedProperty = addedProperty.getInverseOf();
		
		if (inverseOfAddedProperty != null) {
			Model additions = ModelFactory.createDefaultModel();
			aboxModel.enterCriticalSection(Lock.WRITE);
			
			try {
				StmtIterator iter = aboxModel.listStatements((Resource) null, inverseOfAddedProperty, (RDFNode) null);
		
				while (iter.hasNext()) {
					
					Statement stmt = iter.next();
					
					if (stmt.getObject().isResource()) {
					   Statement newStmt = ResourceFactory.createStatement(stmt.getObject().asResource(), addedProperty, stmt.getSubject());
					   additions.add(newStmt);
					} else {
						logger.log("WARNING: expected the object of this statement to be a Resource but it is not. No inverse has been asserted: " + stmtString(stmt));
					}
				}
				
				aboxModel.add(additions);
				record.recordAdditions(additions);
				
				if (additions.size() > 0) {
					logger.log("Added " + additions.size() + " statement" + 
							((additions.size() > 1) ? "s" : "") +
							" with predicate " + addedProperty.getURI() + 
							" (as an inverse to existing  " + inverseOfAddedProperty.getURI() + 
							" statement" + ((additions.size() > 1) ? "s" : "") + ")");
				}
				
			} finally {
				aboxModel.leaveCriticalSection();
			}
		}
	}
	
	private void deleteProperty(AtomicOntologyChange propObj) throws IOException{
		
		//logger.log("Processing a property deletion of property " + propObj.getSourceURI());
		
		OntProperty deletedProperty = oldTboxModel.getOntProperty(propObj.getSourceURI());
		
		if (deletedProperty == null && "Prop".equals(propObj.getNotes())) {
			deletedProperty = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createOntProperty(propObj.getSourceURI());
		}
		
		if (deletedProperty == null ) {
			logger.log("WARNING: didn't find deleted property " + propObj.getSourceURI() + " in oldTBoxModel");
			return;
		}
		
		OntProperty replacementProperty = null;
		
		if (!propObj.getNotes().equals("Delete")) {
		
			OntProperty parent =  deletedProperty.getSuperProperty();
			
			if (parent != null) {
				replacementProperty = newTboxModel.getOntProperty(parent.getURI());
				
				while (replacementProperty == null) {
					 parent = parent.getSuperProperty();
					 if (parent == null) {
						 break;
					 }
			    	 replacementProperty = newTboxModel.getOntProperty(parent.getURI()); 			
				} 
			}
		}
		
		Model deletePropModel = ModelFactory.createDefaultModel();
		
		if (replacementProperty == null) {
						
			aboxModel.enterCriticalSection(Lock.WRITE);
			try {
				deletePropModel.add(aboxModel.listStatements((Resource) null, deletedProperty, (RDFNode) null));
				aboxModel.remove(deletePropModel);
			} finally {
				aboxModel.leaveCriticalSection();
			}
			record.recordRetractions(deletePropModel);
			boolean plural = (deletePropModel.size() > 1);
			if (deletePropModel.size() > 0) {
				logger.log("Removed " + deletePropModel.size() + " statement" + (plural ? "s" : "") + " with predicate " + 
						propObj.getSourceURI());
			}
		} else {
			AtomicOntologyChange chg = new AtomicOntologyChange(deletedProperty.getURI(), replacementProperty.getURI(), AtomicChangeType.RENAME, propObj.getNotes());
			renameProperty(chg);
		}		
		
	}
	
	private void renameProperty(AtomicOntologyChange propObj) throws IOException {
		
		//logger.log("Processing a property rename from: " + propObj.getSourceURI() + " to " + propObj.getDestinationURI());
		
		OntProperty oldProperty = oldTboxModel.getOntProperty(propObj.getSourceURI());
		OntProperty newProperty = newTboxModel.getOntProperty(propObj.getDestinationURI());
		
		if (oldProperty == null) {
			logger.logError("didn't find the " + propObj.getSourceURI() + " property in the old TBox");
			return;
		}
		
		if (newProperty == null) {
			logger.logError("didn't find the " + propObj.getDestinationURI() + " property in the new TBox");
			return;
		}
		
		Model renamePropAddModel = ModelFactory.createDefaultModel();
		Model renamePropRetractModel = 	ModelFactory.createDefaultModel();
		
		aboxModel.enterCriticalSection(Lock.WRITE);
		try {
			renamePropRetractModel.add(	aboxModel.listStatements(
					(Resource) null, oldProperty, (RDFNode) null));
			StmtIterator stmItr = renamePropRetractModel.listStatements();
			while(stmItr.hasNext()) {
				Statement tempStatement = stmItr.nextStatement();
				renamePropAddModel.add( tempStatement.getSubject(),
										newProperty,
										tempStatement.getObject() );
			}
			aboxModel.remove(renamePropRetractModel);
			aboxModel.add(renamePropAddModel);
		} finally {
			aboxModel.leaveCriticalSection();
		}
		
		record.recordAdditions(renamePropAddModel);
		record.recordRetractions(renamePropRetractModel);
		
		if (renamePropRetractModel.size() > 0) {
			logger.log("Changed " + renamePropRetractModel.size() + " statement" + 
					((renamePropRetractModel.size() > 1) ? "s" : "") +
					" with predicate " + propObj.getSourceURI() + " to use " +
					propObj.getDestinationURI() + " instead");		
		}
	}

	protected void migrateExternalConcepts() throws IOException {
		
		Property hasResearchArea = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#hasResearchArea");
		Property researchAreaOf = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#researchAreaOf");
		Property hasSubjectArea = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#hasSubjectArea");
		Property subjectAreaFor = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#subjectAreaFor");
		
		Property sourceVocabularyReference = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#sourceVocabularyReference");
		Property webpage = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#webpage");
		Property linkURI = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#linkURI");
		Property linkAnchorText = ResourceFactory.createProperty("http://vivoweb.org/ontology/core#linkAnchorText");
		
		HashSet<Resource> resourcesToDelete = new HashSet<Resource>();
		HashSet<Resource> resourcesToDetype = new HashSet<Resource>();
			
		Model additions = ModelFactory.createDefaultModel();
		Model retractions = ModelFactory.createDefaultModel();
	    
		int subjectRefCount = 0;
		int researchRefCount = 0;
		int webpageCount = 0;
		int typeCount = 0;
		int count = 0;
		
		aboxModel.enterCriticalSection(Lock.WRITE);
	    try {
			StmtIterator iter = aboxModel.listStatements((Resource) null, sourceVocabularyReference, (RDFNode) null);
	
			while (iter.hasNext()) {
				 Statement stmt = iter.next();
				 Resource subjectAreaResource = stmt.getSubject();
				 
				 if (!stmt.getObject().isResource()) continue;
				 
			     Resource vocabularySourceReferenceResource = stmt.getObject().asResource();
			     Resource externalConceptResource = null;
			     Resource webpageResource = null;
			     
			     if (subjectAreaResource.hasProperty(webpage)) {
			    	 Statement webpageStmt = subjectAreaResource.getProperty(webpage);
			    	 RDFNode webpageObject = webpageStmt.getObject();
			    	 if (!webpageObject.isResource()) continue;
			    	 webpageResource = webpageObject.asResource();
			    	 if (!webpageResource.hasProperty(linkURI)) continue;
			    	 Statement linkURIStmt = webpageResource.asResource().getProperty(linkURI);
			    	 RDFNode linkURIObject = linkURIStmt.getObject();
			    	 if (!linkURIObject.isLiteral()) continue;
			    	 externalConceptResource = ResourceFactory.createResource(linkURIObject.asLiteral().getString()); 
			    	 if (externalConceptResource == null) continue;
			    	 resourcesToDelete.add(webpageResource.asResource());
	                 resourcesToDetype.add(vocabularySourceReferenceResource);
			         additions.add(externalConceptResource,RDFS.isDefinedBy,vocabularySourceReferenceResource);
			         additions.add(externalConceptResource, RDF.type, OWL_THING);
			         Statement conceptLabelStmt = subjectAreaResource.getProperty(RDFS.label);
			         if (conceptLabelStmt == null) {
			        	 conceptLabelStmt = webpageResource.asResource().getProperty(RDFS.label);
			         }
			         if (conceptLabelStmt != null) {
			        	 additions.add(externalConceptResource, RDFS.label, conceptLabelStmt.getObject().asLiteral());
			         }
			     } else {
			    	 continue;
			     }
			     				 
				 subjectRefCount += migrateConceptReferences(externalConceptResource, subjectAreaResource, hasSubjectArea, subjectAreaFor, additions);
				 researchRefCount += migrateConceptReferences(externalConceptResource, subjectAreaResource, hasResearchArea, researchAreaOf, additions);
				 migrateRelatedConcepts(externalConceptResource, subjectAreaResource, additions);
				 
				 resourcesToDelete.add(subjectAreaResource);
				 if (webpageResource != null) {
					 resourcesToDelete.add(webpageResource);
					 webpageCount++;
				 }
				 count++;
			}	
		
		    Iterator<Resource> vsrIter = resourcesToDetype.iterator();
		    while (vsrIter.hasNext()) { 
		    	 Resource resource = vsrIter.next();
		   		 StmtIterator typeiter = resource.listProperties(RDF.type);
				 while (typeiter.hasNext()) {
					 Statement typeStatement = typeiter.next();
					 if (!typeStatement.getObject().equals(OWL_THING)) {
					    retractions.add(typeStatement);
					 }
				 }	
				 additions.add(resource, RDF.type, OWL_THING);
				 typeCount++;
		    }

			aboxModel.add(additions);
			aboxModel.remove(retractions);
	    } finally {
	        aboxModel.leaveCriticalSection();	
	    }
	    
	    record.recordAdditions(additions);
	    record.recordRetractions(retractions);
	   
	    Iterator<Resource> iter = resourcesToDelete.iterator();
	    while (iter.hasNext()) {
	    	Resource ind = iter.next();
	    	deleteIndividual(ind);
	    }
	    
	    if (count > 0) {
;		   logger.log("migrated " + count + " external concept" + ((count == 1) ? "" : "s") + ", which involved deleting " + 
				   count + " vivo:SubjectArea individual" + ((count == 1) ? "" : "s") + " and " +
				   webpageCount + " vivo:URLLink individual" + ((webpageCount == 1) ? "" : "s") +
				   ", and changing the type for " + typeCount + " vivo:VocabularySourceReference individual" + ((typeCount == 1) ? "" : "s") + " to owl:Thing");
		}
	    
	    if (subjectRefCount > 0) {
			   logger.log("migrated " + subjectRefCount + " " + hasSubjectArea.getLocalName() + " external concept reference" + ((subjectRefCount == 1) ? "" : "s"));
		}
	    
	    if (researchRefCount > 0) {
			   logger.log("migrated " + researchRefCount + " " + hasResearchArea.getLocalName() + " external concept reference" + ((researchRefCount == 1) ? "" : "s"));
		}
	    
        return;
    }
	
	protected int migrateConceptReferences(Resource externalConceptResource, Resource subjectArea, Property hasConcept, Property conceptOf, Model additions) throws IOException {
		
	    int count = 0;
	    	    
		aboxModel.enterCriticalSection(Lock.WRITE);
	    try {
			 StmtIterator iter = aboxModel.listStatements((Resource) null, hasConcept, subjectArea);
			 while (iter.hasNext()) {

				 Statement stmt = iter.next();
				 Resource agent = stmt.getSubject();
				 if (!additions.contains(agent, hasConcept, externalConceptResource)) {
					 additions.add(agent, hasConcept, externalConceptResource);
					 count++;
				 }
				 
				 if (!additions.contains(externalConceptResource, conceptOf, agent)) {
				    additions.add(externalConceptResource, conceptOf, agent);
				 }
			 }
	    } finally {
	        aboxModel.leaveCriticalSection();	
	    }

		return count;
    }

	protected void migrateRelatedConcepts(Resource externalConceptResource, Resource subjectAreaResource, Model additions) throws IOException {
	    	
		Property related = ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#related");
		
		aboxModel.enterCriticalSection(Lock.WRITE);
	    try {
			 StmtIterator iter = subjectAreaResource.listProperties(related);
			 while (iter.hasNext()) {
				 Statement stmt = iter.next();
				 if (!stmt.getObject().isResource()) continue;
				 Resource relatedConcept = stmt.getObject().asResource();
				 if (!additions.contains(externalConceptResource, related, relatedConcept)) {
					 additions.add(externalConceptResource, related, relatedConcept);
				 }
				 if (!additions.contains(relatedConcept, related, externalConceptResource)) {
					 additions.add(relatedConcept, related, externalConceptResource);
				 }				 
			 }
	    } finally {
	        aboxModel.leaveCriticalSection();	
	    }
    }
	
	public void logChanges(Statement oldStatement, Statement newStatement) throws IOException {
       logChange(oldStatement,false);
       logChange(newStatement,true);
	}

	public void logChange(Statement statement, boolean add) throws IOException {
		logger.log( (add ? "Added" : "Removed") + stmtString(statement));
	}

    public static String stmtString(Statement statement) {
    	return  " [subject = " + statement.getSubject().getURI() +
    			"] [property = " + statement.getPredicate().getURI() +
                "] [object = " + (statement.getObject().isLiteral() ? ((Literal)statement.getObject()).getLexicalForm() + " (Literal)"
                		                                          : ((Resource)statement.getObject()).getURI() + " (Resource)") + "]";	
    }    
	
    public static String stmtString(Resource subject, Property predicate, RDFNode object) {
    	return  " [subject = " + subject.getURI() +
    			"] [property = " + predicate.getURI() +
                "] [object = " + (object.isLiteral() ? ((Literal)object).getLexicalForm() + " (Literal)"
                		                             : ((Resource)object).getURI() + " (Resource)") + "]";	
    }    
}
