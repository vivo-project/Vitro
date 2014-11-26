/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.pellet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.mindswap.pellet.jena.PelletInfGraph;
import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ConfiguredReasonerListener;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ConfiguredReasonerListener.Suspension;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerStatementPattern;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasonerDriver;

public class PelletListener implements TBoxReasonerDriver {

	private static final Log log = LogFactory.getLog(PelletListener.class.getName());
	private boolean isReasoning = false;
	private boolean isSynchronizing = false;
	private boolean dirty = false;
	
	private OntModel pelletModel;
	private OntModel fullModel;
	private OntModel mainModel;
	private Model inferenceModel;
	private ReasonerConfiguration reasonerConfiguration;
	private Set<ReasonerStatementPattern> inferenceDrivingPatternAllowSet;
	private Set<ReasonerStatementPattern> inferenceDrivingPatternDenySet;
	private Set<ReasonerStatementPattern> inferenceReceivingPatternAllowSet;
	
	private final ConfiguredReasonerListener listener;
	
	private Model additionModel;
	private Model removalModel;
	
	private Model deletedObjectProperties;
	private Model deletedDataProperties;
	
	private boolean isConsistent = true;
	private boolean inErrorState = false;
	private String explanation = "";
	
	public boolean isConsistent() {
		return this.isConsistent;
	}
	
	public String getExplanation() {
		return this.explanation;
	}
	
	public boolean isInErrorState() {
		return this.inErrorState;
	}
	
	public boolean isReasoning() {
		return this.isReasoning;
	}
	
	public synchronized boolean checkAndStartReasoning(){
	    if( this.isReasoning )
	        return false;
	    else{
	        this.isReasoning = true;
	        return true;
	    }
	}	
	
	public synchronized void endReasoning() {
		this.isReasoning = false;
	}
	
	public boolean isDirty() {
		return this.dirty;
	}
	
	public void setDirty(boolean dirt) {
		this.dirty = dirt;
	}
	
	private int inferenceRounds = 0;
	
	private boolean foreground = false;
	private static final boolean FOREGROUND = true;
	private static final boolean BACKGROUND = false;
	private static final boolean DONT_SKIP_INITIAL_REASONING = false;
	
	public PelletListener(OntModel fullModel, OntModel model, Model inferenceModel, ReasonerConfiguration reasonerConfiguration) {
	    this(fullModel, model, inferenceModel, reasonerConfiguration, BACKGROUND);	
	}
	
	public PelletListener(OntModel fullModel, OntModel model, Model inferenceModel, ReasonerConfiguration reasonerConfiguration, boolean foreground) {
		this(fullModel, model, inferenceModel, reasonerConfiguration, foreground, DONT_SKIP_INITIAL_REASONING);
	}	
		
	public PelletListener(OntModel fullModel, OntModel model, Model inferenceModel, ReasonerConfiguration reasonerConfiguration, boolean foreground, boolean skipReasoningUponInitialization) {
		this.pelletModel = ModelFactory.createOntologyModel(reasonerConfiguration.getOntModelSpec());
		this.fullModel = fullModel;
		this.mainModel = model;
		this.inferenceModel = inferenceModel;
		if (this.inferenceModel == null) {
			log.trace("Inference model is null");
		}
		this.reasonerConfiguration = reasonerConfiguration;
		this.inferenceDrivingPatternAllowSet = reasonerConfiguration.getInferenceDrivingPatternAllowSet();
		this.inferenceDrivingPatternDenySet = reasonerConfiguration.getInferenceDrivingPatternDenySet();
		this.inferenceReceivingPatternAllowSet = reasonerConfiguration.getInferenceReceivingPatternAllowSet();
        
		this.additionModel = ModelFactory.createDefaultModel();
		this.removalModel = ModelFactory.createDefaultModel();
		this.deletedObjectProperties = ModelFactory.createDefaultModel();
		this.deletedDataProperties = ModelFactory.createDefaultModel();
		
		listener = new ConfiguredReasonerListener(reasonerConfiguration, this);
		
		this.mainModel.enterCriticalSection(Lock.READ);
    	try {
    	    for (ReasonerStatementPattern pat : this.inferenceDrivingPatternAllowSet) {
    	        listener.addedStatements(mainModel.listStatements((Resource) null, pat.getPredicate(), (RDFNode) null));    
    	    }
        	if (!skipReasoningUponInitialization) {
        		this.foreground = foreground;
        		listener.notifyEvent(null,new EditEvent(null,false));
        	} else if (inferenceModel.size() == 0){
        		foreground = true;
        		listener.notifyEvent(null,new EditEvent(null,false));
        		this.foreground = foreground;
        	}
        } finally {
        	this.mainModel.leaveCriticalSection();
        }
        
		this.fullModel.getBaseModel().register(listener);
        this.mainModel.getBaseModel().register(listener);
	}
	
