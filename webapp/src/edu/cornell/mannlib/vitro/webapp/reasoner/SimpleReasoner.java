/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.jena.ABoxJenaChangeListener;
import edu.cornell.mannlib.vitro.webapp.dao.jena.CumulativeDeltaModeler;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DifferenceGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

/**
 * Allows for real-time incremental materialization or retraction of RDFS-
 * style class and property subsumption based ABox inferences as statements
 * are added to or removed from the (ABox or TBox) knowledge base. 
 * @author sjm222
 */

public class SimpleReasoner extends StatementListener {

	private static final Log log = LogFactory.getLog(SimpleReasoner.class);
	
	private OntModel tboxModel;             // asserted and inferred TBox axioms
	private OntModel aboxModel;             // ABox assertions
	private Model inferenceModel;           // ABox inferences
	private Model inferenceRebuildModel;    // work area for re-computing all ABox inferences
	private Model scratchpadModel;          // work area for re-computing all ABox inferences
	
	private static final String mostSpecificTypePropertyURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType";	
	private static final AnnotationProperty mostSpecificType = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createAnnotationProperty(mostSpecificTypePropertyURI);
	
	//TODO check this for thread safety
	private CumulativeDeltaModeler aBoxDeltaModeler1 = null;
	private CumulativeDeltaModeler aBoxDeltaModeler2 = null;
	private volatile boolean batchMode1 = false;
	private volatile boolean batchMode2 = false;
	private boolean stopRequested = false;
	
	private List<ReasonerPlugin> pluginList = new CopyOnWriteArrayList<ReasonerPlugin>();

	/**
	 * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param aboxModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
	 * @param inferenceRebuildModel - output. This the model is temporarily used when the whole ABox inference model is rebuilt
	 * @param inferenceScratchpadModel - output. This the model is temporarily used when the whole ABox inference model is rebuilt
 	 */
	public SimpleReasoner(OntModel tboxModel, RDFService rdfService, Model inferenceModel,
			              Model inferenceRebuildModel, Model scratchpadModel) {

		this.tboxModel = tboxModel;
        this.aboxModel = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM, ModelFactory.createModelForGraph(
                        new DifferenceGraph(new RDFServiceGraph(rdfService), inferenceModel.getGraph()))); 
		this.inferenceModel = inferenceModel;
		this.inferenceRebuildModel = inferenceRebuildModel;
		this.scratchpadModel = scratchpadModel;	
		this.batchMode1 = false;
		this.batchMode2 = false;
		aBoxDeltaModeler1 = new CumulativeDeltaModeler();
		aBoxDeltaModeler2 = new CumulativeDeltaModeler();
		stopRequested = false;
		
