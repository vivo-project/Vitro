/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.jena.CumulativeDeltaModeler;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;

/**
 * Allows for real-time incremental materialization or retraction of RDFS-
 * style class and property subsumption based ABox inferences as statements
 * are added to or removed from the (ABox or TBox) knowledge base. 
 */

public class SimpleReasoner extends StatementListener {

	private static final Log log = LogFactory.getLog(SimpleReasoner.class);
	//private static final MyTempLogger log = new MyTempLogger();
	
	private OntModel tboxModel;             // asserted and inferred TBox axioms
	private OntModel aboxModel;             // ABox assertions
	private Model inferenceModel;           // ABox inferences
	private Model inferenceRebuildModel;    // work area for re-computing all ABox inferences
	private Model scratchpadModel;          // work area for re-computing all ABox inferences
	
	private static final String mostSpecificTypePropertyURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#mostSpecificType";
	
	private AnnotationProperty mostSpecificType = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createAnnotationProperty(mostSpecificTypePropertyURI);
	
	private CumulativeDeltaModeler aBoxDeltaModeler1 = null;
	private CumulativeDeltaModeler aBoxDeltaModeler2 = null;
	private boolean batchMode1 = false, batchMode2 = false;
	private boolean stopRequested = false;
	
	private List<ReasonerPlugin> pluginList = new ArrayList<ReasonerPlugin>();

