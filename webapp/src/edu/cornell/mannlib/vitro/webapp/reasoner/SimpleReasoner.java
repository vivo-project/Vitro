/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import static edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel.WORKING;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * Allows for real-time incremental materialization or retraction of RDFS-
 * style class and property subsumption based ABox inferences as statements
 * are added to or removed from the (ABox or TBox) knowledge base. 
 * @author sjm222
 */

public class SimpleReasoner extends StatementListener {

	private static final Log log = LogFactory.getLog(SimpleReasoner.class);
	
	private final SearchIndexer searchIndexer;
	
	private OntModel tboxModel;             // asserted and inferred TBox axioms
	private OntModel aboxModel;             // ABox assertions
	private Model inferenceModel;           // ABox inferences
	private OntModel fullModel;             // contains at least the 
	                                        // asserted and inferred ABox
	
	private static final String mostSpecificTypePropertyURI = 
        "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType";	
	private static final AnnotationProperty mostSpecificType = (
			VitroModelFactory.createOntologyModel())
				.createAnnotationProperty(mostSpecificTypePropertyURI);
	
	// DeltaComputer
	private CumulativeDeltaModeler aBoxDeltaModeler1 = null;
	private CumulativeDeltaModeler aBoxDeltaModeler2 = null;
	private int batchMode = 0;  // values: 0, 1 and 2

	// Recomputer
	private ABoxRecomputer recomputer = null;
	
	private boolean stopRequested = false;
	
	private List<ReasonerPlugin> pluginList = new CopyOnWriteArrayList<ReasonerPlugin>();
    
    private boolean doSameAs = true;