	@Override
	public void addStatement(Statement stmt) {
		additionModel.enterCriticalSection(Lock.WRITE);
		try {
			additionModel.add(stmt);
		} finally {
			additionModel.leaveCriticalSection();
		}
	}

	@Override
	public void removeStatement(Statement stmt) {
		removalModel.enterCriticalSection(Lock.WRITE);
		try {
			removalModel.add(stmt);
		} finally {
			removalModel.leaveCriticalSection();
		}
	}

	@Override
	public void deleteDataProperty(Statement stmt) {
		deletedDataProperties.enterCriticalSection(Lock.WRITE);
		try {
			deletedDataProperties.add(stmt);
		} finally {
			deletedDataProperties.leaveCriticalSection();
		}
	}

	@Override
	public void deleteObjectProperty(Statement stmt) {
		deletedObjectProperties.enterCriticalSection(Lock.WRITE);
		try {
			deletedObjectProperties.add(stmt);
		} finally {
			deletedObjectProperties.leaveCriticalSection();
		}
	}

	@Override
	public void runSynchronizer() {
		if ((additionModel.size() > 0) || (removalModel.size() > 0)) {
			if (!isSynchronizing) {
				if (foreground) {
					log.debug("Running Pellet in foreground.");
					(new PelletSynchronizer()).run();
				} else {
					log.debug("Running Pellet in background.");
					new Thread(new PelletSynchronizer(),
							"PelletListener.PelletSynchronizer").start();
				}
			}
		}
	}
	
	private class InferenceGetter implements Runnable {
		
		private PelletListener pelletListener;
		
		public InferenceGetter(PelletListener pelletListener) {
			this.pelletListener = pelletListener;
		}
		