	/**
	 * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param aboxModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
	 * @param inferenceRebuildModel - output. This the model temporarily used when the whole ABox inference model is rebuilt
	 * @param inferenceScratchpadModel - output. This the model temporarily used when the whole ABox inference model is rebuilt
 	 */
	public SimpleReasoner(OntModel tboxModel, OntModel aboxModel, Model inferenceModel,
			              Model inferenceRebuildModel, Model scratchpadModel) {
		this.tboxModel = tboxModel;
		this.aboxModel = aboxModel; 
		this.inferenceModel = inferenceModel;
		this.inferenceRebuildModel = inferenceRebuildModel;
		this.scratchpadModel = scratchpadModel;	
		this.batchMode1 = false;
		this.batchMode2 = false;
		aBoxDeltaModeler1 = new CumulativeDeltaModeler();
		aBoxDeltaModeler2 = new CumulativeDeltaModeler();
		stopRequested = false;
				
	    aboxModel.getBaseModel().register(this);    
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
	 * Performs selected incremental ABox reasoning based
	 * on the addition of a new statement (aka assertion) 
	 * to the ABox.
	 */
	@Override
	public void addedStatement(Statement stmt) {

		try {
			if (stmt.getPredicate().equals(RDF.type)) {
			    addedABoxTypeAssertion(stmt, inferenceModel, new HashSet<String>());
			    setMostSpecificTypes(stmt.getSubject(), inferenceModel, new HashSet<String>());
			} 
			
			doPlugins(ModelUpdate.Operation.ADD,stmt);

		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while computing inferences: " + e.getMessage());
		}
	}
	
	/*
	 * Performs selected incremental ABox reasoning based
	 * on the retraction of a statement (aka assertion)
	 * from the ABox. 
	 */
	@Override
	public void removedStatement(Statement stmt) {
	
		try {
			
            if (!isInterestedInRemovedStatement(stmt)) return;
			
			if (batchMode1) {
				 aBoxDeltaModeler1.removedStatement(stmt);
			} else if (batchMode2) {
				 aBoxDeltaModeler2.removedStatement(stmt);
			} else {
				if (stmt.getPredicate().equals(RDF.type)) {
					removedABoxTypeAssertion(stmt, inferenceModel);
					setMostSpecificTypes(stmt.getSubject(), inferenceModel, new HashSet<String>());
				}
								
				doPlugins(ModelUpdate.Operation.RETRACT,stmt);
			}
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while retracting inferences: ", e);
		}
	}
	
	/*
	 * Performs incremental selected ABox reasoning based
	 * on changes to the class hierarchy.
	 * 
	 * Handles rdfs:subclassOf, owl:equivalentClass, 
	 */	
	public void addedTBoxStatement(Statement stmt) {

		try {
			log.debug("added TBox assertion = " + stmt.toString());
			
			if ( stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(OWL.equivalentClass) ) {
				// ignore anonymous classes
				if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
				    return;
				}
			
				if ( stmt.getObject().isResource() && (stmt.getObject().asResource()).getURI() == null ) {
					log.warn("The object of this assertion has a null URI: " + stmtString(stmt));
					return;
				}

				if ( stmt.getSubject().getURI() == null ) {
					log.warn("The subject of this assertion has a null URI: " + stmtString(stmt));
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
			} 
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while adding inference(s): " + e.getMessage());
		}
	}

	/*
	 * Performs incremental selected ABox reasoning based
	 * on changes to the class hierarchy.
	 * 
	 * Handles rdfs:subclassOf, owl:equivalentClass, 
	 */
	public void removedTBoxStatement(Statement stmt) {
	
		try {
			log.debug("removed TBox assertion = " + stmt.toString());
			
			if ( stmt.getPredicate().equals(RDFS.subClassOf) || stmt.getPredicate().equals(OWL.equivalentClass) ) {
				// ignore anonymous classes
				if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
				    return;
				}
				
				if ( stmt.getObject().isResource() && (stmt.getObject().asResource()).getURI() == null ) {
					log.warn("The object of this assertion has a null URI: " + stmtString(stmt));
					return;
				}

				if ( stmt.getSubject().getURI() == null ) {
					log.warn("The subject of this assertion has a null URI: " + stmtString(stmt));
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
			} 
		} catch (Exception e) {
			// don't stop the edit if there's an exception
			log.error("Exception while removing inference(s): " + e.getMessage());
		}
	}

	/*
	 * 
	 */
	public void addedABoxTypeAssertion(Resource individual, Model inferenceModel, HashSet<String> unknownTypes) {

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
	 * 
	 */
	public void addedABoxTypeAssertion(Statement stmt, Model inferenceModel, HashSet<String> unknownTypes) {
		
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {

			OntClass cls = null;
			
			if ( (stmt.getObject().asResource()).getURI() != null ) {
				
			    cls = tboxModel.getOntClass(stmt.getObject().asResource().getURI()); 
			    if (cls != null) {
					
					List<OntClass> parents = (cls.listSuperClasses(false)).toList();		
					parents.addAll((cls.listEquivalentClasses()).toList());	
					Iterator<OntClass> parentIt = parents.iterator();
					
					while (parentIt.hasNext()) {
						OntClass parentClass = parentIt.next();
						
						// VIVO doesn't materialize statements that assert anonymous types
						// for individuals. Also, sharing an identical anonymous node is
						// not allowed in owl-dl. picklist population code looks at qualities
						// of classes not individuals.
						if (parentClass.isAnon()) continue;
						
						Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
						aboxModel.enterCriticalSection(Lock.READ);
						try {
							inferenceModel.enterCriticalSection(Lock.WRITE);
							try {
								if (!inferenceModel.contains(infStmt) && !aboxModel.contains(infStmt))  {
								    //log.debug("Adding this inferred statement:  " + infStmt.toString() );
									inferenceModel.add(infStmt);
							    }
							} finally {
								inferenceModel.leaveCriticalSection();
							}
						} finally {
							aboxModel.leaveCriticalSection();
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
	}

	/*
	 * If it is removed that B is of type A, then for each superclass of A remove
	 * the inferred statement that B is of that type UNLESS it is otherwise entailed
	 * that B is of that type.
	 * 
	 */
	public void removedABoxTypeAssertion(Statement stmt, Model inferenceModel) {
				
		tboxModel.enterCriticalSection(Lock.READ);
		
		// convert this method to use generic resources - not get ontclass, not cls.listSuperClasses...
		// use model contains if want to log warning about type owl class
		
		try {
			
			OntClass cls = null;
			
			if ( (stmt.getObject().asResource()).getURI() != null ) {
			    cls = tboxModel.getOntClass(stmt.getObject().asResource().getURI()); 
			    
				if (cls != null) {
					if (entailedType(stmt.getSubject(),cls)) {
						inferenceModel.enterCriticalSection(Lock.WRITE);
						try {
							//don't have to check aboxmodel here because this is the
							//statement being removed.
							if (!inferenceModel.contains(stmt)) {
								inferenceModel.add(stmt);
							}
						} finally {
							inferenceModel.leaveCriticalSection();
						}	
						
						return;
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
						
						if (entailedType(stmt.getSubject(),parentClass)) continue;    // if a type is still entailed without the
						                                                              // removed statement, then don't remove it
						                                                              // from the inferences
						
						Statement infStmt = ResourceFactory.createStatement(stmt.getSubject(), RDF.type, parentClass);
							
						inferenceModel.enterCriticalSection(Lock.WRITE);
						try {
							if (inferenceModel.contains(infStmt)) {
								//log.debug("Removing this inferred statement:  " + infStmt.toString() + " - " + infStmt.getSubject().toString() + " - " + infStmt.getPredicate().toString() + " - " + infStmt.getObject().toString());
								inferenceModel.remove(infStmt);
							}
						} finally {
							inferenceModel.leaveCriticalSection();
						}	
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
	
	// Returns true if it is entailed by class subsumption that
	// subject is of type cls; otherwise returns false.
	public boolean entailedType(Resource subject, OntClass cls) {
		aboxModel.enterCriticalSection(Lock.READ);
		tboxModel.enterCriticalSection(Lock.READ);
		
		try {
			List<OntClass> subclasses = null;
			subclasses = (cls.listSubClasses(false)).toList();		
			subclasses.addAll((cls.listEquivalentClasses()).toList());
			
			Iterator<OntClass> iter = subclasses.iterator();
						
			while (iter.hasNext()) {		
				OntClass childClass = iter.next();
				if (childClass.equals(cls)) continue;
				Statement stmt = ResourceFactory.createStatement(subject, RDF.type, childClass);
				if (aboxModel.contains(stmt)) return true;
			}
			
			return false;
		} catch (Exception e) {
			log.debug("exception in method entailedType: " + e.getMessage());
			return false;
		} finally {
			aboxModel.leaveCriticalSection();
			tboxModel.leaveCriticalSection();
		}	
	}
	
	/*
	 * If it is added that B is a subClass of A, then for each
	 * individual that is typed as B, either in the ABox or in the
	 * inferred model, assert that it is of type A.
	 */
	public void addedSubClass(OntClass subClass, OntClass superClass, Model inferenceModel) {
		log.debug("subClass = " + subClass.getURI() + " superClass = " + superClass.getURI());
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
			
			inferenceModel.enterCriticalSection(Lock.WRITE);
			aboxModel.enterCriticalSection(Lock.READ);
            try {		
				if (!inferenceModel.contains(infStmt) ) {
					if (!aboxModel.contains(infStmt)) {
						inferenceModel.add(infStmt);
					}
					setMostSpecificTypes(infStmt.getSubject(), inferenceModel, new HashSet<String>());
				} 
            } finally {
                inferenceModel.leaveCriticalSection();
                aboxModel.leaveCriticalSection();
            } 
        }
	}
	
	/*
	 * If removed that B is a subclass of A, then for each individual
	 * that is of type B, either inferred or in the ABox, remove the
	 * assertion that it is of type A from the inferred model,
	 * UNLESS the individual is of some type C that is a subClass 
	 * of A (including A itself)
	 */
	public void removedSubClass(OntClass subClass, OntClass superClass, Model inferenceModel) {
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
			    setMostSpecificTypes(ind, inferenceModel, new HashSet<String>());
            } finally {
                inferenceModel.leaveCriticalSection();
            }
		}
	}

	/*
     * Find the most specific types (classes) of an individual and
     * indicate them for the individual with the core:mostSpecificType
     * annotation.
	 */
	public void setMostSpecificTypes(Resource individual, Model inferenceModel, HashSet<String> unknownTypes) {
			
		inferenceModel.enterCriticalSection(Lock.WRITE);
		aboxModel.enterCriticalSection(Lock.READ);
		tboxModel.enterCriticalSection(Lock.READ);
		
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
	
			HashSet<String> typeURIs = new HashSet<String>();
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
							
			setMostSpecificTypes(individual, typeURIs, inferenceModel);
			
		} finally {
			aboxModel.leaveCriticalSection();
			tboxModel.leaveCriticalSection();
			inferenceModel.leaveCriticalSection();
		}
	
	    return;	
	}
	
	public void setMostSpecificTypes(Resource individual, HashSet<String> typeURIs, Model inferenceModel) {
		
		inferenceModel.enterCriticalSection(Lock.WRITE);
		
		try {
		    Model retractions = ModelFactory.createDefaultModel();
			// remove obsolete most-specific-type assertions
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
			
			inferenceModel.remove(retractions);
			
			// add new most-specific-type assertions 
			Iterator<String> typeIter = typeURIs.iterator();
			
			while (typeIter.hasNext()) {
				String typeURI = typeIter.next();
				Resource mstResource = ResourceFactory.createResource(typeURI);
				
				if (!inferenceModel.contains(individual, mostSpecificType, mstResource)) {
					inferenceModel.add(individual, mostSpecificType, mstResource);
				}
			}			
		} finally {
			inferenceModel.leaveCriticalSection();
		}
	
	    return;	
	}
	
	private boolean recomputing = false;
	
	/**
	 * Returns true if the reasoner is in the process of recomputing all
	 * inferences.
	 */
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
	 * inference graph is built up in a separate model and
	 * then reconciled with the inference graph used by the
	 * application. The model reconciliation must be done
	 * without reading the whole inference models into 
	 * memory in order to support very large ABox 
	 * inference models.	  
	 */
	public synchronized void recomputeABox() {
		
		HashSet<String> unknownTypes = new HashSet<String>();
		
		// recompute the inferences 
		inferenceRebuildModel.enterCriticalSection(Lock.WRITE);			
		try {
			log.info("Computing class-based ABox inferences.");
			inferenceRebuildModel.removeAll();
			
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
                    log.info("Still computing class-based ABox inferences...");
                }
                
                if (stopRequested) {
                	log.info("a stopRequested signal was received during recomputeABox. Halting Processing.");
                	return;
                }
			}
		} catch (Exception e) {
			 log.error("Exception while recomputing ABox inference model", e);
			 inferenceRebuildModel.removeAll(); // don't do this in the finally, it's needed in the case
                                                // where there isn't an exception
			 return;
		} finally {
			 inferenceRebuildModel.leaveCriticalSection();
		}			
		
		log.info("Finished computing class-based ABox inferences");
		
		// reflect the recomputed inferences into the application inference
		// model.
	    log.info("Updating ABox inference model");
	    StmtIterator iter = null;
 
		// Remove everything from the current inference model that is not
		// in the recomputed inference model	
        int num = 0;
		inferenceRebuildModel.enterCriticalSection(Lock.WRITE);
		scratchpadModel.enterCriticalSection(Lock.WRITE);
		try {
			inferenceModel.enterCriticalSection(Lock.READ);
			try {
				scratchpadModel.removeAll();
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
	                	log.info("a stopRequested signal was received during recomputeABox. Halting Processing.");
	                	return;
	                }
				}
			} catch (Exception e) {
				log.error("Exception while reconciling the current and recomputed ABox inference models", e);
				return;
			} finally {
				iter.close();
	            inferenceModel.leaveCriticalSection();
			}
			
			iter = scratchpadModel.listStatements();
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				try {
					inferenceModel.remove(stmt);
				} catch (Exception e) {
					log.error("Exception while reconciling the current and recomputed ABox inference models", e);
				} finally {
					inferenceModel.leaveCriticalSection();
				}
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
	                	log.info("a stopRequested signal was received during recomputeABox. Halting Processing.");
	                	return;
	                }
				}
			} catch (Exception e) {		
				log.error("Exception while reconciling the current and recomputed ABox inference models", e);
				return;
			} finally {
				iter.close();	
			}
						
			iter = scratchpadModel.listStatements();
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				
				inferenceModel.enterCriticalSection(Lock.WRITE);
				try {
					inferenceModel.add(stmt);
				} catch (Exception e) {
					log.error("Exception while reconciling the current and recomputed ABox inference models", e);
					return;
				} finally {
					inferenceModel.leaveCriticalSection();
				}
			}
		} finally {
			iter.close();
			inferenceRebuildModel.removeAll();
			scratchpadModel.removeAll();
			inferenceRebuildModel.leaveCriticalSection();
			scratchpadModel.leaveCriticalSection();			
		}
		
		log.info("ABox inference model updated");
	}

	
	public synchronized void computeMostSpecificType() {
	    recomputing = true;
	    try {
	    	doComputeMostSpecificType();
	    } finally {
	        recomputing = false;
	    }
	}
	
