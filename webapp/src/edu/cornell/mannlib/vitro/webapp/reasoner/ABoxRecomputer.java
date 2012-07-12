/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class ABoxRecomputer {

	private static final Log log = LogFactory.getLog(ABoxRecomputer.class);
	
	private OntModel tboxModel;             // asserted and inferred TBox axioms
	private OntModel aboxModel;             // ABox assertions
	private Model inferenceModel;           // ABox inferences
	private Model inferenceRebuildModel;    // work area for recomputing all ABox inferences
	private Model scratchpadModel;          // work area for recomputing all ABox inferences
	private SimpleReasoner simpleReasoner;
	private Object lock1 = new Object();
	
	private volatile boolean recomputing = false;
	private boolean stopRequested = false;
	
	/**
	 * @param tboxModel - input.  This model contains both asserted and inferred TBox axioms
	 * @param aboxModel - input.  This model contains asserted ABox statements
	 * @param inferenceModel - output. This is the model in which inferred (materialized) ABox statements are maintained (added or retracted).
	 * @param inferenceRebuildModel - output. This the model is temporarily used when the whole ABox inference model is rebuilt
	 * @param inferenceScratchpadModel - output. This the model is temporarily used when the whole ABox inference model is rebuilt
 	 */
	public ABoxRecomputer(OntModel tboxModel,
			              OntModel aboxModel,
			              Model inferenceModel,
			              Model inferenceRebuildModel,
			              Model scratchpadModel,
			              SimpleReasoner simpleReasoner) {
		this.tboxModel = tboxModel;
        this.aboxModel = aboxModel; 
		this.inferenceModel = inferenceModel;
		this.inferenceRebuildModel = inferenceRebuildModel;
		this.scratchpadModel = scratchpadModel;	
		this.simpleReasoner = simpleReasoner;
		recomputing = false;
		stopRequested = false;		
	}
	
	/**
	 * Returns true if the recomputer is in the process of recomputing
	 * all inferences.
	 */
	public boolean isRecomputing() {
	     return recomputing;
	}
	
	/**
	 * Recompute all inferences.
	 */
	public void recompute() {
		
		synchronized (lock1) {
			if (recomputing) {
				return;
			} else {
				recomputing = true;
			}
		}
		
	    try {
	        recomputeABox();
	    } finally {
	    	synchronized (lock1) {
		        recomputing = false;	    		
	    	}
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
	protected void recomputeABox() {
			
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
					simpleReasoner.setMostSpecificTypes(individual, inferenceRebuildModel, unknownTypes);
					StmtIterator sit = aboxModel.listStatements(individual, null, (RDFNode) null);
					while (sit.hasNext()) {
						Statement s = sit.nextStatement();
						for (ReasonerPlugin plugin : simpleReasoner.getPluginList()) {
							plugin.addedABoxStatement(s, aboxModel, inferenceRebuildModel, tboxModel);
						}
					}
				} catch (NullPointerException npe) {
	            	log.error("a NullPointerException was received while recomputing the ABox inferences. Halting inference computation.");
	            	npe.printStackTrace();
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
										
					simpleReasoner.addedInverseProperty(prop1, prop2, inferenceRebuildModel);
				} catch (NullPointerException npe) {
	            	log.error("a NullPointerException was received while recomputing the ABox inferences. Halting inference computation.");
	            	npe.printStackTrace();
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
					simpleReasoner.addedABoxSameAsAssertion(stmt, inferenceRebuildModel); 
				} catch (NullPointerException npe) {
	            	log.error("a NullPointerException was received while recomputing the ABox inferences. Halting inference computation.");
	            	npe.printStackTrace();
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

	protected void addedABoxTypeAssertion(Resource individual, Model inferenceModel, HashSet<String> unknownTypes) {

		StmtIterator iter = null;
		
		aboxModel.enterCriticalSection(Lock.READ);
		try {		
			iter = aboxModel.listStatements(individual, RDF.type, (RDFNode) null);
			
			while (iter.hasNext()) {	
				Statement stmt = iter.next();
				simpleReasoner.addedABoxTypeAssertion(stmt, inferenceModel, unknownTypes);
			}
		} finally {
			iter.close();
			aboxModel.leaveCriticalSection();
		}
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
}