		public void run() {
			while (pelletListener.isDirty()) {
				//pipeOpen = false;
				try {
					pelletListener.setDirty(false);
					inferenceRounds++;
					log.info("Getting new inferences");
					long startTime = System.currentTimeMillis();
					LinkedList<ReasonerStatementPattern> irpl = new LinkedList<>();
					
					if (inferenceReceivingPatternAllowSet != null) {
						irpl.addAll(inferenceReceivingPatternAllowSet);
					} else {
						irpl.add(ReasonerStatementPattern.ANY_OBJECT_PROPERTY);
					}
					
					if (reasonerConfiguration.getQueryForAllObjectProperties()) {	
							pelletModel.enterCriticalSection(Lock.READ);
							try {
								ClosableIterator closeIt = pelletModel.listObjectProperties();
								try {
									for (Iterator objPropIt = closeIt; objPropIt.hasNext();) {
										ObjectProperty objProp = (ObjectProperty) objPropIt.next();
										if ( !("http://www.w3.org/2002/07/owl#".equals(objProp.getNameSpace())) ) {
											irpl.add(ReasonerStatementPattern.objectPattern(objProp));
										}
									}
								} finally {
									closeIt.close();
								}
							} finally {
								pelletModel.leaveCriticalSection();
							}
							deletedObjectProperties.enterCriticalSection(Lock.WRITE);
							try {
								ClosableIterator sit = deletedObjectProperties.listSubjects();
								try {
									while (sit.hasNext()) {
										Resource subj = (Resource) sit.next();
										irpl.add(ReasonerStatementPattern.objectPattern(ResourceFactory.createProperty(subj.getURI())));
									}
								} finally {
									sit.close();
								}
								deletedObjectProperties.removeAll();
							} finally {
								deletedObjectProperties.leaveCriticalSection();
							}
					}
					
					if (reasonerConfiguration.getQueryForAllDatatypeProperties()) {	
						pelletModel.enterCriticalSection(Lock.READ);
						try {
							ClosableIterator closeIt = pelletModel.listDatatypeProperties();
							try {
								for (Iterator dataPropIt = closeIt; dataPropIt.hasNext();) {
									DatatypeProperty dataProp = (DatatypeProperty) dataPropIt.next();
									if ( !("http://www.w3.org/2002/07/owl#".equals(dataProp.getNameSpace())) ) {
										// TODO: THIS WILL WORK, BUT NEED TO GENERALIZE THE PATTERN CLASSES 
										irpl.add(ReasonerStatementPattern.objectPattern(dataProp));
									}
								}
							} finally {
								closeIt.close();
							}
						} finally {
							pelletModel.leaveCriticalSection();
						}
						deletedDataProperties.enterCriticalSection(Lock.WRITE);
						try {
							ClosableIterator sit = deletedDataProperties.listSubjects();
							try {
								while (sit.hasNext()) {
									Resource subj = (Resource) sit.next();
									irpl.add(ReasonerStatementPattern.objectPattern(ResourceFactory.createProperty(subj.getURI())));
								}
							} finally {
								sit.close();
							}
							deletedDataProperties.removeAll();
						} finally {
							deletedDataProperties.leaveCriticalSection();
						}
					}
						
					int addCount = 0;
					int retractCount = 0;
				
					// force new reasoner (disabled)
					if (false && !reasonerConfiguration.isIncrementalReasoningEnabled()) {
						Model baseModel = pelletModel.getBaseModel();
						pelletModel = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
						pelletModel.getDocumentManager().setProcessImports(false);
						pelletModel.add(baseModel);
					}
					
					pelletModel.enterCriticalSection(Lock.WRITE);
					try {	
						pelletModel.rebind();
						pelletModel.prepare();
					} finally {
						pelletModel.leaveCriticalSection();
					} 
					
					 for (Iterator<ReasonerStatementPattern> patIt = irpl.iterator(); patIt.hasNext(); ) {
				        	ReasonerStatementPattern pat = patIt.next();
							log.debug("Querying for "+pat);
				        	
				        	Model tempModel = ModelFactory.createDefaultModel();
				        	
				        	pelletModel.enterCriticalSection(Lock.READ); 
				        	try {
				        		for(Statement stmt : pat.matchStatementsFromModel(pelletModel)) {
						        	
						        	boolean reject = false;
					        		
					        		// this next part is only needed if we're using Jena's OWL reasoner instead of actually using Pellet
					        		try {
					        			if ( ( ((Resource)stmt.getObject()).equals(RDFS.Resource) ) ) {
					        				reject = true;
					        			} else if ( ( stmt.getSubject().equals(OWL.Nothing) ) )  {
											reject = true; 
										}  else if ( ( stmt.getObject().equals(OWL.Nothing) ) )  {
											reject = true;
										}
					        		} catch (Exception e) {}
					        		
					        		if (!reject) {
					        			tempModel.add(stmt);
					        		
					        			boolean fullModelContainsStatement = false;
					        			fullModel.enterCriticalSection(Lock.READ);
					        			try {
					        				fullModelContainsStatement = fullModel.contains(stmt);
					        			} finally {
					        				fullModel.leaveCriticalSection();
					        			}
					        			
							        	if (!fullModelContainsStatement) {
							        		// in theory we should be able to lock only the inference model, but I'm not sure yet if Jena propagates the locking upward
							        		fullModel.enterCriticalSection(Lock.WRITE);
								        	try (Suspension susp = listener.suspend()) {
							        			inferenceModel.add(stmt);
							        			addCount++;
							        		} finally {
							        			fullModel.leaveCriticalSection();
							        		}
							        	}
						        	
					        		}
						        }   
				        	} finally {
				        		pelletModel.leaveCriticalSection();
				        	}
				        	
				        	// now we see what's in the inference model that isn't in the temp model and remove it
				        	
							try {
								Queue<Statement> localRemovalQueue = new LinkedList<Statement>();
								for (Statement stmt : pat.matchStatementsFromModel(inferenceModel)) {
						        	if (!tempModel.contains(stmt)) {
						        		localRemovalQueue.add(stmt);
						        	}
								}
						        for (Iterator<Statement> i = localRemovalQueue.iterator(); i.hasNext(); ) {
						        	fullModel.enterCriticalSection(Lock.WRITE);
						        	try (Suspension susp = listener.suspend()) {
						        		retractCount++;
						        		inferenceModel.remove(i.next());
						        	} finally {
						        		fullModel.leaveCriticalSection();
						        	}
						        }
						        
						        localRemovalQueue.clear();
						    } catch (Exception e) {
								log.error("Error getting inferences", e);
						    } 
						    tempModel = null;
				        }
					 this.pelletListener.isConsistent = true;
					 this.pelletListener.inErrorState = false;
					 this.pelletListener.explanation = "";
					 if (log.isDebugEnabled()) {
						 log.info("Added "+addCount+" statements entailed by assertions");					 
						 log.info("Retracted "+retractCount+" statements no longer entailed by assertions");						 
						 log.info("Done getting new inferences: "+(System.currentTimeMillis()-startTime)/1000+" seconds");
					 }
				} catch (InconsistentOntologyException ioe) {
					this.pelletListener.isConsistent = false;
					String explanation = ((PelletInfGraph)pelletModel.getGraph()).getKB().getExplanation();
					this.pelletListener.explanation = explanation;
					log.error(ioe);
					log.error(explanation);
				} catch (Exception e) {
					this.pelletListener.inErrorState = true;
					log.error("Exception during inference", e);
				} finally {
					pelletListener.endReasoning();
				}
			}
		}
			
	}
	