	/*
	 * Special for version 1.4 
	 */
	public synchronized void doComputeMostSpecificType() {

		log.info("Computing mostSpecificType annotations.");
		HashSet<String> unknownTypes = new HashSet<String>();
			
		try {
			String queryString = "select distinct ?subject where {?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2004/02/skos/core#Concept>}";
			ArrayList<String> individuals = this.getIndividualURIs(queryString);

			int numStmts = 0;
			for (String individualURI : individuals ) {
				
				Resource individual = ResourceFactory.createResource(individualURI);
				
				try {
				    setMostSpecificTypes(individual, inferenceModel, unknownTypes);
				} catch (NullPointerException npe) {
					log.error("a NullPointerException was received while computing mostSpecificType annotations. Halting inference computation.");	
					return;
				} catch (JenaException je) {
					 if (je.getMessage().equals("Statement models must no be null")) {
						 log.error("Exception while computing mostSpecificType annotations.: " + je.getMessage() + ". Halting inference computation.");
		                 return; 
					 } 
					 log.error("Exception while computing mostSpecificType annotations.: " + je.getMessage());	
				} catch (Exception e) {
					log.error("Exception while computing mostSpecificType annotations", e);	
				}
				
				numStmts++;
                if ((numStmts % 10000) == 0) {
                    log.info("Still computing mostSpecificType annotations...");
                }
                
                if (stopRequested) {
                	log.info("a stopRequested signal was received during computeMostSpecificType. Halting Processing.");
                	return;
                }
			}
		} catch (Exception e) {
			 log.error("Exception while computing mostSpecificType annotations", e);
			 return;
		} 
		
		log.info("Finished computing mostSpecificType annotations");
	}