		if (rdfService == null) {
		    aboxModel.register(this);
		} else {
		    try {
    		    rdfService.registerListener(new ABoxJenaChangeListener(this));
    		} catch (RDFServiceException e) {
    		    throw new RuntimeException("Unable to register change listener", e);
    		}
		} 
	}
	
	/**
	 * This constructor is used for the unit tests only
	 * 
	 * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param aboxModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
 	 */
	public SimpleReasoner(OntModel tboxModel, OntModel aboxModel, Model inferenceModel) {
		this.tboxModel = tboxModel;
		this.aboxModel = aboxModel; 
		this.inferenceModel = inferenceModel;
		this.inferenceRebuildModel = ModelFactory.createDefaultModel();
		this.scratchpadModel = ModelFactory.createDefaultModel();
		aBoxDeltaModeler1 = new CumulativeDeltaModeler();
		aBoxDeltaModeler2 = new CumulativeDeltaModeler();
		this.batchMode1 = false;
		this.batchMode2 = false;
		stopRequested = false;
	}
	
	public void setPluginList(List<ReasonerPlugin> pluginList) {
		this.pluginList = pluginList;
	}
	
	public List<ReasonerPlugin> getPluginList() {
		return this.pluginList;
	}
	
	/*
	 * Performs incremental ABox reasoning based
	 * on the addition of a new statement
	 *  (aka assertion) to the ABox.
	 */
	@Override
	public void addedStatement(Statement stmt) {
		try {
			if (stmt.getPredicate().equals(RDF.type)) {
			     addedABoxTypeAssertion(stmt, inferenceModel, new HashSet<String>());
			     setMostSpecificTypes(stmt.getSubject(), inferenceModel, new HashSet<String>());
			} else if (stmt.getPredicate().equals(OWL.sameAs)) {  
                 addedABoxSameAsAssertion(stmt, inferenceModel); 
			} else {
				 addedABoxAssertion(stmt, inferenceModel);
			}
			
			doPlugins(ModelUpdate.Operation.ADD,stmt);

		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while computing inferences: " + e.getMessage());
			e.printStackTrace(); //TODO remove this for release
		}
	}
	
	/*
	 * Performs incremental ABox reasoning based
	 * on the retraction of a statement (aka assertion)
	 * from the ABox. 
	 */
	@Override
	public void removedStatement(Statement stmt) {
	
		try {
           // if (!isInterestedInRemovedStatement(stmt)) { return; }
		   // interested in all of them now that we are doing inverse
		   // property reasoning
            handleRemovedStatement(stmt);
            
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while retracting inferences: ", e);
			e.printStackTrace();
		}
	}
	
	/*
	 * Synchronized part of removedStatement. Interacts
	 * with DeltaComputer.
	 */
	protected synchronized void handleRemovedStatement(Statement stmt) {    
		if (batchMode1) {
			 aBoxDeltaModeler1.removedStatement(stmt);
		} else if (batchMode2) {
			 aBoxDeltaModeler2.removedStatement(stmt);
		} else {
			if (stmt.getPredicate().equals(RDF.type)) {
				removedABoxTypeAssertion(stmt, inferenceModel);
				setMostSpecificTypes(stmt.getSubject(), inferenceModel, new HashSet<String>());
			} else if (stmt.getPredicate().equals(OWL.sameAs)) {
                removedABoxSameAsAssertion(stmt, inferenceModel); 	
			} else {
				removedABoxAssertion(stmt, inferenceModel);
			}
			doPlugins(ModelUpdate.Operation.RETRACT,stmt);
		}
	}
	
	/*
	 * Performs incremental ABox reasoning based
	 * on changes to the class hierarchy.
	 * 
	 * Handles rdfs:subclassOf, owl:equivalentClass, and owl:inverseOf
	 */	
	public void addedTBoxStatement(Statement stmt) {
		try {		
			if (!(stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(OWL.equivalentClass) || stmt.getPredicate().equals(OWL.inverseOf))) {
				return;
			}

		    //log.debug("added TBox assertion = " + stmt.toString());
			
			if ( stmt.getObject().isResource() && (stmt.getObject().asResource()).getURI() == null ) {
				log.warn("The object of this assertion has a null URI: " + stmtString(stmt));
				return;
			}

			if ( stmt.getSubject().getURI() == null ) {
				log.warn("The subject of this assertion has a null URI: " + stmtString(stmt));
				return;
			}
			
			if (stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(OWL.equivalentClass)) {
				// ignore anonymous classes
				if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
				    return;
				}
			
				OntClass subject = tboxModel.getOntClass((stmt.getSubject()).getURI());
				if (subject == null) {
					log.debug("didn't find subject class in the tbox: " + (stmt.getSubject()).getURI());
					return;
				}
				
				OntClass object = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
				if (object == null) {
					log.debug("didn't find object class in the tbox: " + ((Resource)stmt.getObject()).getURI());
					return;
				}
				
				if (stmt.getPredicate().equals(RDFS.subClassOf)) {
					 addedSubClass(subject,object,inferenceModel);
				} else {
					 // equivalent class is the same as subclass in both directions
					 addedSubClass(subject,object,inferenceModel);
					 addedSubClass(object,subject,inferenceModel);
				} 
			} else {
				OntProperty prop1 = tboxModel.getOntProperty((stmt.getSubject()).getURI());
				if (prop1 == null) {
					log.debug("didn't find subject property in the tbox: " + (stmt.getSubject()).getURI());
					return;
					}
					
				OntProperty prop2 = tboxModel.getOntProperty(((Resource)stmt.getObject()).getURI()); 
				if (prop2 == null) {
					log.debug("didn't find object property in the tbox: " + ((Resource)stmt.getObject()).getURI());
					return;
				}
				
				addedInverseProperty(prop1, prop2, inferenceModel);	
			}
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while adding inference(s): " + e.getMessage());
		}
	}

	/*
	 * Performs incremental ABox reasoning based
	 * on changes to the class hierarchy.
	 * 
	 * Handles rdfs:subclassOf, owl:equivalentClass, and owl:inverseOf 
	 */
	public void removedTBoxStatement(Statement stmt) {	
		try {
			if (!(stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(OWL.equivalentClass) || stmt.getPredicate().equals(OWL.inverseOf))) {
				return;
			}
			
			//log.debug("removed TBox assertion = " + stmt.toString());
						
			if ( stmt.getObject().isResource() && (stmt.getObject().asResource()).getURI() == null ) {
				log.warn("The object of this assertion has a null URI: " + stmtString(stmt));
				return;
			}

			if ( stmt.getSubject().getURI() == null ) {
				log.warn("The subject of this assertion has a null URI: " + stmtString(stmt));
				return;
			}
						
			if ( stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(OWL.equivalentClass) ) {
				
				// ignore anonymous classes
				if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
				    return;
				}
				
				OntClass subject = tboxModel.getOntClass((stmt.getSubject()).getURI());
				if (subject == null) {
					log.debug("didn't find subject class in the tbox: " + (stmt.getSubject()).getURI());
					return;
				}
				
				OntClass object = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
				if (object == null) {
					log.debug("didn't find object class in the tbox: " + ((Resource)stmt.getObject()).getURI());
					return;
				}
				
				if (stmt.getPredicate().equals(RDFS.subClassOf)) {
				   removedSubClass(subject,object,inferenceModel);
				} else {
					// equivalent class is the same as subclass in both directions
				   removedSubClass(subject,object,inferenceModel);
				   removedSubClass(object,subject,inferenceModel);
				}
			} else {
				OntProperty prop1 = tboxModel.getOntProperty((stmt.getSubject()).getURI());
				if (prop1 == null) {
					log.debug("didn't find subject property in the tbox: " + (stmt.getSubject()).getURI());
					return;
				}
				
				OntProperty prop2 = tboxModel.getOntProperty(((Resource)stmt.getObject()).getURI()); 
				if (prop2 == null) {
					log.debug("didn't find object property in the tbox: " + ((Resource)stmt.getObject()).getURI());
					return;
				}
				
				removedInverseProperty(prop1, prop2, inferenceModel);					
			}
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while removing inference(s): " + e.getMessage());
		}
	}

    /*
     * This signature used when recomputing the whole ABox
     */
	protected void addedABoxTypeAssertion(Resource individual, Model inferenceModel, HashSet<String> unknownTypes) {

		StmtIterator iter = null;
		
		aboxModel.enterCriticalSection(Lock.READ);
		try {		
			iter = aboxModel.listStatements(individual, RDF.type, (RDFNode) null);
			
			while (iter.hasNext()) {	
				Statement stmt = iter.next();
				addedABoxTypeAssertion(stmt, inferenceModel, unknownTypes);
			}
		} finally {
			iter.close();
			aboxModel.leaveCriticalSection();
		}
	}
	
	/*
	 * Performs incremental reasoning based on a new type assertion
	 * added to the ABox (assertion that an individual is of a certain
	 * type).
	 * 
	 * If it is added that B is of type A, then for each superclass of
	 * A assert that B is of that type.
	 */
	protected void addedABoxTypeAssertion(Statement stmt, Model inferenceModel, HashSet<String> unknownTypes) {
				
	    tboxModel.enterCriticalSection(Lock.READ);
		try {
			OntClass cls = null;
			if ( (stmt.getObject().asResource()).getURI() != null ) {
				
			    cls = tboxModel.getOntClass(stmt.getObject().asResource().getURI()); 
			    if (cls != null) {
					List<OntClass> parents = (cls.listSuperClasses(false)).toList();		
					parents.addAll((cls.listEquivalentClasses()).toList());	
					Iterator<OntClass> parentIt = parents.iterator();
	
					if (parentIt.hasNext()) {
						while (parentIt.hasNext()) {
							OntClass parentClass = parentIt.next();
							
							// VIVO doesn't materialize statements that assert anonymous types
							// for individuals. Also, sharing an identical anonymous node is
							// not allowed in owl-dl. picklist population code looks at qualities
							// of classes not individuals.
							if (parentClass.isAnon()) continue;
							
							Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
							addInference(infStmt,inferenceModel,true);
						}						
					}					
				} else {
					if ( !(stmt.getObject().asResource().getNameSpace()).equals(OWL.NS)) {
						if (!unknownTypes.contains(stmt.getObject().asResource().getURI())) {
							unknownTypes.add(stmt.getObject().asResource().getURI());
					        log.warn("Didn't find the target class (the object of an added rdf:type statement) in the TBox: " +
						          	 (stmt.getObject().asResource()).getURI() + ". No class subsumption reasoning will be done based on type assertions of this type.");
						}
					}
				}
			} else {
				log.warn("The object of this rdf:type assertion has a null URI: " + stmtString(stmt));
				return;
			}
		} finally {
			tboxModel.leaveCriticalSection();
		}
		
		inferenceModel.enterCriticalSection(Lock.WRITE);
		try {
			if (inferenceModel.contains(stmt)) {
			    inferenceModel.remove(stmt);
			}
		} finally {
			inferenceModel.leaveCriticalSection();
		}	
	}

	/*
	 * If it is removed that B is of type A, then for each superclass of A remove
	 * the inferred statement that B is of that type UNLESS it is otherwise entailed
	 * that B is of that type.
	 * 
	 */
	protected void removedABoxTypeAssertion(Statement stmt, Model inferenceModel) {
		tboxModel.enterCriticalSection(Lock.READ);
		try {		
			OntClass cls = null;
			
			if ( (stmt.getObject().asResource()).getURI() != null ) {
			    cls = tboxModel.getOntClass(stmt.getObject().asResource().getURI()); 
			    
				if (cls != null) {
					if (entailedType(stmt.getSubject(),cls)) {
						addInference(stmt,inferenceModel,true);
					} 
					
					List<OntClass> parents = null;
					parents = (cls.listSuperClasses(false)).toList();		
					parents.addAll((cls.listEquivalentClasses()).toList());
					
					Iterator<OntClass> parentIt = parents.iterator();
					
					while (parentIt.hasNext()) {
					    
						OntClass parentClass = parentIt.next();
						
						// VIVO doesn't materialize statements that assert anonymous types
						// for individuals. Also, sharing an identical anonymous node is
						// not allowed in owl-dl. picklist population code looks at qualities
						// of classes not individuals.
						if (parentClass.isAnon()) continue;  
						
						if (entailedType(stmt.getSubject(),parentClass)) {
						    continue;    // if a type is still entailed without the
						}
						                                                              // removed statement, then don't remove it
						                                                              // from the inferences
						
						Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
                        removeInference(infStmt,inferenceModel,true,false);							
					}
				} else {
					log.warn("Didn't find target class (the object of the removed rdf:type statement) in the TBox: "
							+ ((Resource)stmt.getObject()).getURI() + ". No class subsumption reasoning will be performed based on the removal of this assertion.");
				}
			} else {
				log.warn("The object of this rdf:type assertion has a null URI: " + stmtString(stmt));
			}		
		} catch (Exception e) {
			log.warn("exception while removing abox type assertions: " + e.getMessage());
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

	/*
	 * Materializes inferences based on the owl:sameAs relationship.
	 *  
	 * If it is added that x owl:sameAs y, then all asserted and inferred
	 * statements about x will become inferred about y if they are not already
	 * asserted about y, and vice versa.
	 */
	protected void addedABoxSameAsAssertion(Statement stmt, Model inferenceModel) {
		Resource subject = null;
		Resource object = null;
		
		if (stmt.getSubject().isResource()) {
			 subject = stmt.getSubject().asResource();
			 if (tboxModel.containsResource(subject) || subject.isAnon()) {
				 log.debug("the subject of this sameAs statement is either in the tbox or an anonymous node, no reasoning will be done: " + stmtString(stmt));
				 return;
			 }
		} else {
			 log.warn("the subject of this sameAs statement is not a resource, no reasoning will be done: " + stmtString(stmt));
			 return;
		}

		if (stmt.getObject().isResource()) {
			 object = stmt.getObject().asResource();
			 if (tboxModel.containsResource(object) || object.isAnon()) {
				 log.debug("the object of this sameAs statement is either in the tbox or an anonymous node, no reasoning will be done: " + stmtString(stmt));
				 return;
			 }
		} else {
			 log.warn("the object of this sameAs statement is not a resource, no reasoning will be done: " + stmtString(stmt));
			 return;
		}

		inferenceModel.enterCriticalSection(Lock.WRITE);
		try {
			if (inferenceModel.contains(stmt)) {
			    inferenceModel.remove(stmt);
			}
		} finally {
			inferenceModel.leaveCriticalSection();
		}
		
		Statement opposite = ResourceFactory.createStatement(object, OWL.sameAs, subject);
		addInference(opposite,inferenceModel,true);
		
		generateSameAsInferences(subject, object, inferenceModel);
		generateSameAsInferences(object, subject, inferenceModel);		
	}	

	/*
	 * Materializes inferences based on the owl:sameAs relationship.
	 *  
	 * If it is removed	that x is sameAs y, then remove y sameAs x from 
	 * the inference graph and then recompute the inferences for x and
	 * y based on their respective assertions.																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																														 that x owl:sameAs y, then all asserted and inferred
	 */
	protected void removedABoxSameAsAssertion(Statement stmt, Model inferenceModel) {
		Resource subject = null;
		Resource object = null;
		
		if (stmt.getSubject().isResource()) {
			 subject = stmt.getSubject().asResource();
			 if (tboxModel.containsResource(subject) || subject.isAnon()) {
				 log.debug("the subject of this removed sameAs statement is either in the tbox or an anonymous node, no reasoning will be done: " + stmtString(stmt));
				 return;
			 }
		} else {
			 log.warn("the subject of this removed sameAs statement is not a resource, no reasoning will be done: " + stmtString(stmt));
			 return;
		}

		if (stmt.getObject().isResource()) {
			 object = stmt.getObject().asResource();
			 if (tboxModel.containsResource(object) || object.isAnon()) {
				 log.debug("the object of this removed sameAs statement is either in the tbox or an anonymous node, no reasoning will be done: " + stmtString(stmt));
				 return;
			 }
		} else {
			 log.warn("the object of this removed sameAs statement is not a resource, no reasoning will be done: " + stmtString(stmt));
			 return;
		}
		
		List<Resource> sameIndividuals = getSameIndividuals(subject,inferenceModel);
		sameIndividuals.addAll(getSameIndividuals(object, inferenceModel));
		
		Iterator<Resource> sIter1 = sameIndividuals.iterator();
		while (sIter1.hasNext()) {
		    removeInferencesForIndividual(sIter1.next(), inferenceModel);			
		}
		
		Iterator<Resource> sIter2 = sameIndividuals.iterator();
		while (sIter2.hasNext()) {
		    computeInferencesForIndividual(sIter2.next(), inferenceModel);			
		}
	}

	/*
	 * 
	 * Materializes inferences based on the owl:inverseOf relationship.
	 * and owl:sameAs
	 *  
	 * If it is added that x prop1 y, and prop2 is an inverseOf prop1
	 * then add y prop2 x to the inference graph, if it is not already in
	 * the assertions graph.
	 * 
	 */
	protected void addedABoxAssertion(Statement stmt, Model inferenceModel) {
		
		List<OntProperty> inverseProperties = getInverseProperties(stmt);	
        Iterator<OntProperty> inverseIter = inverseProperties.iterator();
		        
	    while (inverseIter.hasNext()) {
	       Property inverseProp = inverseIter.next();
	       Statement infStmt = ResourceFactory.createStatement(stmt.getObject().asResource(), inverseProp, stmt.getSubject());
	       addInference(infStmt,inferenceModel,true);
	    }	
	    
        List<Resource> sameIndividuals = getSameIndividuals(stmt.getSubject().asResource(), inferenceModel);
		Iterator<Resource> sameIter = sameIndividuals.iterator();
		while (sameIter.hasNext()) {
			Resource subject = sameIter.next();
			Statement sameStmt = ResourceFactory.createStatement(subject,stmt.getPredicate(),stmt.getObject());
			addInference(sameStmt,inferenceModel,false);
		}	    

		inferenceModel.enterCriticalSection(Lock.WRITE);
		try {
			if (inferenceModel.contains(stmt)) {
				inferenceModel.remove(stmt);
			}
		} finally {
			inferenceModel.leaveCriticalSection();
		}
	}	
			
	/*
	 * Performs incremental property-based reasoning.
	 * 
	 * Retracts inferences based on the owl:inverseOf relationship.
	 * 
	 * If it is removed that x prop1 y, and prop2 is an inverseOf prop1
	 * then remove y prop2 x from the inference graph, unless it is 
	 * otherwise entailed by the assertions graph independently of
	 * this removed statement.
	 */
	protected void removedABoxAssertion(Statement stmt, Model inferenceModel) {
		
		List<OntProperty> inverseProperties = getInverseProperties(stmt);	
	    Iterator<OntProperty> inverseIter = inverseProperties.iterator();
		
	    while (inverseIter.hasNext()) {
	        OntProperty inverseProp = inverseIter.next();
	        Statement infStmt = ResourceFactory.createStatement(stmt.getObject().asResource(), inverseProp, stmt.getSubject());
	        removeInference(infStmt,inferenceModel);
	    }	   
	
	    List<Resource> sameIndividuals = getSameIndividuals(stmt.getSubject().asResource(), inferenceModel);
		Iterator<Resource> sameIter = sameIndividuals.iterator();	 
		while (sameIter.hasNext()) {
			 Statement stmtSame = ResourceFactory.createStatement(sameIter.next(), stmt.getPredicate(), stmt.getObject());
			 removeInference(stmtSame,inferenceModel,false,true);
		}		 
	    	    
		 // if a statement has been removed that is otherwise entailed,
		 // add it to the inference graph.
	    inferenceModel.enterCriticalSection(Lock.WRITE);
	    try {
			 if (entailedStatement(stmt) && !inferenceModel.contains(stmt)) {
				inferenceModel.add(stmt);
			 }			 
	    } finally {
	    	inferenceModel.leaveCriticalSection();
	    }	    
	}

	/*
	 * If it is added that B is a subClass of A, then for each
	 * individual that is typed as B, either in the ABox or in the
	 * inferred model, infer that it is of type A.
	 */
	protected void addedSubClass(OntClass subClass, OntClass superClass, Model inferenceModel) {
		//log.debug("subClass = " + subClass.getURI() + " superClass = " + superClass.getURI());
		OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		unionModel.addSubModel(aboxModel);
		unionModel.addSubModel(inferenceModel);
	    List<Resource> subjectList = new ArrayList<Resource>();
		aboxModel.enterCriticalSection(Lock.READ);
		try {
			StmtIterator iter = unionModel.listStatements((Resource) null, RDF.type, subClass);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
	            subjectList.add(stmt.getSubject());
	        }
		} finally {
			aboxModel.leaveCriticalSection();
		}
		
	    for (Resource subject : subjectList) {
			Statement infStmt = ResourceFactory.createStatement(subject, RDF.type, superClass);	
		    addInference(infStmt, inferenceModel, true);	
			setMostSpecificTypes(infStmt.getSubject(), inferenceModel, new HashSet<String>());
	    }
	}

	/*
	 * If removed that B is a subclass of A, then for each individual
	 * that is of type B, either inferred or in the ABox, remove the
	 * assertion that it is of type A from the inferred model,
	 * UNLESS the individual is of some type C that is a subClass 
	 * of A (including A itself)
	 */
	protected void removedSubClass(OntClass subClass, OntClass superClass, Model inferenceModel) {
		OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		unionModel.addSubModel(aboxModel);
		unionModel.addSubModel(inferenceModel);
		List<Resource> subjectList = new ArrayList<Resource>();
	    aboxModel.enterCriticalSection(Lock.READ);
	    try {		
		    StmtIterator iter = unionModel.listStatements((Resource) null, RDF.type, subClass);
		    while (iter.hasNext()) {
			    Statement stmt = iter.next();
			    subjectList.add(stmt.getSubject());	
	        }
	    } finally {
	        aboxModel.leaveCriticalSection();
	    }
	    
	    for (Resource ind : subjectList) {
			if (entailedType(ind,superClass)) {
				continue;
			}
			Statement infStmt = ResourceFactory.createStatement(ind, RDF.type, superClass);
			inferenceModel.enterCriticalSection(Lock.WRITE);
			try {
			    if (inferenceModel.contains(infStmt)) {
				    inferenceModel.remove(infStmt);
			    } 
			    
	        } finally {
	            inferenceModel.leaveCriticalSection();
	        }
			
			setMostSpecificTypes(ind, inferenceModel, new HashSet<String>());
		}
	}

	/*
	 * If it is added that P is an inverse of Q, then:
	 *  1. For each statement involving predicate P in
	 *     the assertions model add the inverse statement
	 *     to the inference model if that inverse is
	 *     in the assertions model.
	 *      	      
	 *  2. Repeat the same for predicate Q.
	 *  
	 */
	protected void addedInverseProperty(OntProperty prop1, OntProperty prop2, Model inferenceModel) {
		
		if ( !prop1.isObjectProperty() || !prop2.isObjectProperty() ) {
		   log.warn("The subject and object of the inverseOf statement are not both object properties. No inferencing will be performed. property 1: " + prop1.getURI() + " property 2:" + prop2.getURI());
		   return;
		}
		
		Model inferences = ModelFactory.createDefaultModel();		
	    inferences.add(generateInverseInferences(prop1, prop2));
	    inferences.add(generateInverseInferences(prop2, prop1));
	    
		if (inferences.isEmpty()) return;
				
		StmtIterator iter = inferences.listStatements();
		
		while (iter.hasNext()) {
			Statement infStmt = iter.next();
			addInference(infStmt, inferenceModel, true);
		}
	}

	/*
	 * If it is removed that P is an inverse of Q, then:
	 *  1. For each statement involving predicate P in 
	 *     the abox assertions model remove the inverse
	 *     statement from the inference model unless
	 *     that statement is otherwise entailed.
	 *      	      
	 *  2. Repeat the same for predicate Q.
	 */
	protected void removedInverseProperty(OntProperty prop1, OntProperty prop2, Model inferenceModel) {
		
		if ( !prop1.isObjectProperty() || !prop2.isObjectProperty() ) {
		   log.warn("The subject and object of the inverseOf statement are not both object properties. No inferencing will be performed. property 1: " + prop1.getURI() + " property 2:" + prop2.getURI());
		   return;
		}
				
		Model inferences = ModelFactory.createDefaultModel();
	    inferences.add(generateInverseInferences(prop1, prop2));
	    inferences.add(generateInverseInferences(prop2, prop1));
				
		if (inferences.isEmpty()) return;
					
		StmtIterator iter = inferences.listStatements();
			
		while (iter.hasNext()) {
			Statement infStmt = iter.next();
					
			if (entailedStatement(infStmt)) {
				continue;
			}
			
			removeInference(infStmt,inferenceModel);
		}
	}

	/*
	 * Get a list of individuals the same as the given individual
	 */
	protected List<Resource> getSameIndividuals(Resource ind, Model inferenceModel) {	
				
		OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		unionModel.addSubModel(aboxModel);
		unionModel.addSubModel(inferenceModel);
	
		ArrayList<Resource> sameIndividuals = new ArrayList<Resource>();
		aboxModel.enterCriticalSection(Lock.READ);
		try {
			inferenceModel.enterCriticalSection(Lock.READ);
			try {
				Iterator<Statement> iter = unionModel.listStatements(ind, OWL.sameAs, (RDFNode) null);
				
				while (iter.hasNext()) {
					Statement stmt = iter.next();
					if (stmt.getObject() == null || !stmt.getObject().isResource() || stmt.getObject().asResource().getURI() == null) continue;
					sameIndividuals.add(stmt.getObject().asResource());
				}
			} finally {
				inferenceModel.leaveCriticalSection();
			}			
		} finally {
			aboxModel.leaveCriticalSection();
		}
		
		return sameIndividuals;
	}

	protected void generateSameAsInferences(Resource ind1, Resource ind2, Model inferenceModel) {	
		
		OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		unionModel.addSubModel(aboxModel);
		unionModel.addSubModel(inferenceModel);
		
			aboxModel.enterCriticalSection(Lock.READ);
			try {
				Iterator<Statement> iter = unionModel.listStatements(ind1, (Property) null, (RDFNode) null);
				while (iter.hasNext()) {
					Statement stmt = iter.next();
					if (stmt.getObject() == null) continue;
					Statement infStmt = ResourceFactory.createStatement(ind2,stmt.getPredicate(),stmt.getObject());
					addInference(infStmt, inferenceModel,true);
				}
			} finally {
				aboxModel.leaveCriticalSection();
			}
		
		return;
	}

	/*
	 * Remove inferences for individual
	 */
	protected void removeInferencesForIndividual(Resource ind, Model inferenceModel) {	
		
		Model individualInferences = ModelFactory.createDefaultModel();
		
		inferenceModel.enterCriticalSection(Lock.READ);
		try {
			Iterator<Statement> iter = inferenceModel.listStatements(ind, (Property) null, (RDFNode) null);
			
			while (iter.hasNext()) {
				individualInferences.add(iter.next());
			}
		} finally {
			inferenceModel.leaveCriticalSection();
		}

		inferenceModel.enterCriticalSection(Lock.WRITE);
		try {
			inferenceModel.remove(individualInferences);
		} finally {
			inferenceModel.leaveCriticalSection();
		}
		
		return;
	}

	/*
	 * compute inferences for individual
	 */
	protected void computeInferencesForIndividual(Resource ind, Model inferenceModel) {	
				
		Iterator<Statement> iter = null;
		aboxModel.enterCriticalSection(Lock.WRITE);
		try {
		    iter = aboxModel.listStatements(ind, (Property) null, (RDFNode) null);
		} finally {
			aboxModel.leaveCriticalSection();
		}
		
		while (iter.hasNext()) {
		   Statement stmt = iter.next();		
		   addedStatement(stmt);	
		}
			
		return;
	}

	// Returns true if it is entailed by class subsumption that
	// subject is of type cls; otherwise returns false.
	protected boolean entailedType(Resource subject, OntClass cls) {

		List<Resource> sameIndividuals = getSameIndividuals(subject,inferenceModel);
		sameIndividuals.add(subject);
				
		tboxModel.enterCriticalSection(Lock.READ);
		try {
			aboxModel.enterCriticalSection(Lock.READ);
			try {			
				List<OntClass> subclasses = null;
				subclasses = (cls.listSubClasses(false)).toList();		
				subclasses.addAll((cls.listEquivalentClasses()).toList());
				Iterator<OntClass> iter = subclasses.iterator();
				while (iter.hasNext()) {		
					OntClass childClass = iter.next();
					if (childClass.equals(cls)) continue;  // TODO - determine whether this is needed
					Iterator<Resource> sameIter = sameIndividuals.iterator();
					while (sameIter.hasNext()) {
						Statement stmt = ResourceFactory.createStatement(sameIter.next(), RDF.type, childClass);
						if (aboxModel.contains(stmt)) {
						   return true;					
						}
					}
				}
				return false;
			} finally { 
				aboxModel.leaveCriticalSection();
			}	
		} finally {
			tboxModel.leaveCriticalSection();			
		}
	}
		
	// Returns true if the triple is entailed by inverse property
	// reasoning or sameAs reasoning; otherwise returns false.
	protected boolean entailedStatement(Statement stmt) {	
		
		//TODO think about checking class subsumption here
		
		// Inverse properties
		List<OntProperty> inverses = getInverseProperties(stmt);
		Iterator<OntProperty> iIter = inverses.iterator();
		if (iIter.hasNext()) {
			aboxModel.enterCriticalSection(Lock.READ);
			try {						
				while (iIter.hasNext()) {		
					Property invProp = iIter.next();

					Statement invStmt = ResourceFactory.createStatement(stmt.getObject().asResource(), invProp, stmt.getSubject());
					if (aboxModel.contains(invStmt)) {
						return true;
					}
				}
			} finally { 
				aboxModel.leaveCriticalSection();
			}	
		}
		
		// individuals sameAs each other
		List<Resource> sameIndividuals = getSameIndividuals(stmt.getSubject().asResource(),inferenceModel);
		Iterator<Resource> rIter = sameIndividuals.iterator();
		if (rIter.hasNext()) {
			aboxModel.enterCriticalSection(Lock.READ);
			try {						
				while (rIter.hasNext()) {		
					Resource subject = rIter.next();
	
					if (aboxModel.contains(subject, stmt.getPredicate(), stmt.getObject())) {
						return true;
					}
				}
			} finally { 
				aboxModel.leaveCriticalSection();
			}	
		}
		
		return false;
	}
	
	/*
	 * Returns a list of properties that are inverses of the property
	 * in the given statement. 
	 */
	protected List<OntProperty> getInverseProperties(Statement stmt) {
		
		List<OntProperty> inverses = new ArrayList<OntProperty>();
		
		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
			return inverses;
		}
		
		tboxModel.enterCriticalSection(Lock.READ);
		try {
			
			OntProperty prop = tboxModel.getOntProperty(stmt.getPredicate().getURI()); 
			
			if (prop != null) {	
				if (!prop.isObjectProperty()) {
					return inverses;
				}
				
				if (!stmt.getObject().isResource()) {
					log.warn("The predicate of this statement is an object property, but the object is not a resource.");
					return inverses;
				}
				
				// not reasoning on properties in the OWL, RDF or RDFS namespace
				if ((prop.getNameSpace()).equals(OWL.NS) || 
					(prop.getNameSpace()).equals(RDFS.getURI()) ||
					(prop.getNameSpace()).equals(RDF.getURI())) {
					return inverses;
				}
			
			    ExtendedIterator <? extends OntProperty> iter = prop.listInverse();
			
			    while (iter.hasNext()) {
			       OntProperty invProp = iter.next();		
			   
			       if ((invProp.getNameSpace()).equals(OWL.NS) || 
				       (invProp.getNameSpace()).equals(RDFS.getURI()) ||
				       (invProp.getNameSpace()).equals(RDF.getURI())) {
					 continue;
			       } 
			       inverses.add(invProp);
			    }
			}   
		 } finally {
			tboxModel.leaveCriticalSection();
		 }
		
		return inverses;
	}	
	protected Model generateInverseInferences(OntProperty prop, OntProperty inverseProp) {
		Model inferences = ModelFactory.createDefaultModel();

		aboxModel.enterCriticalSection(Lock.READ);
		try {
			StmtIterator iter = aboxModel.listStatements((Resource) null, prop, (RDFNode) null);
			
			while (iter.hasNext()) {		
				Statement stmt = iter.next();		
				if (!stmt.getObject().isResource()) continue;
				Statement infStmt = ResourceFactory.createStatement(stmt.getObject().asResource(), inverseProp, stmt.getSubject());
				inferences.add(infStmt);
			}				
	    } finally {
		    aboxModel.leaveCriticalSection();
	    }
		
	    return inferences;	
	}

	/*
	 * Add an inference from the inference model
	 * 
	 * Adds the inference to the inference model if it is not already in
	 * the inference model and not in the abox model.
	 */
	
	public void addInference(Statement infStmt, Model inferenceModel) {	
        addInference(infStmt,inferenceModel,true);
	}
	
	protected void addInference(Statement infStmt, Model inferenceModel, boolean handleSameAs) {
		
		aboxModel.enterCriticalSection(Lock.READ);
		try {
			inferenceModel.enterCriticalSection(Lock.WRITE);
			try {
				if (!inferenceModel.contains(infStmt) && !aboxModel.contains(infStmt))  {
					inferenceModel.add(infStmt);
			    }
		
				if (handleSameAs) {
					List<Resource> sameIndividuals = getSameIndividuals(infStmt.getSubject().asResource(), inferenceModel);
					Iterator<Resource> sameIter = sameIndividuals.iterator();
					while (sameIter.hasNext()) {
						Resource subject = sameIter.next();
						
						Statement sameStmt = ResourceFactory.createStatement(subject,infStmt.getPredicate(),infStmt.getObject());
						if (subject.equals(infStmt.getObject()) && OWL.sameAs.equals(infStmt.getPredicate())) {
							continue;
						}
						
						if (!inferenceModel.contains(sameStmt) && !aboxModel.contains(sameStmt)) {
							inferenceModel.add(sameStmt);
						}
					}				
				}				
			} finally {
				inferenceModel.leaveCriticalSection();
			}
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}

	/*
	 * Remove an inference from the inference model
	 * 
	 * Removes the inference if it is not entailed by the abox model
	 * and if the inference model contains it.
	 * 
	 * Removes the corresponding inference for each same individual
	 * if that inference is not entailed by the abox model. 
	 */
	public void removeInference(Statement infStmt, Model inferenceModel) {
       removeInference(infStmt,inferenceModel,true,true);
	}

	protected void removeInference(Statement infStmt, Model inferenceModel, boolean handleSameAs, boolean checkEntailment) {
		
		inferenceModel.enterCriticalSection(Lock.WRITE);		
		try {	
		   if ( (!checkEntailment || !entailedStatement(infStmt)) && inferenceModel.contains(infStmt)) {
			   inferenceModel.remove(infStmt);
		   } 		   
        } finally {
	       inferenceModel.leaveCriticalSection();	
        }
		
		if (handleSameAs) {
			inferenceModel.enterCriticalSection(Lock.WRITE);		
			try {	
			    List<Resource> sameIndividuals = getSameIndividuals(infStmt.getSubject().asResource(), inferenceModel);
			   
			    Iterator<Resource> sameIter = sameIndividuals.iterator();	 
			    while (sameIter.hasNext()) {
				  Statement infStmtSame = ResourceFactory.createStatement(sameIter.next(), infStmt.getPredicate(), infStmt.getObject());
				  if ((!checkEntailment || !entailedStatement(infStmtSame)) && inferenceModel.contains(infStmtSame)) { 
					inferenceModel.remove(infStmtSame);
				  }					 		   
			    }
			} finally {
				inferenceModel.leaveCriticalSection();
			}
		}	
	}

	/*
     * Find the most specific types (classes) of an individual and
     * infer them for the individual with the mostSpecificType
     * annotation.
	 */
	protected void setMostSpecificTypes(Resource individual, Model inferenceModel, HashSet<String> unknownTypes) {
			
		tboxModel.enterCriticalSection(Lock.READ);
		aboxModel.enterCriticalSection(Lock.READ);
		inferenceModel.enterCriticalSection(Lock.READ);
		HashSet<String> typeURIs = new HashSet<String>();
		
		try {
			OntModel unionModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
			unionModel.addSubModel(aboxModel);
			unionModel.addSubModel(inferenceModel);
					
			List<OntClass> types = new ArrayList<OntClass>();
						
			StmtIterator stmtIter = unionModel.listStatements(individual, RDF.type, (RDFNode) null);
			
			while (stmtIter.hasNext()) {
				
				Statement stmt = stmtIter.next();
				
				if ( !stmt.getObject().isResource() ) {
					log.warn("The object of this rdf:type assertion is expected to be a resource: " + stmtString(stmt));
					continue;
				}
				
				OntClass ontClass = null;
				
				if ( (stmt.getObject().asResource()).getURI() != null ) {
				    ontClass = tboxModel.getOntClass(stmt.getObject().asResource().getURI()); 
				} else {
					log.warn("The object of this rdf:type assertion has a null URI: " + stmtString(stmt));
					continue;
				}
				 
				if (ontClass == null) {
					if ( !(stmt.getObject().asResource().getNameSpace()).equals(OWL.NS)) {
						if (!unknownTypes.contains(stmt.getObject().asResource().getURI())) {
						   unknownTypes.add(stmt.getObject().asResource().getURI());
					       log.warn("Didn't find the target class (the object of an asserted or inferred rdf:type statement) in the TBox: " +
						          	(stmt.getObject().asResource()).getURI() + ". No mostSpecificType computation will be done based on " + (stmt.getObject().asResource()).getURI() + " type statements.");
						}
					}
					continue;
				}
					
				if (ontClass.isAnon()) continue;
				
				types.add(ontClass); 
			}
	
			List<OntClass> types2 = new ArrayList<OntClass>();
			types2.addAll(types);
			
			Iterator<OntClass> typeIter = types.iterator();
			
			while (typeIter.hasNext()) {
			    OntClass type = typeIter.next();
			    			    
			    boolean add = true;
			    Iterator<OntClass> typeIter2 = types2.iterator();
			    while (typeIter2.hasNext()) {
			    	OntClass type2 = typeIter2.next();
			    				    	
			    	if (type.equals(type2)) { 
			    		continue;
			    	}
			    	
			    	if (type.hasSubClass(type2, false) && !type2.hasSubClass(type, false)) {
			    		add = false;
			    		break;
			    	}
			    }	
			    
			    if (add) {
			    	typeURIs.add(type.getURI());
			    	
		            Iterator<OntClass> eIter = type.listEquivalentClasses();
		                
		            while (eIter.hasNext()) {
		                OntClass equivClass = eIter.next();
		                if (equivClass.isAnon()) continue;
		                typeURIs.add(equivClass.getURI());
		            }    
			    }    	
			}
		} finally {
			inferenceModel.leaveCriticalSection();
			aboxModel.leaveCriticalSection();
			tboxModel.leaveCriticalSection();
		}
	
		setMostSpecificTypes(individual, typeURIs, inferenceModel);
	    return;	
	}
	
	protected void setMostSpecificTypes(Resource individual, HashSet<String> typeURIs, Model inferenceModel) {
		
	    Model retractions = ModelFactory.createDefaultModel();
	    
		inferenceModel.enterCriticalSection(Lock.READ);
		try {
			// remove obsolete mostSpecificType assertions
			StmtIterator iter = inferenceModel.listStatements(individual, mostSpecificType, (RDFNode) null);
			
			while (iter.hasNext()) {
				Statement stmt = iter.next();

				if ( !stmt.getObject().isResource() ) {
					log.warn("The object of this assertion is expected to be a resource: " + stmtString(stmt));
					continue;
				}
				
				if (!typeURIs.contains(stmt.getObject().asResource().getURI())) {
					retractions.add(stmt);
				}
			}
		} finally {
			inferenceModel.leaveCriticalSection();
		}	
		
		Iterator<Statement> rIter = retractions.listStatements();
		while (rIter.hasNext()) {
			removeInference(rIter.next(), inferenceModel, true, false);
		}	
		
		Iterator<String> typeIter = typeURIs.iterator();		
		while (typeIter.hasNext()) {
			String typeURI = typeIter.next();
			Statement mstStmt = ResourceFactory.createStatement(individual,mostSpecificType,ResourceFactory.createResource(typeURI));
			addInference(mstStmt,inferenceModel,true);
		}		
		
        return;
	}
		
	// system-configured reasoning modules (plugins)
	protected boolean isInterestedInRemovedStatement(Statement stmt) {
		
		if (stmt.getPredicate().equals(RDF.type)) return true;
	
		for (ReasonerPlugin plugin : getPluginList()) {
			if (plugin.isInterestedInRemovedStatement(stmt)) return true;
		}
		
	    return false;
	}

	protected void doPlugins(ModelUpdate.Operation op, Statement stmt) {
		
		for (ReasonerPlugin plugin : getPluginList()) {
			try {
				switch (op) {
				  case ADD: 
				     if (plugin.isInterestedInAddedStatement(stmt)) {
					    plugin.addedABoxStatement(stmt, aboxModel, inferenceModel, tboxModel);
				     }
				     break;
				  case RETRACT: 
					     if (plugin.isInterestedInRemovedStatement(stmt)) {
						    plugin.removedABoxStatement(stmt, aboxModel, inferenceModel, tboxModel);
					     }	
					     break;
				}
			} catch (Exception e) {
				log.error("Exception while processing " + (op == ModelUpdate.Operation.ADD ? "an added" : "a removed") + 
						" statement in SimpleReasoner plugin:" + plugin.getClass().getName() + " -- " + e.getMessage());
			}
		}
	}

	/**
	 * Returns true if the reasoner is in the process of recomputing all
	 * inferences.
	 */
	private boolean recomputing = false;
	
	public boolean isRecomputing() {
	    return recomputing;
	}
	
	/**
	 * Recompute all inferences.
	 */
	public synchronized void recompute() {
	    recomputing = true;
	    try {
	        recomputeABox();
	    } finally {
	        recomputing = false;
	    }
	}

	/*
	 * Recompute the entire ABox inference graph. The new 
	 * inference graph is built in a separate model and
	 * then reconciled with the inference graph in active
	 * use. The model reconciliation must be done
	 * without reading the whole inference models into 
	 * memory in order to support very large ABox 
	 * inference models.	  
	 */
	protected synchronized void recomputeABox() {
			
		// recompute class subsumption inferences 
		inferenceRebuildModel.enterCriticalSection(Lock.WRITE);			
		try {
			HashSet<String> unknownTypes = new HashSet<String>();
			inferenceRebuildModel.removeAll();
			
			log.info("Computing class subsumption ABox inferences.");
			int numStmts = 0;
			ArrayList<String> individuals = this.getAllIndividualURIs();
			
			for (String individualURI : individuals) {			
				Resource individual = ResourceFactory.createResource(individualURI);
				
				try {
					addedABoxTypeAssertion(individual, inferenceRebuildModel, unknownTypes);
					setMostSpecificTypes(individual, inferenceRebuildModel, unknownTypes);
					StmtIterator sit = aboxModel.listStatements(individual, null, (RDFNode) null);
					while (sit.hasNext()) {
						Statement s = sit.nextStatement();
						for (ReasonerPlugin plugin : getPluginList()) {
							plugin.addedABoxStatement(s, aboxModel, inferenceRebuildModel, tboxModel);
						}
					}
				} catch (NullPointerException npe) {
	            	log.error("a NullPointerException was received while recomputing the ABox inferences. Halting inference computation.");
	                return;
				} catch (JenaException je) {
					 if (je.getMessage().equals("Statement models must no be null")) {
						 log.error("Exception while recomputing ABox inference model. Halting inference computation.", je);
		                 return; 
					 } 
					 log.error("Exception while recomputing ABox inference model: ", je);
				} catch (Exception e) {
					 log.error("Exception while recomputing ABox inference model: ", e);
				}
				
				numStmts++;
	            if ((numStmts % 10000) == 0) {
	                log.info("Still computing class subsumption ABox inferences...");
	            }
	            
	            if (stopRequested) {
	            	log.info("a stopRequested signal was received during recomputeABox. Halting Processing.");
	            	return;
	            }
			}
			
			log.info("Finished computing class subsumption ABox inferences");
			log.info("Computing inverse property ABox inferences");
			
			Iterator<Statement> invStatements = null;
			tboxModel.enterCriticalSection(Lock.READ);
			try {
			    invStatements = tboxModel.listStatements((Resource) null, OWL.inverseOf, (Resource) null);
			} finally {
			    tboxModel.leaveCriticalSection();	
			}
			
			numStmts = 0;
			while (invStatements.hasNext()) {				
				Statement stmt = invStatements.next();
												
				try {
					OntProperty prop1 = tboxModel.getOntProperty((stmt.getSubject()).getURI());
					if (prop1 == null) {
						//TODO make sure not to print out a million of these for the same property
						log.debug("didn't find subject property in the tbox: " + (stmt.getSubject()).getURI());
						continue;
					}
					
					OntProperty prop2 = tboxModel.getOntProperty(((Resource)stmt.getObject()).getURI()); 
					if (prop2 == null) {
						//TODO make sure not to print out a million of these for the same property
						log.debug("didn't find object property in the tbox: " + ((Resource)stmt.getObject()).getURI());
						continue;
					}
					
					addedInverseProperty(prop1, prop2, inferenceRebuildModel);
				} catch (NullPointerException npe) {
	            	log.error("a NullPointerException was received while recomputing the ABox inferences. Halting inference computation.");
	                return;
				} catch (JenaException je) {
					 if (je.getMessage().equals("Statement models must no be null")) {
						 log.error("Exception while recomputing ABox inference model. Halting inference computation.", je);
		                 return; 
					 } 
					 log.error("Exception while recomputing ABox inference model: ", je);
				} catch (Exception e) {
					 log.error("Exception while recomputing ABox inference model: ", e);
				}
				
				numStmts++;
	            if ((numStmts % 10000) == 0) {
	                log.info("Still computing inverse property ABox inferences...");
	            }
	            
	            if (stopRequested) {
	            	log.info("a stopRequested signal was received during recomputeABox. Halting Processing.");
	            	return;
	            }
			}
			
			log.info("Finished computing inverse property ABox inferences");
			log.info("Computing sameAs ABox inferences");
			
			Iterator<Statement> sameAsStatements = null;
			aboxModel.enterCriticalSection(Lock.READ);
			try {
			    sameAsStatements = aboxModel.listStatements((Resource) null, OWL.sameAs, (Resource) null);
			} finally {
			    aboxModel.leaveCriticalSection();	
			}
			
			numStmts = 0;
			while (sameAsStatements.hasNext()) {				
				Statement stmt = sameAsStatements.next();
												
				try {
					addedABoxSameAsAssertion(stmt, inferenceRebuildModel); 
				} catch (NullPointerException npe) {
	            	log.error("a NullPointerException was received while recomputing the ABox inferences. Halting inference computation.");
	                return;
				} catch (JenaException je) {
					 if (je.getMessage().equals("Statement models must no be null")) {
						 log.error("Exception while recomputing ABox inference model. Halting inference computation.", je);
		                 return; 
					 } 
					 log.error("Exception while recomputing ABox inference model: ", je);
				} catch (Exception e) {
					 log.error("Exception while recomputing ABox inference model: ", e);
				}
				
				numStmts++;
	            if ((numStmts % 10000) == 0) {
	                log.info("Still computing sameAs ABox inferences...");
	            }
	            
	            if (stopRequested) {
	            	log.info("a stopRequested signal was received during recomputeABox. Halting Processing.");
	            	return;
	            }
			}
			log.info("Finished computing sameAs ABox inferences");	
			
			try {
				if (updateInferenceModel(inferenceRebuildModel)) {
		        	log.info("a stopRequested signal was received during updateInferenceModel. Halting Processing.");
		        	return;
				}
			} catch (Exception e) {
				log.error("Exception while reconciling the current and recomputed ABox inference model for class subsumption inferences. Halting processing." , e);
			}			
		} catch (Exception e) {
			log.error("Exception while recomputing ABox inferences. Halting processing.", e);
		} finally {
			inferenceRebuildModel.removeAll();
			inferenceRebuildModel.leaveCriticalSection();
		}		
	}
	
	/*
	 * Get the URIs for all individuals in the system
	 */
	protected ArrayList<String> getAllIndividualURIs() {
	    
		String queryString = "select distinct ?subject where {?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type}";
	    return getIndividualURIs(queryString);
	}

	protected ArrayList<String> getIndividualURIs(String queryString) {
	    
		ArrayList<String> individuals = new ArrayList<String>();
		aboxModel.enterCriticalSection(Lock.READ);	
		
		try {
			try {			
				Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
				QueryExecution qe = QueryExecutionFactory.create(query, aboxModel);
				
				ResultSet results = qe.execSelect();
	            
				while (results.hasNext()) {
					QuerySolution solution = results.next();
					Resource resource = solution.getResource("subject");
					
					if ((resource != null) && !resource.isAnon()) {
						individuals.add(resource.getURI());
					}					
				}
				
		   	} catch (Exception e) {
				log.error("exception while retrieving list of individuals ",e);
			}	
		} finally {
			aboxModel.leaveCriticalSection();
		}
		
		return individuals;
	}

	/*
	 * reconcile a set of inferences into the application inference model
	 */
	protected boolean updateInferenceModel(Model inferenceRebuildModel) {
					
    log.info("Updating ABox inference model");
	StmtIterator iter = null;
 
	// Remove everything from the current inference model that is not
	// in the recomputed inference model	
    int num = 0;
	scratchpadModel.enterCriticalSection(Lock.WRITE);
	scratchpadModel.removeAll();
	try {
		inferenceModel.enterCriticalSection(Lock.READ);
		try {
			iter = inferenceModel.listStatements();
			
			while (iter.hasNext()) {				
				Statement stmt = iter.next();
				if (!inferenceRebuildModel.contains(stmt)) {
				   scratchpadModel.add(stmt);  
				}
				
				num++;
                if ((num % 10000) == 0) {
                    log.info("Still updating ABox inference model (removing outdated inferences)...");
                }
                
                if (stopRequested) {
                	return true;
                }
			}
		} finally {
			iter.close();
            inferenceModel.leaveCriticalSection();
		}
		
		try {
			iter = scratchpadModel.listStatements();
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				try {
					inferenceModel.remove(stmt);
				} finally {
					inferenceModel.leaveCriticalSection();
				}
			}
		} finally {
			iter.close();
		}
					
		// Add everything from the recomputed inference model that is not already
		// in the current inference model to the current inference model.	
		try {
			scratchpadModel.removeAll();
			iter = inferenceRebuildModel.listStatements();
			
			while (iter.hasNext()) {				
				Statement stmt = iter.next();
				
				inferenceModel.enterCriticalSection(Lock.READ);
				try {
					if (!inferenceModel.contains(stmt)) {
						 scratchpadModel.add(stmt);
					}
				} finally {
				     inferenceModel.leaveCriticalSection();	
				}
									
				num++;
                if ((num % 10000) == 0) {
                    log.info("Still updating ABox inference model (adding new inferences)...");
                }
                
                if (stopRequested) {
                	return true;
                }
			}
		} finally {
			iter.close();	
		}
					
		iter = scratchpadModel.listStatements();
		while (iter.hasNext()) {
			Statement stmt = iter.next();
			
			inferenceModel.enterCriticalSection(Lock.WRITE);
			try {
				inferenceModel.add(stmt);
			} finally {
				inferenceModel.leaveCriticalSection();
			}
		}
	} finally {
		iter.close();
		scratchpadModel.removeAll();
		scratchpadModel.leaveCriticalSection();			
	}
	
	log.info("ABox inference model updated");
	return false;
	}
	
	/**
	 * This is called when the application shuts down.
	 */
	public void setStopRequested() {
	    this.stopRequested = true;
	}
	
	/*
	 * Utility method for logging
	 */
    public static String stmtString(Statement statement) {
    	return  " [subject = " + statement.getSubject().getURI() +
    			"] [property = " + statement.getPredicate().getURI() +
                "] [object = " + (statement.getObject().isLiteral() ? ((Literal)statement.getObject()).getLexicalForm() + " (Literal)"
                		                                          : ((Resource)statement.getObject()).getURI() + " (Resource)") + "]";	
    }  
	  
    // DeltaComputer    
	/*
	 * Asynchronous reasoning mode (DeltaComputer) is used in the case of batch removals. 
	 */
	public boolean isABoxReasoningAsynchronous() {
         if (batchMode1 || batchMode2) {
        	 return true;
         } else {
        	 return false;
         }
	}
    
	private volatile boolean deltaComputerProcessing = false;
	private int eventCount = 0;
    
	@Override
	public void notifyEvent(Model model, Object event) {
	    
	    if (event instanceof BulkUpdateEvent) {	
	    	handleBulkUpdateEvent(event);
	    }
	}
		
	public synchronized void handleBulkUpdateEvent(Object event) {
	    
	    if (event instanceof BulkUpdateEvent) {	
	    	if (((BulkUpdateEvent) event).getBegin()) {	
	    		
	    		log.info("received a bulk update begin event");
	    		if (deltaComputerProcessing) {
	    			eventCount++;
	    			log.info("received a bulk update begin event while processing in asynchronous mode. Event count = " + eventCount);
	    			return;  
	    		} else {
	    			batchMode1 = true;
	    			batchMode2 = false;
	    			
	    	    	if (aBoxDeltaModeler1.getRetractions().size() > 0) {
	    	     	   log.warn("Unexpected condition: the aBoxDeltaModeler1 retractions model was not empty when entering batch mode.");
	    	     	}

	    	     	if (aBoxDeltaModeler2.getRetractions().size() > 0) {
	    	      	   log.warn("Unexpected condition: the aBoxDeltaModeler2 retractions model was not empty when entering batch mode.");
	    	      	}
	    			    			
	    			log.info("initializing batch mode 1");
	    		}
	    	} else {
	    		log.info("received a bulk update end event");
	    		if (!deltaComputerProcessing) {
	    		    deltaComputerProcessing = true;
	    		    new Thread(new DeltaComputer(),"DeltaComputer").start();
	    		} else {
	    			eventCount--;
	    			log.info("received a bulk update end event while currently processing in aynchronous mode. Event count = " + eventCount);
	    		}
	    	}
	    }
	}
	
	private synchronized boolean switchBatchModes() {

		if (batchMode1) { 
    	   aBoxDeltaModeler2.getRetractions().removeAll();
    	   
    	   if (aBoxDeltaModeler1.getRetractions().size() > 0) { 
    	       batchMode2 = true;
    	   	   batchMode1 = false;  
			   log.info("entering batch mode 2");
    	   } else {
    		   deltaComputerProcessing = false;
    		   if (eventCount == 0) {
    			   batchMode1 = false;
    		   }
    	   }
	   } else if (batchMode2) {
			aBoxDeltaModeler1.getRetractions().removeAll();

    	    if (aBoxDeltaModeler2.getRetractions().size() > 0) { 
    	       batchMode1 = true;
    	   	   batchMode2 = false;  
			   log.info("entering batch mode 1");
    	    } else {
    		   deltaComputerProcessing = false;
    		   if (eventCount == 0) {
    			   batchMode2 = false;
    		   }
    	    }
	   } else { 
		    log.warn("unexpected condition, invoked when batchMode1 and batchMode2 are both false");
            deltaComputerProcessing = false;
	   }
       
       return deltaComputerProcessing;
	}
		
    private class DeltaComputer extends Thread  {      
        public DeltaComputer() {
        }
        
        @Override
        public void run() {  
        	log.info("starting DeltaComputer.run");
        	boolean abort = false;
       	    Model retractions = ModelFactory.createDefaultModel();
       	    String qualifier = "";
        	
        	while (deltaComputerProcessing && !stopRequested) {
        		
        		if (switchBatchModes()) {
        			if (batchMode1) {
        				qualifier = "2";
        				retractions = aBoxDeltaModeler2.getRetractions();
        			} else if (batchMode2) {
        				qualifier = "1";
        				retractions = aBoxDeltaModeler1.getRetractions();        				
        			} 
        		} else {
        			break;
        		}
        	
    			retractions.enterCriticalSection(Lock.READ);	
    			StmtIterator iter = null;
    			int num = 0;
    			
    			try {
    	   	       	log.info("started computing inferences for batch " + qualifier + " updates");
    				iter = retractions.listStatements();
    			   		
    				while (iter.hasNext() && !stopRequested) {				
    					Statement stmt = iter.next();
    				    num++;
     				    
    					try {
    						if (stmt.getPredicate().equals(RDF.type)) {
    							removedABoxTypeAssertion(stmt, inferenceModel);
    						}
    				        setMostSpecificTypes(stmt.getSubject(), inferenceModel, new HashSet<String>());
    				        doPlugins(ModelUpdate.Operation.RETRACT,stmt);
    					} catch (NullPointerException npe) {
    						 abort = true;
    						 break;
    					} catch (Exception e) {
    						 log.error("exception in batch mode ",e);
    					}
    					
		                if ((num % 6000) == 0) {
		                    log.info("still computing inferences for batch " + qualifier + " update...");
		                }	
		                
		                if (stopRequested) {
		                	log.info("a stopRequested signal was received during DeltaComputer.run. Halting Processing.");
		                	return;
		                }
    				}
    			} finally {
    				iter.close();
    	    		retractions.removeAll();	
    	   			retractions.leaveCriticalSection();
    			}			
 				
                if (stopRequested) {
                	log.info("a stopRequested signal was received during DeltaComputer.run. Halting Processing.");
                	deltaComputerProcessing = false;
                	return;
                }
                
                if (abort) {
                	log.error("a NullPointerException was received while computing inferences in batch " + qualifier + " mode. Halting inference computation.");
                	deltaComputerProcessing = false;
                	return;
                }
                
                log.info("finished computing inferences for batch " + qualifier + " updates");
                log.debug("\t--> processed " + num + " statements");
        	}
        	
        	log.info("ending DeltaComputer.run. batchMode1 = " + batchMode1 + ", batchMode2 = " + batchMode2);
        }        
    }   
}
