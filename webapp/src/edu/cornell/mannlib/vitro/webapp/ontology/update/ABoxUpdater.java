/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.util.ArrayList;
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
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.ontology.update.AtomicOntologyChange.AtomicChangeType;

/**  
* Performs knowledge base updates to the abox to align with a new ontology version
*   
*/ 
public class ABoxUpdater {

	private OntModel oldTboxModel;
	private OntModel newTboxModel;
	private OntModel aboxModel;
	private OntologyChangeLogger logger;  
	private OntologyChangeRecord record;
	private OntClass OWL_THING = (ModelFactory
			.createOntologyModel(OntModelSpec.OWL_MEM))
			.createClass(OWL.Thing.getURI());

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
		               OntologyChangeLogger logger,
		               OntologyChangeRecord record) {
		
		this.oldTboxModel = oldTboxModel;
		this.newTboxModel = newTboxModel;
		this.aboxModel = aboxModel;
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
				  deleteClass(change);
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
	 * Update a knowledge based on a class rename in the ontology. All references to the
	 * old class URI in either the subject or the object position of a statement are
	 * changed to use the new class URI. 
	 *  
	 * @param   change - an AtomicOntologyChange object representing a class
	 *                   rename operation.
	 *                    
	 */
	public void renameClass(AtomicOntologyChange change) throws IOException {
		
		logger.log("Processing a class rename from: " + change.getSourceURI() + " to " + change.getDestinationURI());
		aboxModel.enterCriticalSection(Lock.WRITE);
		
		try {
			
	       Model additions = ModelFactory.createDefaultModel();
	       Model retractions = ModelFactory.createDefaultModel();
	       
	       //TODO - look for these in the models and log error if not found
		   Resource oldClass = ResourceFactory.createResource(change.getSourceURI());
		   Resource newClass = ResourceFactory.createResource(change.getDestinationURI());	   
		   
		   // Change class references in the subjects of statements
		   StmtIterator iter = aboxModel.listStatements(oldClass, (Property) null, (RDFNode) null);

		   int count = 0;
		   while (iter.hasNext()) {
			   count++;
			   Statement oldStatement = iter.next();
			   Statement newStatement = ResourceFactory.createStatement(newClass, oldStatement.getPredicate(), oldStatement.getObject());
			   retractions.add(oldStatement);
			   additions.add(newStatement);
			   logChange(oldStatement, false);
			   logChange(newStatement,true);
		   }
		   
		   //log summary of changes
		   if (count > 0) {
			   logger.log("Changing " + count + " subject referernces to the "  + oldClass.getURI() + " class to be " + newClass.getURI());
		   }

		   // Change class references in the objects of statements
		   iter = aboxModel.listStatements((Resource) null, (Property) null, oldClass);

		   count = 0;
		   while (iter.hasNext()) {
			   count++;
			   Statement oldStatement = iter.next();
			   Statement newStatement = ResourceFactory.createStatement(oldStatement.getSubject(), oldStatement.getPredicate(), newClass);
			   retractions.add(oldStatement);
			   additions.add(newStatement);
			   //TODO - worried about logging changes before the changes have actually been made
			   // in the model
			   logChanges(oldStatement, newStatement);
		   }
		   
		   //log summary of changes
		   if (count > 0) {
			   logger.log("Changing " + count + " object referernces to the "  + oldClass.getURI() + " class to be " + newClass.getURI());
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
	 * Examine a knowledge based on a class addition to the ontology and
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
	   
		logger.log("Processing a class addition of class " + change.getDestinationURI());
		
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
				        logger.log("There are " + count + " individuals in the model that are of type " + parentOfAddedClass.getURI() + "," +
				        		    " and a new subclass of that class has been added: " + addedClass.getURI() + ". " +
				        		    "Please review the following individuals to see whether they should be of type: " +  addedClass.getURI() + ":" +
				        		    indList );
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
	public void deleteClass(AtomicOntologyChange change) throws IOException {

		logger.log("Processing a class deletion of class " + change.getSourceURI());
		
		OntClass deletedClass = oldTboxModel.getOntClass(change.getSourceURI());
		
		if (deletedClass == null) {
			logger.logError("didn't find the deleted class " +
					        change.getSourceURI() + " in the old model.");
			return;
		}

		List<OntClass> classList = deletedClass.listSuperClasses(true).toList();
		List<OntClass> namedClassList = new ArrayList<OntClass>();
		for (OntClass ontClass : classList) { 
			if (!ontClass.isAnon()) {
				namedClassList.add(ontClass);
			}
		}
		OntClass parent = (namedClassList.isEmpty()) 
								? namedClassList.get(0) 
								: OWL_THING;
		
		OntClass replacementClass = newTboxModel.getOntClass(parent.getURI());
		
		while (replacementClass == null) {
			 parent = parent.getSuperClass();
	    	 replacementClass = newTboxModel.getOntClass(parent.getURI()); 			
		} 

	   //log summary of changes
	   logger.log("Class " + deletedClass.getURI() + " has been deleted. Any references to it in the knowledge base will be changed to " + 
			        replacementClass.getURI());

		AtomicOntologyChange chg = new AtomicOntologyChange(deletedClass.getURI(), replacementClass.getURI(), AtomicChangeType.RENAME);
		renameClass(chg);		
	}
	
	public void processPropertyChanges(List<AtomicOntologyChange> changes) throws IOException {
		Iterator<AtomicOntologyChange> propItr = changes.iterator();
		while(propItr.hasNext()){
			AtomicOntologyChange propChangeObj = propItr.next();
			switch (propChangeObj.getAtomicChangeType()){
			case ADD: addProperty(propChangeObj);
			break;
			case DELETE: deleteProperty(propChangeObj);
			break;
			case RENAME: renameProperty(propChangeObj);
			break;
			default: logger.logError("unexpected change type indicator: " + propChangeObj.getAtomicChangeType());
			break;
		    }		
		}
	}
	
	private void addProperty(AtomicOntologyChange propObj) throws IOException{
		OntProperty tempProperty = newTboxModel.getOntProperty
			(propObj.getDestinationURI());
		if (tempProperty == null) {
			logger.logError("Unable to find property " + 
					propObj.getDestinationURI() +
					" in newTBoxModel");
			return;
		}
		OntProperty superProperty = tempProperty.getSuperProperty();
		if (superProperty == null) {
			return;
		}
		int count = aboxModel.listStatements(
				(Resource) null, superProperty, (RDFNode) null).toSet().size();
		if (count > 0) {
			logger.log("The Property " + superProperty.getURI() + 
					" which occurs " + count + " times in database has " +
							"a new subProperty " + propObj.getDestinationURI() +
					" in the new ontology version. ");
			logger.log("Please review accordingly.");
		}
	}
	
	private void deleteProperty(AtomicOntologyChange propObj) throws IOException{
		OntProperty deletedProperty = oldTboxModel.getOntProperty(propObj.getSourceURI());
		
		if (deletedProperty == null) {
			logger.logError("expected to find property " 
					+ propObj.getSourceURI() + " in oldTBoxModel");
			return;
		}
		
		OntProperty replacementProperty = null;
		OntProperty parent = deletedProperty.getSuperProperty();
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
		
		Model deletePropModel = ModelFactory.createDefaultModel();
		
		if (replacementProperty == null) {
			aboxModel.enterCriticalSection(Lock.WRITE);
			try {
				deletePropModel.add(aboxModel.listStatements(
						(Resource) null, deletedProperty, (RDFNode) null));
				aboxModel.remove(deletePropModel);
			} finally {
				aboxModel.leaveCriticalSection();
			}
			record.recordRetractions(deletePropModel);
			if (deletePropModel.size() > 0) {
				logger.log(deletePropModel.size() + " statements using " + 
						propObj.getSourceURI() + " were removed. " +
						" Please refer to the removed data model");
			}
		} else {
			AtomicOntologyChange chg = new AtomicOntologyChange(deletedProperty.getURI(), replacementProperty.getURI(), AtomicChangeType.RENAME);
			renameProperty(chg);
		}		
		
	}
	
	private void renameProperty(AtomicOntologyChange propObj) throws IOException {
		
		OntProperty oldProperty = oldTboxModel.getOntProperty(propObj.getSourceURI());
		OntProperty newProperty = newTboxModel.getOntProperty(propObj.getDestinationURI());
		
		if (oldProperty == null || newProperty == null) {
			logger.logError(" expects non-null old property and new property "
					+ "URIs");
			return;
		}
		
		Model renamePropAddModel = ModelFactory.createDefaultModel();
		Model renamePropRetractModel = 
			ModelFactory.createDefaultModel();
		
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
			logger.log(renamePropRetractModel.size() + " statments using " +
					"property " + propObj.getSourceURI() + " were changed to use " +
					propObj.getDestinationURI() + " instead. Please refer to the " +
					"removed data model and the added data model.");
		}
		
	}

	
	public void logChanges(Statement oldStatement, Statement newStatement) throws IOException {
       logChange(oldStatement,false);
       logChange(newStatement,true);
	}

	public void logChange(Statement statement, boolean add) throws IOException {
		logger.log( (add ? "Added " : "Removed") + "Statement: subject = " + statement.getSubject().getURI() +
				" property = " + statement.getPredicate().getURI() +
                " object = " + (statement.getObject().isLiteral() ?  ((Literal)statement.getObject()).getLexicalForm()
                		                                          : ((Resource)statement.getObject()).getURI()));	
	}
}