	public boolean isABoxReasoningAsynchronous() {
         if (batchMode1 || batchMode2) {
        	 return true;
         } else {
        	 return false;
         }
	}
	
	protected  void startBatchMode() {
		if (batchMode1 || batchMode2) {
			return;  
		} else {
			batchMode1 = true;
			batchMode2 = false;
			aBoxDeltaModeler1.getRetractions().removeAll();
			log.info("started processing retractions in batch mode");
		}
	}

	protected void endBatchMode() {
		
		if (!batchMode1 && !batchMode2) {
			log.warn("SimpleReasoner received an end batch mode request when not currently in batch mode. No action was taken");
			return;
		}
		
		new Thread(new DeltaComputer(),"DeltaComputer").start();
	}
	
	@Override
	public synchronized void notifyEvent(Model model, Object event) {
		
	    if (event instanceof BulkUpdateEvent) {	
	    	if (((BulkUpdateEvent) event).getBegin()) {
	    		
	    		log.info("received BulkUpdateEvent(begin)");
	            startBatchMode();
	    	} else {
	    		log.info("received BulkUpdateEvent(end)");
	    		endBatchMode();
	    	}
	    }
	}
	
    private class DeltaComputer extends Thread {      
        public DeltaComputer() {
        }
        
        @Override
        public void run() {  
      
        	log.info("starting DeltaComputer.run");
        	Model retractions = aBoxDeltaModeler1.getRetractions();
        	boolean finished = (retractions.size() == 0);
        	boolean abort = false;
        	String qualifier = "(1)";
        	
        	while (!finished && !stopRequested) {
    			retractions.enterCriticalSection(Lock.READ);	
    			StmtIterator iter = null;
    			
    			try {
    	   	       	log.info("run: started computing inferences for batch " + qualifier + " update");
    				iter = retractions.listStatements();
    	
    				int num = 0;
    				while (iter.hasNext() && !stopRequested) {				
    					Statement stmt = iter.next();
    					
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
    					
						num++;
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
                	return;
                }
                
                if (abort) {
                	log.error("a NullPointerException was received while computing inferences in batch " + qualifier + " mode. Halting inference computation.");
                	return;
                }
                
   				log.info("finished computing inferences for batch " + qualifier + " update");
   				
    			if (batchMode1 && (aBoxDeltaModeler2.getRetractions().size() > 0)) {
    				retractions = aBoxDeltaModeler2.getRetractions();
    				batchMode2 = true;
    				batchMode1 = false;
    				qualifier = "(2)";
    				log.info("switching from batch mode 1 to batch mode 2");
    			} else if (batchMode2 && (aBoxDeltaModeler1.getRetractions().size() > 0)) {
    				retractions = aBoxDeltaModeler1.getRetractions();
    				batchMode1 = true;
    				batchMode2 = false;
    				qualifier = "(1)";
    				log.info("switching from batch mode 2 to batch mode 1");
    			} else {
    				finished = true;
    		       	batchMode1 = false;
    	        	batchMode2 = false;   
    				log.info("finished processing retractions in batch mode");
    			}	
        	}
        	
        	if (aBoxDeltaModeler1.getRetractions().size() > 0) {
        	   log.warn("Unexpected condition: the aBoxDeltaModeler1 retractions model was not empty at the end of the DeltaComputer.run method");
               aBoxDeltaModeler1.getRetractions().removeAll();
        	}

        	if (aBoxDeltaModeler2.getRetractions().size() > 0) {
         	   log.warn("Unexpected condition: the aBoxDeltaModeler2 retractions model was not empty at the end of the DeltaComputer.run method");
                aBoxDeltaModeler2.getRetractions().removeAll();
         	}
 
        	if (batchMode1 || batchMode2) {
        		log.warn("Unexpected condition at the end of DeltaComputer.run method: batchMode1=" + batchMode1 + ", batchMode2 =" + batchMode2 + ". (both should be false)" );
            	batchMode1 = false;
            	batchMode2 = false;    		        		
        	}
        }        
    }
   