	private void getInferences() {
		this.setDirty(true);
		if ( this.checkAndStartReasoning() ){
			if (foreground) {
				(new InferenceGetter(this)).run();
			} else {
				new Thread(new InferenceGetter(this), "PelletListener.InferenceGetter").start();
			}
		}
	}
	
	private class PelletSynchronizer implements Runnable {
		public void run() {
			try {
				isSynchronizing = true;
				while (removalModel.size()>0 || additionModel.size()>0) {
					Model tempModel = ModelFactory.createDefaultModel();
					removalModel.enterCriticalSection(Lock.WRITE);
					try {
						tempModel.add(removalModel);
						removalModel.removeAll();
					} finally {
						removalModel.leaveCriticalSection();
					}
					pelletModel.enterCriticalSection(Lock.WRITE); 
					try {
						pelletModel.remove(tempModel);
					} finally {
						pelletModel.leaveCriticalSection();
					}
					tempModel.removeAll();
					additionModel.enterCriticalSection(Lock.WRITE);
					try {
						tempModel.add(additionModel);
						additionModel.removeAll();
					} finally {
						additionModel.leaveCriticalSection();
					}
					pelletModel.enterCriticalSection(Lock.WRITE);
					try {
						pelletModel.add(tempModel);
					} finally {
						pelletModel.leaveCriticalSection();
					}
					tempModel = null;
					
					getInferences();
					
				}
			} finally {
				isSynchronizing = false;
			}
		}
	}
	
	public OntModel getPelletModel() {
		return this.pelletModel;
	}

}
