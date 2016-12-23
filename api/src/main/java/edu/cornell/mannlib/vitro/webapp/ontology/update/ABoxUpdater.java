/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.ontology.update.AtomicOntologyChange.AtomicChangeType;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;

/**  
* Performs knowledge base updates to the abox to align with a new ontology version
*   
*/ 
public class ABoxUpdater {

    private final Log log = LogFactory.getLog(ABoxUpdater.class);
	private OntModel oldTboxModel;
	private OntModel newTboxModel;
	private Dataset dataset;
	private RDFService rdfService;
	private OntModel newTBoxAnnotationsModel;
	private TBoxUpdater tboxUpdater;
	private ChangeLogger logger;  
	private ChangeRecord record;
	private OntClass OWL_THING = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createClass(OWL.Thing.getURI());

	/**
	 * 
	 * Constructor 
	 *  
	 * @param   settings     - Update settings
	 * @param   logger       - for writing to the change log
	 *                         and the error log.
	 * @param   record       - for writing to the additions model 
	 *                         and the retractions model.
	 *                    
	 */
	public ABoxUpdater(UpdateSettings settings,
		               ChangeLogger logger,
		               ChangeRecord record) {
		
	    this.oldTboxModel = settings.getOldTBoxModel();
        this.newTboxModel = settings.getNewTBoxModel();
        RDFService rdfService = settings.getRDFService();
		this.dataset = new RDFServiceDataset(rdfService);
		this.rdfService = rdfService;
		this.newTBoxAnnotationsModel = settings.getNewTBoxAnnotationsModel();
		this.logger = logger;
		this.record = record;
		this.tboxUpdater = new TBoxUpdater(settings, logger, record);
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
	    
	    Iterator<String> graphIt = dataset.listNames();
	    while(graphIt.hasNext()) {
	        String graph = graphIt.next();
	        if(!KnowledgeBaseUpdater.isUpdatableABoxGraph(graph)){
	            continue;
	        }
	        Model aboxModel = dataset.getNamedModel(graph);
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
				
			    Iterator<String> graphIt = dataset.listNames();
	            while(graphIt.hasNext()) {
	                String graph = graphIt.next();
	                if(!KnowledgeBaseUpdater.isUpdatableABoxGraph(graph)){
	                    continue;
	                }
			        Model aboxModel = dataset.getNamedModel(graph);
			            
    				StmtIterator stmtIter = aboxModel.listStatements(null, RDF.type, parentOfAddedClass);
    				
    				int count = stmtIter.toList().size();
    					
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
        Iterator<String> graphIt = dataset.listNames();
        while(graphIt.hasNext()) {
            String graph = graphIt.next();
            if(!KnowledgeBaseUpdater.isUpdatableABoxGraph(graph)){
                continue;
            }
            Model aboxModel = dataset.getNamedModel(graph);
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
	}
	
	protected int deleteIndividual(Resource individual) throws IOException {

	    Model retractions = ModelFactory.createDefaultModel();
	    int refCount = 0;
	    
        Iterator<String> graphIt = dataset.listNames();
        while(graphIt.hasNext()) {
            String graph = graphIt.next();
            if(!KnowledgeBaseUpdater.isUpdatableABoxGraph(graph)){
                continue;
            }
            Model aboxModel = dataset.getNamedModel(graph);
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
        }
		
		return refCount;
	}
	
	public void processPropertyChanges(List<AtomicOntologyChange> changes) throws IOException {
				
		Iterator<AtomicOntologyChange> propItr = changes.iterator();
		while(propItr.hasNext()){
			AtomicOntologyChange propChangeObj = propItr.next();
			log.debug("processing " + propChangeObj);
			try {
			    if (propChangeObj.getAtomicChangeType() == null) {
			        log.error("Missing change type; skipping " + propChangeObj);
			        continue;
			    }
    			switch (propChangeObj.getAtomicChangeType()){
    			  case ADD: 
    			   log.debug("add");
    			   addProperty(propChangeObj);
    			   break;
    			case DELETE: 
    			   log.debug("delete");
    			   deleteProperty(propChangeObj);
    			   break;
    			case RENAME: 
    			   log.debug("rename");
    			   renameProperty(propChangeObj);
    			   break;
    			default: 
    			   log.debug("unknown");
    			   logger.logError("unexpected change type indicator: " + propChangeObj.getAtomicChangeType());
    			   break;
    		    }	
			} catch (Exception e) {
			    log.error(e,e);
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
            Iterator<String> graphIt = dataset.listNames();
            while(graphIt.hasNext()) {
                String graph = graphIt.next();
                if(!KnowledgeBaseUpdater.isUpdatableABoxGraph(graph)){
                    continue;
                }
                Model aboxModel = dataset.getNamedModel(graph);
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
		
        Iterator<String> graphIt = dataset.listNames();
        while(graphIt.hasNext()) {
            String graph = graphIt.next();
            if(!KnowledgeBaseUpdater.isUpdatableABoxGraph(graph)){
                continue;
            }
            Model aboxModel = dataset.getNamedModel(graph);
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
		
	}
	
	private void renameProperty(AtomicOntologyChange propObj) throws IOException {
		
		logger.log("Processing a property rename from: " + propObj.getSourceURI() + " to " + propObj.getDestinationURI());
		
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
		
		long start = System.currentTimeMillis();
        Iterator<String> graphIt = dataset.listNames();
        while(graphIt.hasNext()) {
            String graph = graphIt.next();
            if(!KnowledgeBaseUpdater.isUpdatableABoxGraph(graph)){
                continue;
            }
            Model aboxModel = dataset.getNamedModel(graph);
		
    		Model renamePropAddModel = ModelFactory.createDefaultModel();
    		Model renamePropRetractModel = 	ModelFactory.createDefaultModel();
    		log.debug("renaming " + oldProperty.getURI() + " in graph " + graph);
    		aboxModel.enterCriticalSection(Lock.WRITE);
    		try {
    		    start = System.currentTimeMillis();
    		    
//    		    String queryStr = "CONSTRUCT { ?s <" + oldProperty.getURI() + "> ?o } WHERE { GRAPH<" + graph + "> { ?s <" + oldProperty.getURI() + "> ?o } } ";
//    		    try {
//    		        renamePropRetractModel = RDFServiceUtils.parseModel(rdfService.sparqlConstructQuery(queryStr, RDFService.ModelSerializationFormat.NTRIPLE), RDFService.ModelSerializationFormat.NTRIPLE);
//    		    } catch (RDFServiceException e) {
//    		        log.error(e,e);
//    		    }
//    		    log.info(System.currentTimeMillis() - start + " to run sparql construct for " + renamePropRetractModel.size() + " statements" );
    		    start = System.currentTimeMillis();
    			renamePropRetractModel.add(	aboxModel.listStatements(
    					(Resource) null, oldProperty, (RDFNode) null));
    			log.debug(System.currentTimeMillis() - start + " to list " + renamePropRetractModel.size() + " old statements");
    			start = System.currentTimeMillis();
    			StmtIterator stmItr = renamePropRetractModel.listStatements();
    			while(stmItr.hasNext()) {
    				Statement tempStatement = stmItr.nextStatement();
    				renamePropAddModel.add( tempStatement.getSubject(),
    										newProperty,
    										tempStatement.getObject() );
    			}
    			log.debug(System.currentTimeMillis() - start + " to make new statements");
    			start = System.currentTimeMillis();
    			aboxModel.remove(renamePropRetractModel);
    			log.debug(System.currentTimeMillis() - start + " to retract old statements");
    			start = System.currentTimeMillis();
    			aboxModel.add(renamePropAddModel);
    			log.debug(System.currentTimeMillis() - start + " to add new statements");
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
        
        tboxUpdater.renameProperty(propObj);
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