	public ArrayList<String> getAllIndividualURIs() {
	    
		String queryString = "select distinct ?subject where {?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type}";
        return getIndividualURIs(queryString);
	}

	public ArrayList<String> getIndividualURIs(String queryString) {
	    
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
    
	/**
	 * 
	 */
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
			} catch (Throwable t) {
				log.error("Exception while processing " + (op == ModelUpdate.Operation.ADD ? "an added" : "a removed") + 
						" statement in SimpleReasoner plugin:" + plugin.getClass().getName() + " -- " + t.getMessage());
			}
		}
	}
	
	public boolean isInterestedInRemovedStatement(Statement stmt) {
		
		if (stmt.getPredicate().equals(RDF.type)) return true;

		for (ReasonerPlugin plugin : getPluginList()) {
			if (plugin.isInterestedInRemovedStatement(stmt)) return true;
		}
		
        return false;
	}
	
	/**
	 * This is called when the system shuts down.
	 */
	public void setStopRequested() {
	    this.stopRequested = true;
	}
    
    public static String stmtString(Statement statement) {
    	return  " [subject = " + statement.getSubject().getURI() +
    			"] [property = " + statement.getPredicate().getURI() +
                "] [object = " + (statement.getObject().isLiteral() ? ((Literal)statement.getObject()).getLexicalForm() + " (Literal)"
                		                                          : ((Resource)statement.getObject()).getURI() + " (Resource)") + "]";	
    }    
}
