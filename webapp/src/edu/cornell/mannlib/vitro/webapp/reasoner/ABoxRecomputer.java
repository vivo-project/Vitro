/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
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

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.SimpleReasonerSetup;

public class ABoxRecomputer {

	private static final Log log = LogFactory.getLog(ABoxRecomputer.class);
	
	private OntModel tboxModel;             // asserted and inferred TBox axioms
	private OntModel aboxModel;             // ABox assertions
	private Model inferenceModel;           // ABox inferences
	private Model inferenceRebuildModel;    // work area for recomputing all ABox inferences
	private Model scratchpadModel;          // work area for recomputing all ABox inferences
	private RDFService rdfService;
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
			              RDFService rdfService,
			              SimpleReasoner simpleReasoner) {
		this.tboxModel = tboxModel;
        this.aboxModel = aboxModel; 
		this.inferenceModel = inferenceModel;
		this.inferenceRebuildModel = inferenceRebuildModel;
		this.scratchpadModel = scratchpadModel;	
		this.rdfService = rdfService;
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
		    
		    log.info("Clearing inference rebuild model.");
			HashSet<String> unknownTypes = new HashSet<String>();
			inferenceRebuildModel.removeAll();
			
			log.info("Computing class subsumption ABox inferences.");
			int numStmts = 0;
			Collection<String> individuals = this.getAllIndividualURIs();
			
			log.info("Recomputing inferences for " + individuals.size() + " individuals");
			
			for (String individualURI : individuals) {			
				Resource individual = ResourceFactory.createResource(individualURI);
				
				try {
					addedABoxTypeAssertion(individual, inferenceRebuildModel, unknownTypes);
					simpleReasoner.setMostSpecificTypes(individual, inferenceRebuildModel, unknownTypes);
					List<ReasonerPlugin> pluginList = simpleReasoner.getPluginList();
					if (pluginList.size() > 0) {
    					StmtIterator sit = aboxModel.listStatements(individual, null, (RDFNode) null);
    					while (sit.hasNext()) {
    						Statement s = sit.nextStatement();
    						for (ReasonerPlugin plugin : pluginList) {
    							plugin.addedABoxStatement(s, aboxModel, inferenceRebuildModel, tboxModel);
    						}
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
				} catch (OutOfMemoryError e) {
				    log.error(individualURI + " out of memory", e);
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
		    e.printStackTrace();
			log.error("Exception while recomputing ABox inferences. Halting processing.", e);
		} finally {
			inferenceRebuildModel.removeAll();
			inferenceRebuildModel.leaveCriticalSection();
		}		
	}
	
	/*
	 * Get the URIs for all individuals in the system
	 */
	protected Collection<String> getAllIndividualURIs() {
	    
		String queryString = "select ?s where {?s a ?type}";
	    return getIndividualURIs(queryString);
	}

	protected Collection<String> getIndividualURIs(String queryString) {
	    
		Set<String> individuals = new HashSet<String>();

		int batchSize = 50000;
		int offset = 0;
		boolean done = false;
		
		while (!done) {
		    String queryStr = queryString + " LIMIT " + batchSize + " OFFSET " + offset;
		    if(log.isDebugEnabled()) {
		        log.debug(queryStr);
		    }
		    
		    ResultSet results = null;
		    
		    try {
		        InputStream in = rdfService.sparqlSelectQuery(queryStr, RDFService.ResultFormat.JSON);
		        results = ResultSetFactory.fromJSON(in);
		    } catch (RDFServiceException e) {
		        throw new RuntimeException(e);
		    }
            
    		if (!results.hasNext()) {
    		    done = true;
    		}
    		
    		while (results.hasNext()) {
    			QuerySolution solution = results.next();
    			Resource resource = solution.getResource("s");
    			
    			if ((resource != null) && !resource.isAnon()) {
    				individuals.add(resource.getURI());
    			}					
    		}
    		
    		if(log.isDebugEnabled()) {
    		    log.info(individuals.size() + " in set");
    		}
    		offset += batchSize;
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
		    if (iter != null) {
			    iter.close();
		    }
			aboxModel.leaveCriticalSection();
		}
	}
	/*
	 * reconcile a set of inferences into the application inference model
	 */
	protected boolean updateInferenceModel(Model inferenceRebuildModel) {
	    
    log.info("Updating ABox inference model");
	Iterator<Statement> iter = null;
 
	// Remove everything from the current inference model that is not
	// in the recomputed inference model	
    int num = 0;
	scratchpadModel.enterCriticalSection(Lock.WRITE);
	scratchpadModel.removeAll();
	log.info("Updating ABox inference model (checking for outdated inferences)");
	try {
		inferenceModel.enterCriticalSection(Lock.READ);
	
		try {
			iter = listModelStatements(inferenceModel, JenaDataSourceSetupBase.JENA_INF_MODEL);
			while (iter.hasNext()) {				
				Statement stmt = iter.next();
				if (!inferenceRebuildModel.contains(stmt)) {
				   scratchpadModel.add(stmt);  
				}
				
				num++;
                if ((num % 10000) == 0) {
                    log.info("Still updating ABox inference model (checking for outdated inferences)...");
                }
                
                if (stopRequested) {
                	return true;
                }
			}
		} finally {
//		    if (iter != null) {
//			    iter.close();
//		    }
            inferenceModel.leaveCriticalSection();
		}
		
		try {
			iter = listModelStatements(scratchpadModel, SimpleReasonerSetup.JENA_INF_MODEL_SCRATCHPAD);
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
//		    if (iter != null) {
//		        iter.close();    
//		    }
		}
					
		// Add everything from the recomputed inference model that is not already
		// in the current inference model to the current inference model.	
		try {
			scratchpadModel.removeAll();
			log.info("Updating ABox inference model (adding any new inferences)");
			iter = listModelStatements(inferenceRebuildModel, SimpleReasonerSetup.JENA_INF_MODEL_REBUILD); 
			
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
                    log.info("Still updating ABox inference model (adding any new inferences)...");
                }
                
                if (stopRequested) {
                	return true;
                }
			}
		} finally {
//		    if (iter != null) {
//			    iter.close();	
//		    }
		}
					
		iter = listModelStatements(scratchpadModel, SimpleReasonerSetup.JENA_INF_MODEL_SCRATCHPAD);
		try {
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
//		    if (iter != null) {
//		        iter.close();
//		    }
		}
	} finally {
		scratchpadModel.removeAll();
		scratchpadModel.leaveCriticalSection();			
	}
	
	log.info("ABox inference model updated");
	return false;
	}
	
	private Iterator<Statement> listModelStatements(Model model, String graphURI) {
	    // the RDFServices supplied by the unit tests won't have the right
	    // named graphs.  So if the graphURI-based chunked iterator is empty, 
	    // we'll try listStatements() on the model instead.
	    Iterator<Statement> it = new ChunkedStatementIterator(graphURI);
	    if (it.hasNext()) {
	        return it;
	    } else {
	        return model.listStatements();
	    }
	}
	
	// avoids OutOfMemory errors by retrieving triples in batches
	private class ChunkedStatementIterator implements Iterator<Statement> {
	    
	    final int CHUNK_SIZE = 50000;
	    private int offset = 0;
	    
	    private String queryString;
	    
	    private Model temp = ModelFactory.createDefaultModel();
	    private StmtIterator tempIt;
	    
	    public ChunkedStatementIterator(String graphURI) {
	        this.queryString = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + 
	                graphURI + "> { ?s ?p ?o } }";
	    }
	    
	    public Statement next() {
	        if (tempIt.hasNext()) {
	            return tempIt.nextStatement();
	        } else {
	            return null;
	        }
	    }
	    
	    public void remove() {
	        throw new UnsupportedOperationException(this.getClass().getName() + 
	                " does not support .remove()");
	    }
	    
	    public boolean hasNext() {
	        if (tempIt != null && tempIt.hasNext()) {
	            return true;
	        } else {
	            getNextChunk();
	            if (temp.size() > 0) {
	                tempIt = temp.listStatements();
	                return true;
	            } else {
	                return false;
	            }
	        }
	    }
	    
	    private void getNextChunk() {
	        
            String chunkQueryString = queryString + " LIMIT " + CHUNK_SIZE + " OFFSET " + offset;
            offset += CHUNK_SIZE;
            
            try {
                InputStream in = rdfService.sparqlConstructQuery(
                        chunkQueryString, RDFService.ModelSerializationFormat.NTRIPLE);
                temp.removeAll();
                temp.add(RDFServiceUtils.parseModel(
                        in, RDFService.ModelSerializationFormat.NTRIPLE));
            } catch (RDFServiceException e) {
                throw new RuntimeException(e);
            }
	    }
        
	}
	
	
	/**
	 * This is called when the application shuts down.
	 */
	public void setStopRequested() {
	    this.stopRequested = true;
	}
}
