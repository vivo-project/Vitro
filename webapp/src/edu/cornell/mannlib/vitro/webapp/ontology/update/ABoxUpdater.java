/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
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
				  //TODO - get method name automatically
				  logger.logError(this.getClass().getName() + " processClassChanges: unexpected change type indicator: " + change.getAtomicChangeType());
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
		
		aboxModel.enterCriticalSection(Lock.WRITE);
		
		try {
			
	       OntModel additions = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	       OntModel retractions = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	       
		   Resource oldClass = ResourceFactory.createResource(change.getSourceURI());
		   Resource newClass = ResourceFactory.createResource(change.getDestinationURI());	   
		   
		   // Change class references in the subjects of statements
		   StmtIterator iter = aboxModel.listStatements(oldClass, (Property) null, (RDFNode) null);

		   // TODO - catch and report exceptions in adding and removing from the model
		   while (iter.hasNext()) {	
			   Statement oldStatement = iter.next();
			   Statement newStatement = ResourceFactory.createStatement(newClass, oldStatement.getPredicate(), oldStatement.getObject());
			   retractions.add(oldStatement);
			   additions.add(newStatement);
			   logChange(oldStatement, false);
			   logChange(newStatement,true);
		   }
		   
		   // Change class references in the objects of statements
		   iter = aboxModel.listStatements((Resource) null, (Property) null, oldClass);

		   // TODO - catch and report exceptions in adding and removing from the model
		   while (iter.hasNext()) {	
			   Statement oldStatement = iter.next();
			   Statement newStatement = ResourceFactory.createStatement(oldStatement.getSubject(), oldStatement.getPredicate(), newClass);
			   retractions.add(oldStatement);
			   additions.add(newStatement);
			   //TODO - worried about logging changes before the changes are already made
			   // in the model
			   logChanges(oldStatement, newStatement);
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
	 * Examine a knowledge based on a class addition to the ontology, and
	 * add messages to the change log indicating where manual review is 
	 * recommended. If the added class has a parent in the new ontology
	 * that is not OWL.Thing, and if the knowledge base contains individuals
	 * asserted to be in the parent class, then log message recommending
	 * review of those individuals to see whether they are of the new
	 * class type.
	 *  
	 * @param   change - an AtomicOntologyChange object representing a class
	 *                   addition operation.
	 *                    
	 */
	public void addClass(AtomicOntologyChange change) throws IOException {
	   
		//TODO - is sourceURI the right thing here?
		OntClass addedClass = newTboxModel.getOntClass(change.getSourceURI());
		
		if (addedClass == null) {
			// TODO - log 
			return;
		}
		
		ExtendedIterator<OntClass> classIter = addedClass.listSuperClasses();
		
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
					
			        logger.log("There are " + count + " individuals in the model that are of type " + parentOfAddedClass.getURI() + "," +
			        		    " and a new subclass of that class has been added: " + addedClass.getURI() + ". " +
			        		    "Please review the following individuals to see whether they should be of type: " +  addedClass.getURI() + ":" +
			        		    indList );		
				}				
			}			
		}
	}

	/**
	 * 
	 * Update a knowledge base to account for a class deletion from the ontology.
	 * All references to the deleted class URI in either the subject or the object
	 * position of a statement are changed to use the closest available parent of
	 * the deleted class from the previous ontology that remains in the new version
	 * of the ontology. If the deleted class has more than one closest available parent,
	 * then no change is made to the knowledge base, and message indicating that manual
	 * review is necessary is added to the change log.
	 *  
	 * @param   change - an AtomicOntologyChange object representing a class
	 *                   delete operation.
	 *                    
	 */
	public void deleteClass(AtomicOntologyChange change) throws IOException {

		OntClass deletedClass = oldTboxModel.getOntClass(change.getSourceURI());
		
		if (deletedClass == null) {
			logger.logError(this.getClass().getName() + " :  deleteClass - didn't find the deleted class " + change.getSourceURI() + " in the old model.");
			return;
		}
		
		//TODO - what if there are multiple parents? maybe
		// in that case don't do the rename, but rather
		// log a message that they will have to review?
		OntClass parent = deletedClass.getSuperClass();
		OntClass replacementClass = newTboxModel.getOntClass(parent.getURI());
		
		while (replacementClass == null) {
			 //ditto
			 parent = parent.getSuperClass();
	    	 replacementClass = newTboxModel.getOntClass(parent.getURI()); 			
		} 
		
		AtomicOntologyChange chg = new AtomicOntologyChange(deletedClass.getURI(), replacementClass.getURI(), AtomicChangeType.RENAME);
		renameClass(chg);		
	}
	
	public void processPropertyChanges(List<AtomicOntologyChange> changes) throws IOException {
		Iterator propItr = changes.iterator();
		while(propItr.hasNext()){
			AtomicOntologyChange propChangeObj = (AtomicOntologyChange)
			                                     propItr.next();
			switch (propChangeObj.getAtomicChangeType()){
			case ADD: addProperties(propChangeObj);
			break;
			case DELETE: deleteProperties(propChangeObj);
			break;
			case RENAME: renameProperties(propChangeObj);
			break;
			default: logger.logError("Property change can't be null");
			break;
		    }		
		}
	}
	
	private void addProperties(AtomicOntologyChange propObj) throws IOException{
		Statement tempStatement = null;
		OntProperty tempProperty = newTboxModel.getOntProperty
		(propObj.getDestinationURI()).getSuperProperty();
		StmtIterator stmItr = aboxModel.listStatements();
		int count = 0;
		while(stmItr.hasNext()){
			tempStatement = stmItr.nextStatement();
			if(tempStatement.getPredicate().getURI().
					equalsIgnoreCase(tempProperty.getURI())){
				count++;			
			}
		}
		logger.log("The Property " + tempProperty.getURI() + 
				"which occurs " + count + "times in database has " +
						"a new subProperty " + propObj.getDestinationURI() +
				"added to Core 1.0");
		logger.log("Please review accordingly");			
	}
	private void deleteProperties(AtomicOntologyChange propObj) throws IOException{
		OntModel deletePropModel = ModelFactory.createOntologyModel();
		Statement tempStatement = null;
		StmtIterator stmItr = aboxModel.listStatements();
		while(stmItr.hasNext()){
			tempStatement = stmItr.nextStatement();
			if(tempStatement.getPredicate().getURI().equalsIgnoreCase
					(propObj.getSourceURI())){
				deletePropModel.add(tempStatement);	
			}
		}
		aboxModel = (OntModel) aboxModel.difference(deletePropModel);	
		record.recordRetractions(deletePropModel);
		logger.log("All statements using " + propObj.getSourceURI() +
				"were removed. Please refer removed data model");
	}
	private void renameProperties(AtomicOntologyChange propObj){
		Model renamePropAddModel = ModelFactory.createDefaultModel();
		Model renamePropRetractModel = 
			ModelFactory.createDefaultModel();
		Statement tempStatement = null;
		Property tempProperty = null; 
		StmtIterator stmItr = aboxModel.listStatements();
		while(stmItr.hasNext()){
			tempStatement = stmItr.nextStatement();
			if(tempStatement.getPredicate().getURI().
					equalsIgnoreCase(propObj.getSourceURI())){
				tempProperty = ResourceFactory.createProperty(
						propObj.getDestinationURI());
				aboxModel.remove(tempStatement);
				renamePropRetractModel.add(tempStatement);
				aboxModel.add(tempStatement.getSubject(),tempProperty
						,tempStatement.getObject());
				renamePropAddModel.add(tempStatement.getSubject(),
						tempProperty,tempStatement.getObject());
			}
		}
		record.recordAdditions(renamePropAddModel);
		record.recordRetractions(renamePropRetractModel);	
	}

	
	public void logChanges(Statement oldStatement, Statement newStatement) throws IOException {
       logChange(oldStatement,false);
       logChange(newStatement,true);
	}

	public void logChange(Statement statement, boolean add) throws IOException {
		logger.log( (add ? "Added " : "Removed") + "Statement: subject = " + statement.getSubject().getURI() +
				" property = " + statement.getPredicate().getURI() +
                " object = " + (statement.getObject().isLiteral() ? ((Resource)statement.getObject()).getURI() 
                		                                          : ((Literal)statement.getObject()).getLexicalForm()));	
	}
}