	/**
	 * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param aboxModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) 
     *  ABox statements are maintained (added or retracted).
	 * @param inferenceRebuildModel - output. This the model is temporarily used when the 
     *  whole ABox inference model is rebuilt
	 * @param inferenceScratchpadModel - output. This the model is temporarily used when 
     *  the whole ABox inference model is rebuilt
     * @param searchIndexer - output. If not null, the indexer will be paused before the 
     *  ABox inference model is rebuilt and unpaused when the rebuild is complete.
 	 */
	public SimpleReasoner(OntModel tboxModel, 
			              RDFService rdfService, 
			              Model inferenceModel,
			              Model inferenceRebuildModel, 
			              Model scratchpadModel, 
			              SearchIndexer searchIndexer) {
		
		this.searchIndexer = searchIndexer;

		this.tboxModel = tboxModel;
		
		this.fullModel = VitroModelFactory.createOntologyModel(
                VitroModelFactory.createModelForGraph(
                        new RDFServiceGraph(rdfService)));
		
        this.aboxModel = VitroModelFactory.createOntologyModel(
                  VitroModelFactory.createModelForGraph(
                        new DifferenceGraph(new DifferenceGraph(new RDFServiceGraph(rdfService),inferenceModel.getGraph()),
                        		tboxModel.getGraph())));
                        
		this.inferenceModel = inferenceModel;
		this.batchMode = 0;
		aBoxDeltaModeler1 = new CumulativeDeltaModeler();
		aBoxDeltaModeler2 = new CumulativeDeltaModeler();
		recomputer = new ABoxRecomputer(tboxModel, aboxModel, rdfService, this, searchIndexer);
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
	 * @param inferenceModel - output. This is the model in which inferred (materialized)
     *  ABox statements are maintained (added or retracted).
 	 */
	public SimpleReasoner(OntModel tboxModel, OntModel aboxModel, Model inferenceModel) {
		this.searchIndexer = null;
		this.tboxModel = tboxModel;
		this.aboxModel = aboxModel; 
		this.inferenceModel = inferenceModel;
		this.fullModel = VitroModelFactory.createUnion(aboxModel, 
				VitroModelFactory.createOntologyModel(inferenceModel));
		aBoxDeltaModeler1 = new CumulativeDeltaModeler();
		aBoxDeltaModeler2 = new CumulativeDeltaModeler();
		this.batchMode = 0;
		stopRequested = false;
		Dataset ds = DatasetFactory.createMem();
		ds.addNamedModel(ModelNames.ABOX_ASSERTIONS, aboxModel);
		ds.addNamedModel(ModelNames.ABOX_INFERENCES, inferenceModel);
		ds.addNamedModel(ModelNames.TBOX_ASSERTIONS, tboxModel);
		
		ds.setDefaultModel(ModelFactory.createUnion(fullModel, tboxModel));
		recomputer = new ABoxRecomputer(tboxModel, aboxModel, new RDFServiceModel(ds), this, searchIndexer);
	}
	
	public void setPluginList(List<ReasonerPlugin> pluginList) {
		this.pluginList = pluginList;
	}
	
	public List<ReasonerPlugin> getPluginList() {
		return this.pluginList;
	}

    public void setSameAsEnabled( boolean tf){
        this.doSameAs = tf;
    }
    
    public boolean getSameAsEnabled() {
        return this.doSameAs;
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
			} else if ( doSameAs && stmt.getPredicate().equals(OWL.sameAs)) {  
                 addedABoxSameAsAssertion(stmt, inferenceModel); 
			} else {
				 addedABoxAssertion(stmt, inferenceModel);
			}
			
			doPlugins(ModelUpdate.Operation.ADD,stmt);

		} catch (Exception e) { // don't stop the edit if there's an exception
			log.error("Exception while computing inferences: " + e.getMessage());
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
            handleRemovedStatement(stmt);            
		} catch (Exception e) { // don't stop the edit if there's an exception
			log.error("Exception while retracting inferences: ", e);
		}
	}
	
	/*
	 * Synchronized part of removedStatement. Interacts with DeltaComputer.
	 */
	protected synchronized void handleRemovedStatement(Statement stmt) {    
		if (batchMode == 1) {
			 aBoxDeltaModeler1.removedStatement(stmt);
		} else if (batchMode == 2) {
			 aBoxDeltaModeler2.removedStatement(stmt);
		} else {  // batchMode == 0
			if (stmt.getPredicate().equals(RDF.type)) {
				removedABoxTypeAssertion(stmt, inferenceModel);
				setMostSpecificTypes(stmt.getSubject(), inferenceModel, new HashSet<String>());
			} else if ( doSameAs && stmt.getPredicate().equals(OWL.sameAs)) {
                removedABoxSameAsAssertion(stmt, inferenceModel); 	
			} else {
				removedABoxAssertion(stmt, inferenceModel);
			}
			doPlugins(ModelUpdate.Operation.RETRACT,stmt);
		}
	}

    /**
	 * Performs incremental ABox reasoning based
	 * on changes to the class hierarchy.
     *
     * addedTBoxStatement and removedTBoxStatement use the
     * same tests so the are merged into this method.
     *
	 * Handles rdfs:subclassOf, owl:equivalentClass, and owl:inverseOf 
	 */
    protected void changedTBoxStatement( Statement stmt , boolean add){
        try {		
			if (!(stmt.getPredicate().equals(RDFS.subClassOf) 
                  || stmt.getPredicate().equals(OWL.equivalentClass) 
                  || stmt.getPredicate().equals(OWL.inverseOf))) {
				return;
			}

			if (!stmt.getObject().isResource()) {
				log.warn("The object of this assertion is not a resource: " + stmtString(stmt));
				return;
			}
						
			if (stmt.getPredicate().equals(RDFS.subClassOf) 
                || stmt.getPredicate().equals(OWL.equivalentClass)) {

				// ignore anonymous classes
				if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
				    return;
				}
			
				OntClass subject, object;
				tboxModel.enterCriticalSection(Lock.READ);
				try {
    				subject = tboxModel.getOntClass((stmt.getSubject()).getURI());
    				if (subject == null) {
    					log.debug("didn't find subject class in the tbox: " 
                                  + (stmt.getSubject()).getURI());
    					return;
    				}
    				
    				object = tboxModel.getOntClass(((Resource)stmt.getObject()).getURI()); 
    				if (object == null) {
    					log.debug("didn't find object class in the tbox: " 
                                  + ((Resource)stmt.getObject()).getURI());
    					return;
    				}
				} finally {
				    tboxModel.leaveCriticalSection();
				}
				
				if (stmt.getPredicate().equals(RDFS.subClassOf)) {
                    if( add ){
					 addedSubClass(subject,object,inferenceModel);
                    }else{
                        removedSubClass( subject,object,inferenceModel);
                    }
				} else {
					 // equivalent class is the same as subclass in both directions
                    if(add){
                        addedSubClass(subject,object,inferenceModel);
                        addedSubClass(object,subject,inferenceModel);
                    }else{
                        removedSubClass( subject,object,inferenceModel);
                        removedSubClass(object,subject,inferenceModel);
                    }
				} 
			} else {	
				if ( stmt.getObject().asResource().getURI() == null ) {
					log.warn("The object of this assertion has a null URI: " + stmtString(stmt));
					return;
				}

				if ( stmt.getSubject().getURI() == null ) {
					log.warn("The subject of this assertion has a null URI: " + stmtString(stmt));
					return;
				}
				
				OntProperty prop1 = tboxModel.getOntProperty((stmt.getSubject()).getURI());
				if (prop1 == null) {
					log.debug("didn't find subject property in the tbox: " 
                              + (stmt.getSubject()).getURI());
					return;
					}
					
				OntProperty prop2 = tboxModel.getOntProperty(((Resource)stmt.getObject()).getURI()); 
				if (prop2 == null) {
					log.debug("didn't find object property in the tbox: " 
                              + ((Resource)stmt.getObject()).getURI());
					return;
				}
				
                if( add ){
                    addedInverseProperty(prop1, prop2, inferenceModel);	
                } else {
                    removedInverseProperty(prop1,prop2,inferenceModel);
                }
			}
		} catch (Exception e) { // don't stop the edit if there's an exception
			log.error("Exception while " + (add?"adding":"removing") + " inference(s)",e);
		}        
    }

	/**
	 * Performs incremental ABox reasoning based
	 * on changes to the class hierarchy.
	 * 
	 * Handles rdfs:subclassOf, owl:equivalentClass, and owl:inverseOf
	 */	
	public void addedTBoxStatement(Statement stmt) {
        changedTBoxStatement( stmt, true);
	}

	/**
	 * Performs incremental ABox reasoning based
	 * on changes to the class hierarchy.
	 * 
	 * Handles rdfs:subclassOf, owl:equivalentClass, and owl:inverseOf 
	 */
	public void removedTBoxStatement(Statement stmt) {	
        changedTBoxStatement(stmt, false);
	}

	protected void addedABoxTypeAssertion(Statement stmt, 
	        Model inferenceModel, 
	        HashSet<String> unknownTypes) {
	    addedABoxTypeAssertion(stmt, inferenceModel, unknownTypes, true);
	}
	
	/**
	 * Performs incremental reasoning based on a new type assertion
	 * added to the ABox (assertion that an individual is of a certain
	 * type).
	 * 
	 * If it is added that B is of type A, then for each superclass of
	 * A assert that B is of that type.
	 */
	protected void addedABoxTypeAssertion(Statement stmt, 
                                          Model inferenceModel, 
                                          HashSet<String> unknownTypes,
                                          boolean checkRedundancy) {
				
	    tboxModel.enterCriticalSection(Lock.READ);
		try {
			Resource cls = null;
			if ( (stmt.getObject().asResource()).getURI() != null ) {
				
			    cls = tboxModel.getResource(stmt.getObject().asResource().getURI()); 
			    if (cls != null) {
			    	List<Resource> parents = getParents(cls,tboxModel);
			    	
					Iterator<Resource> parentIt = parents.iterator();
	
					if (parentIt.hasNext()) {
						while (parentIt.hasNext()) {
							Resource parentClass = parentIt.next();
							
							// VIVO doesn't materialize statements that assert anonymous types
							// for individuals. Also, sharing an identical anonymous node is
							// not allowed in owl-dl. picklist population code looks at qualities
							// of classes not individuals.
							if (parentClass.isAnon()) continue;
							
							Statement infStmt = 
                                ResourceFactory.createStatement(stmt.getSubject(), 
                                                                RDF.type, parentClass);
							addInference(infStmt, inferenceModel, true, checkRedundancy);
						}						
					}					
				} else {
					if ( !(stmt.getObject().asResource().getNameSpace()).equals(OWL.NS)) {
						if (!unknownTypes.contains(stmt.getObject().asResource().getURI())) {
							unknownTypes.add(stmt.getObject().asResource().getURI());
					        log.warn("Didn't find the target class (the object of an added " +
                                     "rdf:type statement) in the TBox: " +
						          	 (stmt.getObject().asResource()).getURI() + 
                                     ". No class subsumption reasoning will be done " +
                                     "based on type assertions of this type.");
						}
					}
				}
			} else {
				log.debug("The object of this rdf:type assertion has a null URI, no reasoning"
                          + " will be done based on this assertion: " + stmtString(stmt));
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

	/**
     * If it is removed that B is of type A, then for each superclass of A remove
     * the inferred statement that B is of that type UNLESS it is otherwise entailed
     * that B is of that type.
     * 
     */
	protected void removedABoxTypeAssertion(Statement stmt, Model inferenceModel) {
	    removedABoxTypeAssertion(stmt, inferenceModel, null);
	}
	
	/**
	 * If it is removed that B is of type A, then for each superclass of A remove
	 * the inferred statement that B is of that type UNLESS it is otherwise entailed
	 * that B is of that type.
	 * 
	 * remainingTypeURIs is an optional list of asserted type URIs for the subject of 
	 * stmt, and may be null.  Supplying a precompiled list can yield performance 
     * improvement when this method is called repeatedly for the same subject.  
	 * 
	 */
	protected void removedABoxTypeAssertion(Statement stmt, 
                                            Model inferenceModel, 
                                            List<String> remainingTypeURIs) {
		tboxModel.enterCriticalSection(Lock.READ);
		try {		
			Resource cls = null;
			
			if ( (stmt.getObject().asResource()).getURI() != null ) {
			    cls = tboxModel.getResource(stmt.getObject().asResource().getURI()); 
			    
				if (cls != null) {
					if (entailedType(stmt.getSubject(),cls)) {
						addInference(stmt,inferenceModel,true);
					} 
					
			    	List<Resource> parents = getParents(cls,tboxModel);
					
					Iterator<Resource> parentIt = parents.iterator();
					
					while (parentIt.hasNext()) {
					    
						Resource parentClass = parentIt.next();
						
						// VIVO doesn't materialize statements that assert anonymous types
						// for individuals. Also, sharing an identical anonymous node is
						// not allowed in owl-dl. picklist population code looks at qualities
						// of classes not individuals.
						if (parentClass.isAnon()) continue;  
						
						List<String> typeURIs = (remainingTypeURIs == null) 
                            ? getRemainingAssertedTypeURIs(stmt.getSubject()) : remainingTypeURIs;
						if (entailedType(stmt.getSubject(),parentClass, typeURIs)) {
						    continue;    // if a type is still entailed without the
						}
                        // removed statement, then don't remove it
                        // from the inferences
						
						Statement infStmt = 
                            ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
                        removeInference(infStmt,inferenceModel,true,false);							
					}
				} else {
					log.warn("Didn't find target class (the object of the removed rdf:type"
                             + "statement) in the TBox: "
							+ ((Resource)stmt.getObject()).getURI() + ". No class subsumption"
                             +" reasoning will be performed based on the removal of this assertion.");
				}
			} else {
				log.warn("The object of this rdf:type assertion has a null URI: " 
                         + stmtString(stmt));
			}		
		} catch (Exception e) {
			log.warn("exception while removing abox type assertions: " + e.getMessage());
		} finally {
			tboxModel.leaveCriticalSection();
		}
	}

	/**
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
		
	    if (!stmt.getObject().isLiteral()) {
    		List<OntProperty> inverseProperties = getInverseProperties(stmt);	
    	    Iterator<OntProperty> inverseIter = inverseProperties.iterator();
    		
    	    while (inverseIter.hasNext()) {
    	        OntProperty inverseProp = inverseIter.next();
    	        Statement infStmt = ResourceFactory.createStatement(
    	                stmt.getObject().asResource(), inverseProp, stmt.getSubject());
    	        removeInference(infStmt,inferenceModel);
    	    }	   
	    }

        if( doSameAs )
            doSameAsForRemovedABoxAssertion( stmt, inferenceModel );

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


	/**
	 * If it is added that B is a subClass of A, then for each
	 * individual that is typed as B, either in the ABox or in the
	 * inferred model, infer that it is of type A.
	 */
	protected void addedSubClass(OntClass subClass, OntClass superClass, Model inferenceModel) {
		//log.debug("subClass = " + subClass.getURI() + " superClass = " + superClass.getURI());
		OntModel unionModel = VitroModelFactory.createOntologyModel(); 
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

	/**
	 * If removed that B is a subclass of A, then for each individual
	 * that is of type B, either inferred or in the ABox, remove the
	 * assertion that it is of type A from the inferred model,
	 * UNLESS the individual is of some type C that is a subClass 
	 * of A (including A itself)
	 */
	protected void removedSubClass(OntClass subClass, OntClass superClass, Model inferenceModel) {
		OntModel unionModel = VitroModelFactory.createOntologyModel(); 
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

	/**
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

	/**
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

	/**
	 * Get a list of individuals the same as the given individual
	 */
	protected List<Resource> getSameIndividuals(Resource ind, Model inferenceModel) {	
		ArrayList<Resource> sameIndividuals = new ArrayList<Resource>();
		fullModel.enterCriticalSection(Lock.READ);
		try {
			Iterator<Statement> iter = fullModel.listStatements(ind, OWL.sameAs, (RDFNode) null);	
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				if (stmt.getObject() != null 
                    && stmt.getObject().isResource() 
                    && stmt.getObject().asResource().getURI() != null) {
                    sameIndividuals.add(stmt.getObject().asResource());
                }
			}
		} finally {
			fullModel.leaveCriticalSection();
		}		
		return sameIndividuals;
	}

	/**
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

	/**
	 * Materializes inferences based on the owl:sameAs relationship.
	 *  
	 * If it is removed	that x is sameAs y, then remove y sameAs x from 
	 * the inference graph and then recompute the inferences for x and
	 * y based on their respective assertions.
     * that x owl:sameAs y, then all asserted and inferred
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
    protected void doSameAsForAddedABoxAssertion(Statement stmt, Model inferenceModel){
        List<Resource> sameIndividuals = 
            getSameIndividuals(stmt.getSubject().asResource(), inferenceModel);

		Iterator<Resource> sameIter = sameIndividuals.iterator();
		while (sameIter.hasNext()) {
			Resource subject = sameIter.next();
			Statement sameStmt = 
                ResourceFactory.createStatement(subject,stmt.getPredicate(),stmt.getObject());
			addInference(sameStmt,inferenceModel, doSameAs);
		}	    
    }


	/**
	 * Materializes inferences based on the owl:inverseOf relationship.
	 * and owl:sameAs
	 *  
	 * If it is added that x prop1 y, and prop2 is an inverseOf prop1
	 * then add y prop2 x to the inference graph, if it is not already in
	 * the assertions graph.
	 * 
	 */
	protected void addedABoxAssertion(Statement stmt, Model inferenceModel) {
		
	    if (!stmt.getObject().isLiteral()) {
    		List<OntProperty> inverseProperties = getInverseProperties(stmt);	
            Iterator<OntProperty> inverseIter = inverseProperties.iterator();
    		        
    	    while (inverseIter.hasNext()) {
    	       Property inverseProp = inverseIter.next();
    	       Statement infStmt = ResourceFactory.createStatement(
    	               stmt.getObject().asResource(), inverseProp, stmt.getSubject());
    	       addInference(infStmt, inferenceModel, true);
    	    }	
	    }

	    inferenceModel.enterCriticalSection(Lock.WRITE);
        try {
            if (inferenceModel.contains(stmt)) {
                inferenceModel.remove(stmt);
            }
        } finally {
            inferenceModel.leaveCriticalSection();
        }
	    
	    if(doSameAs) {
            doSameAsForAddedABoxAssertion( stmt, inferenceModel);
	    }
	}	

    void doSameAsForRemovedABoxAssertion(Statement stmt, Model inferenceModel){
	    List<Resource> sameIndividuals = 
            getSameIndividuals(stmt.getSubject().asResource(), inferenceModel);
		Iterator<Resource> sameIter = sameIndividuals.iterator();	 
		while (sameIter.hasNext()) {
			 Statement stmtSame = 
                 ResourceFactory.createStatement(sameIter.next(), 
                                                 stmt.getPredicate(), 
                                                 stmt.getObject());
			 removeInference(stmtSame,inferenceModel,false,true);
		}		 	    	    
    }

	protected void generateSameAsInferences(Resource ind1, Resource ind2, Model inferenceModel) {	
		
		OntModel unionModel = VitroModelFactory.createOntologyModel(); 
		unionModel.addSubModel(aboxModel);
		unionModel.addSubModel(inferenceModel);
		
			aboxModel.enterCriticalSection(Lock.READ);
			try {
				Iterator<Statement> iter = 
                    unionModel.listStatements(ind1, (Property) null, (RDFNode) null);
				while (iter.hasNext()) {
					Statement stmt = iter.next();
					if (stmt.getObject() == null) continue;
					Statement infStmt = 
                        ResourceFactory.createStatement(ind2,stmt.getPredicate(),stmt.getObject());
					addInference(infStmt, inferenceModel,true);
				}
			} finally {
				aboxModel.leaveCriticalSection();
			}
		
		return;
	}

	/**
	 * Remove inferences for individual
	 */
	protected void removeInferencesForIndividual(Resource ind, Model inferenceModel) {	
		
		Model individualInferences = ModelFactory.createDefaultModel();
		
		inferenceModel.enterCriticalSection(Lock.READ);
		try {
			Iterator<Statement> iter = 
                inferenceModel.listStatements(ind, (Property) null, (RDFNode) null);
			
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

	/**
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

	/**
     * Returns true if it is entailed by class subsumption that
     * subject is of type cls; otherwise returns false.
     */
	protected boolean entailedType(Resource subject, Resource cls) {
	    return entailedType(subject, cls, null);
	}
	
	/**
     * Returns true if it is entailed by class subsumption that
	 * subject is of type cls; otherwise returns false.
	 * remainingTypeURIs is an optional list of asserted type URIs for the subject 
	 * resource, and may be null.  Supplying a precompiled list can yield performance 
	 * improvement when this method is called repeatedly for the same subject. 
     */
	protected boolean entailedType(Resource subject, Resource cls, List<String> remainingTypeURIs) {
				
		List<Resource> subClasses = getSubClasses(cls);
		Set<String> subClassURIs = new HashSet<String>();
	    for (Resource subClass : subClasses) {
	        if (!subClass.isAnon()) {
	            subClassURIs.add(subClass.getURI());
	        }
	    }
	    
	    List<String> typeURIs = (remainingTypeURIs == null) ? 
            getRemainingAssertedTypeURIs(subject) : remainingTypeURIs;
	    
	    for (String typeURI : typeURIs) {
            if (!typeURI.equals(cls.getURI()) && subClassURIs.contains(typeURI)) {
                return true;
            }
	    }
	    
        return false;
		
	}
	
	protected List<String> getRemainingAssertedTypeURIs(Resource resource) {
	    
	    List<String> typeURIs = new ArrayList<String>();

	    List<Resource> sameIndividuals = getSameIndividuals(resource,inferenceModel);
	    sameIndividuals.add(resource);

	    aboxModel.enterCriticalSection(Lock.READ);
	    try {           
	        Iterator<Resource> sameIter = sameIndividuals.iterator();
	        while (sameIter.hasNext()) {
	            Resource res = sameIter.next();
	            StmtIterator typeIt = aboxModel.listStatements(res, RDF.type, (RDFNode) null);
	            while (typeIt.hasNext()) {
	                Statement stmt = typeIt.nextStatement();
	                if (stmt.getObject().isURIResource()) {
	                    String typeURI = stmt.getObject().asResource().getURI();
	                    typeURIs.add(typeURI);
	                }
	            }
	        }
	    } finally { 
	        aboxModel.leaveCriticalSection();
	    }   
	    
	    return typeURIs;
	}
	
	protected List<Resource> getSubClasses(Resource cls) {
		List<Resource> subClasses = new ArrayList<Resource>();
		tboxModel.enterCriticalSection(Lock.READ);
		try {
			Iterator<Statement> iter = 
                tboxModel.listStatements((Resource) null, RDFS.subClassOf, cls);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				if (stmt.getSubject() == null 
                    || stmt.getSubject().asResource().getURI() == null) continue;
				if (!subClasses.contains(stmt.getSubject())) {
					subClasses.add(stmt.getSubject());
				}
			}

			iter = tboxModel.listStatements((Resource) null, OWL.equivalentClass, cls);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				if (stmt.getSubject() == null || stmt.getSubject().getURI() == null) continue;
				if (!subClasses.contains(stmt.getSubject())) {
					subClasses.add(stmt.getSubject());
				}
			}

			return subClasses;
		} finally {
			tboxModel.leaveCriticalSection();
		}	
	}
	
	protected List<Resource> getSuperClasses(Resource cls) {
		List<Resource> superClasses = new ArrayList<Resource>();
		tboxModel.enterCriticalSection(Lock.READ);
		try {
			Iterator<Statement> iter = tboxModel.listStatements(cls, RDFS.subClassOf, (RDFNode) null);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				
				if (stmt.getObject() != null && stmt.getObject().isResource()) {
					Resource superCls = stmt.getObject().asResource();
					
					if (superCls.isAnon() || superClasses.contains(superCls)) {
						continue;
					}
					superClasses.add(superCls);
				}				
			}

			iter = tboxModel.listStatements((Resource) null, OWL.equivalentClass, cls);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				if (stmt.getSubject() == null 
                    || stmt.getSubject().asResource().getURI() == null) continue;
				if (!superClasses.contains(stmt.getSubject())) {
					superClasses.add(stmt.getSubject());
				}
			}
			return superClasses;
		} finally {
			tboxModel.leaveCriticalSection();
		}	
	}
	
	/**
     * Returns true if the triple is entailed by inverse property
	 * reasoning or sameAs reasoning; otherwise returns false.
     */
	protected boolean entailedStatement(Statement stmt) {	
		//TODO think about checking class subsumption here (for convenience)
		
		// Inverse properties
		List<OntProperty> inverses = getInverseProperties(stmt);
		Iterator<OntProperty> iIter = inverses.iterator();
		if (iIter.hasNext()) {
			aboxModel.enterCriticalSection(Lock.READ);
			try {						
				while (iIter.hasNext()) {		
					Property invProp = iIter.next();
					Statement invStmt = 
                        ResourceFactory.createStatement(stmt.getObject().asResource(), 
                                                        invProp, stmt.getSubject());
					if (aboxModel.contains(invStmt)) {
						return true;
					}
				}
			} finally { 
				aboxModel.leaveCriticalSection();
			}	
		}
		
		// individuals sameAs each other
        if( doSameAs ){
            List<Resource> sameIndividuals = 
                getSameIndividuals(stmt.getSubject().asResource(),inferenceModel);
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
        }
		
		return false;
	}
	
	/**
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
					log.debug("The predicate of this statement is an object property, "
                              +"but the object is not a resource.");
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
				Statement infStmt = 
                    ResourceFactory.createStatement(stmt.getObject().asResource(), 
                                                    inverseProp, stmt.getSubject());
				inferences.add(infStmt);
			}				
	    } finally {
		    aboxModel.leaveCriticalSection();
	    }
		
	    return inferences;	
	}

	/**
	 * Add an inference from the inference model
	 * 
	 * Adds the inference to the inference model if it is not already in
	 * the inference model and not in the abox model.
	 */
	
	public void addInference(Statement infStmt, Model inferenceModel) {	
        addInference(infStmt,inferenceModel,true);
	}
	
	protected void addInference(Statement infStmt, Model inferenceModel, 
	        boolean handleSameAs) {
	    addInference(infStmt, inferenceModel, handleSameAs, true);
	}
	
	protected void addInference(Statement infStmt, Model inferenceModel, 
	        boolean handleSameAs, boolean checkRedundancy) {
		
		aboxModel.enterCriticalSection(Lock.READ);
		try {
			inferenceModel.enterCriticalSection(Lock.WRITE);
			try {
				if (!checkRedundancy 
				        || (!inferenceModel.contains(infStmt) && !aboxModel.contains(infStmt)))  {
					inferenceModel.add(infStmt);
			    }
		
				if (handleSameAs) {
					List<Resource> sameIndividuals = 
                        getSameIndividuals(infStmt.getSubject().asResource(), inferenceModel);
					Iterator<Resource> sameIter = sameIndividuals.iterator();
					while (sameIter.hasNext()) {
						Resource subject = sameIter.next();
						
						Statement sameStmt = 
                            ResourceFactory.createStatement(subject,infStmt.getPredicate(),
                                                            infStmt.getObject());
						if (subject.equals(infStmt.getObject()) 
                            && OWL.sameAs.equals(infStmt.getPredicate())) {
							continue;
						}
						
						if (!inferenceModel.contains(sameStmt) 
                            && !aboxModel.contains(sameStmt)) {
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

	/**
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
		   if ( (!checkEntailment 
                 || !entailedStatement(infStmt)) 
                && inferenceModel.contains(infStmt)) {
			   inferenceModel.remove(infStmt);
		   } 		   
        } finally {
	       inferenceModel.leaveCriticalSection();	
        }
		
		if (handleSameAs) {
			inferenceModel.enterCriticalSection(Lock.WRITE);		
			try {	
			    List<Resource> sameIndividuals = 
                    getSameIndividuals(infStmt.getSubject().asResource(), inferenceModel);
			   
			    Iterator<Resource> sameIter = sameIndividuals.iterator();	 
			    while (sameIter.hasNext()) {
				  Statement infStmtSame = 
                      ResourceFactory.createStatement(sameIter.next(), 
                                                      infStmt.getPredicate(), infStmt.getObject());
				  if ((!checkEntailment 
                       || !entailedStatement(infStmtSame)) 
                      && inferenceModel.contains(infStmtSame)) { 
					inferenceModel.remove(infStmtSame);
				  }					 		   
			    }
			} finally {
				inferenceModel.leaveCriticalSection();
			}
		}	
	}

    
	/**
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
			OntModel unionModel = VitroModelFactory.createOntologyModel(); 
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
					log.debug("The object of this rdf:type assertion has a null URI: " + stmtString(stmt));
					continue;
				}
				 
				if (ontClass == null) {
					if ( !(stmt.getObject().asResource().getNameSpace()).equals(OWL.NS)) {
						if (!unknownTypes.contains(stmt.getObject().asResource().getURI())) {
						   unknownTypes.add(stmt.getObject().asResource().getURI());
					       log.debug("Didn't find the target class (the object of an asserted or inferred rdf:type statement) in the TBox: " +
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
			    	
			    	ArrayList<Resource> equivalentClasses = new ArrayList<Resource>();
			    	
			    	Iterator<Statement> iter = tboxModel.listStatements((Resource) null, OWL.equivalentClass, type);
			    	while (iter.hasNext()) {
			    		Statement stmt = iter.next();
			    		Resource res = stmt.getSubject();
			    		if ((res == null) || res.isAnon() || equivalentClasses.contains(res)  ) {
			    			continue;
			    		}
			    		equivalentClasses.add(res);
			    	}
			    	
		            Iterator<Resource> eIter = equivalentClasses.iterator();
		                
		            while (eIter.hasNext()) {
		                Resource equivClass = eIter.next();
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
	
	protected List<Resource> getParents(Resource cls, OntModel tboxModel) {
		
		List<Resource> parents = new ArrayList<Resource>();
			
	    tboxModel.enterCriticalSection(Lock.READ);
		try {
			StmtIterator iter = tboxModel.listStatements(cls, RDFS.subClassOf, (RDFNode) null);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				if (stmt.getObject() == null || !stmt.getObject().isResource() || stmt.getObject().asResource().isAnon()) {
					continue;
				}
				if (!parents.contains(stmt.getObject().asResource())) {
					parents.add(stmt.getObject().asResource());
				}
			}
				
			iter = tboxModel.listStatements(cls, OWL.equivalentClass, (RDFNode) null);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				if (stmt.getObject() == null || !stmt.getObject().isResource() || stmt.getObject().asResource().isAnon()) {
					continue;
				}
				if (!parents.contains(stmt.getObject().asResource())) {
					parents.add(stmt.getObject().asResource());
				}
			}
		} catch (Exception e) {
			log.error("problem computing type inferences for: " + cls.getURI() + e.getMessage());
		} finally {
			tboxModel.leaveCriticalSection();
		}
		
		return parents;
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
						" statement in SimpleReasoner plugin:" + plugin.getClass().getName() + " -- ", e);
			}
		}
	}

	/**
	 * Returns true if the reasoner is in the process of recomputing all
	 * inferences.
	 */
	
	public boolean isRecomputing() {
		if (recomputer == null) {
			return false;
		}
		
	    return recomputer.isRecomputing();
	}
	
	public void recompute() {
		if (recomputer != null) {
		    recomputer.recompute();
		}
	}
	
	/**
	 * This is called when the application shuts down.
	 */
	public void setStopRequested() {
	    if (recomputer != null) {
	    	recomputer.setStopRequested();
	    }

	    this.stopRequested = true;
	}
	

	  
    // DeltaComputer    
	/**
	 * Asynchronous reasoning mode (DeltaComputer) is used in the case of batch removals. 
	 */
	public boolean isABoxReasoningAsynchronous() {
         if (batchMode > 0) {
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
	    			batchMode = 1;
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
					VitroBackgroundThread thread = new VitroBackgroundThread(new DeltaComputer(), 
							"SimpleReasoner.DeltaComputer");
	    		    thread.setWorkLevel(WORKING);
					thread.start();
	    		} else {
	    			eventCount--;
	    			log.info("received a bulk update end event while currently processing in aynchronous mode. Event count = " + eventCount);
	    		}
	    	}
	    }
	}
	
	private synchronized boolean switchBatchModes() {

		if (batchMode == 1) { 
    	   aBoxDeltaModeler2.getRetractions().removeAll();
    	   
    	   if (aBoxDeltaModeler1.getRetractions().size() > 0) {
    		   batchMode = 2;
			   log.info("entering batch mode " + batchMode);
    	   } else {
    		   deltaComputerProcessing = false;
    		   if (eventCount == 0) {
    			   batchMode = 0;
    		   }
    	   }
	   } else if (batchMode == 2) {
			aBoxDeltaModeler1.getRetractions().removeAll();

    	    if (aBoxDeltaModeler2.getRetractions().size() > 0) {
    	    	batchMode = 1; 
			   log.info("entering batch mode " + batchMode);
    	    } else {
    		   deltaComputerProcessing = false;
    		   if (eventCount == 0) {
    			   batchMode = 0;
    		   }
    	    }
	   } else { 
		    log.warn("unexpected condition, invoked when batchMode is neither 1 nor 2. batchMode = " + batchMode);
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
        			if (batchMode == 1) {
        				qualifier = "2";
        				retractions = aBoxDeltaModeler2.getRetractions();
        			} else if (batchMode == 2) {
        				qualifier = "1";
        				retractions = aBoxDeltaModeler1.getRetractions();        				
        			} 
        		} else {
        			break;
        		}
        	
    			retractions.enterCriticalSection(Lock.READ);	
    			int num = 0;
    			
    			try {
    	   	       	log.info("started computing inferences for batch " + qualifier + " updates");
    				
    			   	
    				ResIterator subIt = retractions.listSubjects();
    				while (subIt.hasNext()) {	
    				    Resource subj = subIt.nextResource();
    				    StmtIterator iter = retractions.listStatements(
    				            subj, null, (RDFNode) null);
    				    boolean typesModified = false;
    				    try {
    				        List<String> typeURIs = null;
        				    while (iter.hasNext() && !stopRequested) {              
                                Statement stmt = iter.next();
                                num++;                         
                                try {
                                    if (stmt.getPredicate().equals(RDF.type)) {
                                        typesModified = true;
                                        if (typeURIs == null) {
                                            typeURIs = getRemainingAssertedTypeURIs(stmt.getSubject());
                                        }
                                        removedABoxTypeAssertion(stmt, inferenceModel, typeURIs);
                                    } else if (doSameAs && stmt.getPredicate().equals(OWL.sameAs)) {
                                        removedABoxSameAsAssertion(stmt, inferenceModel);   
                                    } else {
                                        removedABoxAssertion(stmt, inferenceModel);
                                    }
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
                            if (typesModified) {
                                setMostSpecificTypes(subj, inferenceModel, new HashSet<String>());
                            }
    				    }
    				}				
    			} finally {
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
        	
        	log.info("ending DeltaComputer.run. batchMode = " + batchMode);
        }        
    } 

	/**
	 * Utility method for logging
	 */
    public static String stmtString(Statement statement) {
    	return  " [subject = " + statement.getSubject().getURI() +
    			"] [property = " + statement.getPredicate().getURI() +
                "] [object = " + (statement.getObject().isLiteral() 
                                  ? ((Literal)statement.getObject()).getLexicalForm() + " (Literal)"
                                  : ((Resource)statement.getObject()).getURI() + " (Resource)") + "]";
    }  

}
