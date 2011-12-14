/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.pellet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

public class PelletListener implements ModelChangedListener {

	private static final Log log = LogFactory.getLog(PelletListener.class.getName());
	private boolean isReasoning = false;
	private boolean isSynchronizing = false;
	private boolean dirty = false;
	
	private OntModel pelletModel;
	private OntModel fullModel;
	private OntModel mainModel;
	private Model inferenceModel;
	private ReasonerConfiguration reasonerConfiguration;
	private Set<ObjectPropertyStatementPattern> inferenceDrivingPatternAllowSet;
	private Set<ObjectPropertyStatementPattern> inferenceDrivingPatternDenySet;
	private Set<ObjectPropertyStatementPattern> inferenceReceivingPatternAllowSet;
	
	private Map<Property,List<ObjectPropertyStatementPattern>> inferenceDrivingPatternMap;
	
	private Model additionModel;
	private Model removalModel;
	
	private Model deletedObjectProperties;
	private Model deletedDataProperties;
	
	private boolean pipeOpen;
	
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
	
	public void closePipe() {
		pipeOpen = false;
	}
	
	public void openPipe() {
		pipeOpen = true;
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
        
		if (this.inferenceDrivingPatternAllowSet != null) {
			this.inferenceDrivingPatternMap = new HashMap<Property,List<ObjectPropertyStatementPattern>>();
			for (Iterator<ObjectPropertyStatementPattern> i = inferenceDrivingPatternAllowSet.iterator(); i.hasNext();) {
				ObjectPropertyStatementPattern pat = i.next();
				Property p = pat.getPredicate();
				List<ObjectPropertyStatementPattern> patList = inferenceDrivingPatternMap.get(p);
				if (patList == null) {
					patList = new LinkedList<ObjectPropertyStatementPattern>();
					patList.add(pat);
					inferenceDrivingPatternMap.put(p, patList);
				} else {
					patList.add(pat);
				}
			}
		}
		this.pipeOpen = true;
		this.additionModel = ModelFactory.createDefaultModel();
		this.removalModel = ModelFactory.createDefaultModel();
		this.deletedObjectProperties = ModelFactory.createDefaultModel();
		this.deletedDataProperties = ModelFactory.createDefaultModel();
		this.mainModel.enterCriticalSection(Lock.READ);
    	try {
        	addedStatements(mainModel);
        	if (!skipReasoningUponInitialization) {
        		this.foreground = foreground;
        		notifyEvent(null,new EditEvent(null,false));
        	} else if (inferenceModel.size() == 0){
        		foreground = true;
        		notifyEvent(null,new EditEvent(null,false));
        		this.foreground = foreground;
        	}
        } finally {
        	this.mainModel.leaveCriticalSection();
        }
        
		this.fullModel.getBaseModel().register(this);
        this.mainModel.getBaseModel().register(this);
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
					LinkedList<ObjectPropertyStatementPattern> irpl = new LinkedList<ObjectPropertyStatementPattern>();
					
					if (inferenceReceivingPatternAllowSet != null) {
						irpl.addAll(inferenceReceivingPatternAllowSet);
					} else {
						irpl.add(ObjectPropertyStatementPatternFactory.getPattern(null,null,null));
					}
					
					if (reasonerConfiguration.getQueryForAllObjectProperties()) {	
							pelletModel.enterCriticalSection(Lock.READ);
							try {
								ClosableIterator closeIt = pelletModel.listObjectProperties();
								try {
									for (Iterator objPropIt = closeIt; objPropIt.hasNext();) {
										ObjectProperty objProp = (ObjectProperty) objPropIt.next();
										if ( !("http://www.w3.org/2002/07/owl#".equals(objProp.getNameSpace())) ) {
											irpl.add(ObjectPropertyStatementPatternFactory.getPattern(null,objProp,null));
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
										irpl.add(ObjectPropertyStatementPatternFactory.getPattern(null,ResourceFactory.createProperty(subj.getURI()),null));
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
										irpl.add(ObjectPropertyStatementPatternFactory.getPattern(null,dataProp,null));
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
									irpl.add(ObjectPropertyStatementPatternFactory.getPattern(null,ResourceFactory.createProperty(subj.getURI()),null));
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
					
					 for (Iterator<ObjectPropertyStatementPattern> patIt = irpl.iterator(); patIt.hasNext(); ) {
				        	ObjectPropertyStatementPattern pat = patIt.next();

				        	if (log.isDebugEnabled()) {
				        		String subjStr = (pat.getSubject() != null) ? pat.getSubject().getURI() : "*";
				        		String predStr = (pat.getPredicate() != null) ? pat.getPredicate().getURI() : "*";
				        		String objStr = (pat.getObject() != null) ? pat.getObject().getURI() : "*";
								log.debug("Querying for "+subjStr+" : "+predStr+" : "+objStr);
							}
				        	
				        	Model tempModel = ModelFactory.createDefaultModel();
				        	
				        	pelletModel.enterCriticalSection(Lock.READ); 
				        	try {
				        		
					        	ClosableIterator ci = pelletModel.listStatements(pat.getSubject(),pat.getPredicate(),pat.getObject());
						        try {	
							        for (ClosableIterator i=ci; i.hasNext();) {
							        	Statement stmt = (Statement) i.next();
							        	
							        	boolean reject = false;
						        		
						        		// this next part is only needed if we're using Jena's OWL reasoner instead of actually using Pellet
						        		try {
						        			if ( ( ((Resource)stmt.getObject()).equals(RDFS.Resource) ) ) {
						        				reject = true;
						        			} else if ( ( ((Resource)stmt.getSubject()).equals(OWL.Nothing) ) )  {
												reject = true; 
											}  else if ( ( ((Resource)stmt.getObject()).equals(OWL.Nothing) ) )  {
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
								        		closePipe();
								        		try {
								        			inferenceModel.add(stmt);
								        			addCount++;
								        		} finally {
								        			openPipe();
								        			fullModel.leaveCriticalSection();
								        		}
								        	}
							        	
						        		}
							        }   
						        } finally {
						        	ci.close();
						        }
				        	} finally {
				        		pelletModel.leaveCriticalSection();
				        	}
				        	
				        	// now we see what's in the inference model that isn't in the temp model and remove it
				        	
							try {
								Queue<Statement> localRemovalQueue = new LinkedList<Statement>();
									inferenceModel.enterCriticalSection(Lock.READ);
									try {
										ClosableIterator ci = inferenceModel.listStatements(pat.getSubject(),pat.getPredicate(),pat.getObject());
										try {
									        for (ClosableIterator i=ci; i.hasNext();) {
									        	Statement stmt = (Statement) i.next();
									        	if (!tempModel.contains(stmt)) {
									        		localRemovalQueue.add(stmt);
									        	}
									        }    
										} finally {
											ci.close();
										}
									} finally {
										inferenceModel.leaveCriticalSection();
									}
						        for (Iterator<Statement> i = localRemovalQueue.iterator(); i.hasNext(); ) {
						        	fullModel.enterCriticalSection(Lock.WRITE);
						        	closePipe();
						        	try {
						        		retractCount++;
						        		inferenceModel.remove(i.next());
						        	} finally {
						        		openPipe();
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
	
	// TODO: These next two methods are really ugly; I need to refactor them to remove redundancy.
	
	private void tryAdd(Statement stmt) {
		boolean sendToPellet = false;
		if ( pipeOpen && reasonerConfiguration.getReasonOnAllDatatypePropertyStatements() && stmt.getObject().isLiteral() ) {
			sendToPellet = true;
		} else 
		if ( pipeOpen && hasCardinalityPredicate(stmt) ) { // see comment on this method
			sendToPellet = true;
		} else 
		if ( (stmt.getObject().isResource()) && !((stmt.getPredicate().getURI().indexOf(VitroVocabulary.vitroURI)==0)) ) {
			if (pipeOpen) {
				sendToPellet = false;
				boolean denied = false;
				ObjectPropertyStatementPattern stPat = ObjectPropertyStatementPatternFactory.getPattern(stmt.getSubject(), stmt.getPredicate(), (Resource) stmt.getObject());
				if (inferenceDrivingPatternDenySet != null) {
					for (Iterator<ObjectPropertyStatementPattern> i = inferenceDrivingPatternDenySet.iterator(); i.hasNext(); ){
						ObjectPropertyStatementPattern pat = i.next();
						if (pat.matches(stPat)) {
							denied = true;
							break;
						}
					}
				}
				if (!denied) {
	 				if (inferenceDrivingPatternAllowSet==null) {
						sendToPellet = true;
					} else {
						// TODO: O(1) implementation of this
						List<ObjectPropertyStatementPattern> patList = this.inferenceDrivingPatternMap.get(stmt.getPredicate());
						if (patList != null) {
							for (Iterator<ObjectPropertyStatementPattern> i = patList.iterator(); i.hasNext(); ){
								ObjectPropertyStatementPattern pat = i.next();
								if (pat.matches(stPat)) {
									sendToPellet = true;
									break;
								}
							}
						}
					}
				}
				
			}
		}	
		if (sendToPellet) {
			//long startTime = System.currentTimeMillis();
			String valueStr = (stmt.getObject().isResource()) ? ((Resource)stmt.getObject()).getLocalName() : ((Literal)stmt.getObject()).getLexicalForm();
			if ( log.isDebugEnabled() ) {
				log.debug( "Adding to Pellet: " + renderStatement( stmt ) );
			}
			additionModel.enterCriticalSection(Lock.WRITE);
			try {
				additionModel.add(stmt);
			} finally {
				additionModel.leaveCriticalSection();
			}
		} else {
			if ( log.isDebugEnabled() ) {
				log.debug( "Not adding to Pellet: " + renderStatement( stmt ) );
			}
		}
	}
	
	
	private void tryRemove(Statement stmt) {
		boolean removeFromPellet = false;
		if ( pipeOpen && reasonerConfiguration.getReasonOnAllDatatypePropertyStatements() && stmt.getObject().isLiteral() ) {
			removeFromPellet = true;
		} else
		if ( pipeOpen && hasCardinalityPredicate(stmt) ) { // see comment on this method
			removeFromPellet = true;
		} else
		if ( stmt.getObject().isResource() ) {
			if (pipeOpen) {
				if (reasonerConfiguration.getQueryForAllObjectProperties() && stmt.getPredicate().equals(RDF.type) && stmt.getObject().equals(OWL.ObjectProperty)) {
					deletedObjectProperties.enterCriticalSection(Lock.WRITE);
					try {
						deletedObjectProperties.add(stmt);
					} finally {
						deletedObjectProperties.leaveCriticalSection();
					}
				}
				if (reasonerConfiguration.getQueryForAllDatatypeProperties() && stmt.getPredicate().equals(RDF.type) && stmt.getObject().equals(OWL.DatatypeProperty)) {
					deletedDataProperties.enterCriticalSection(Lock.WRITE);
					try{
						deletedDataProperties.add(stmt);
					} finally {
						deletedDataProperties.leaveCriticalSection();
					}
				}
				removeFromPellet = false;
				boolean denied = false;
				ObjectPropertyStatementPattern stPat = ObjectPropertyStatementPatternFactory.getPattern(stmt.getSubject(), stmt.getPredicate(), (Resource) stmt.getObject());
				if (inferenceDrivingPatternDenySet != null) {
					for (Iterator<ObjectPropertyStatementPattern> i = inferenceDrivingPatternDenySet.iterator(); i.hasNext(); ){
						ObjectPropertyStatementPattern pat = i.next();
						if (pat.matches(stPat)) {
							denied = true;
							break;
						}
					}
				}
				if (!denied) {
					if (inferenceDrivingPatternAllowSet==null) {
						removeFromPellet = true;
					} else {
						// TODO: O(1) implementation of this
						List<ObjectPropertyStatementPattern> patList = this.inferenceDrivingPatternMap.get(stmt.getPredicate());
						if (patList != null) {
							for (Iterator<ObjectPropertyStatementPattern> i = patList.iterator(); i.hasNext(); ){
								ObjectPropertyStatementPattern pat = i.next();
								if (pat.matches(stPat)) {
									removeFromPellet = true;
									break;
								}
							}
						}
					}
				}
			}
		}
		if (removeFromPellet) {
			String valueStr = (stmt.getObject().isResource()) ? ((Resource)stmt.getObject()).getLocalName() : ((Literal)stmt.getObject()).getLexicalForm();
			log.info("Removing from Pellet: "+stmt.getSubject().getLocalName()+" "+stmt.getPredicate().getLocalName()+" "+valueStr);
			removalModel.enterCriticalSection(Lock.WRITE);
			try {
				removalModel.add(stmt);
			} finally {
				removalModel.leaveCriticalSection();
			}
		}
	}
	
	// The pattern matching stuff needs to get reworked.
	// It originally assumed that only resources would be in object
	// position, but cardinality axioms will have e.g. nonNegativeIntegers.
	// This is a temporary workaround: all cardinality statements will
	// be exposed to Pellet, regardless of configuration patterns.
	private boolean hasCardinalityPredicate(Statement stmt) {
		return (
			stmt.getPredicate().equals(OWL.cardinality) ||
			stmt.getPredicate().equals(OWL.minCardinality) ||
			stmt.getPredicate().equals(OWL.maxCardinality) 
		) ;
	}
	
	
	public void addedStatement(Statement arg0) {
		tryAdd(arg0);
 	}

	
	public void addedStatements(Statement[] arg0) {
		for (int i=0; i<arg0.length; i++) {
			tryAdd(arg0[i]);
		}
	}

	
	public void addedStatements( List arg0 ) {
		for ( Iterator i = arg0.iterator(); i.hasNext(); ) {
			tryAdd( (Statement) i.next() );
		}

	}

	
	public void addedStatements(StmtIterator arg0) {
		for (Iterator i = arg0; i.hasNext(); ) {
			tryAdd( (Statement) i.next() );
		}

	}

	
	public void addedStatements(Model arg0) {
		for (Iterator i = arg0.listStatements(); i.hasNext(); ) {
			tryAdd( (Statement) i.next() );
		}

	}

	
	public void notifyEvent(Model arg0, Object arg1) {
		if (arg1 instanceof EditEvent) {
			EditEvent ee = (EditEvent) arg1;
			if (!ee.getBegin()) {
				if ( (additionModel.size() > 0) || (removalModel.size()>0) ) {
					if (!isSynchronizing) {
						if (foreground) {
							(new PelletSynchronizer()).run();
						} else {
							new Thread(new PelletSynchronizer(), "PelletListener.PelletSynchronizer").start();
						}
					}
				}
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
 	
	public void removedStatement(Statement arg0) {
		tryRemove(arg0);
	}

	
	public void removedStatements(Statement[] arg0) {
		for (int i=0; i<arg0.length; i++) {
			tryRemove(arg0[i]);
		}
	}

	
	public void removedStatements(List arg0) {
		for (Iterator i = arg0.iterator(); i.hasNext(); ) {
			tryRemove( (Statement) i.next());
		}
	}

	
	public void removedStatements(StmtIterator arg0) {
		for (Iterator i = arg0; i.hasNext();) {
			tryRemove( (Statement) i.next());
		}
	}

	
	public void removedStatements(Model arg0) {
		for (Iterator i = arg0.listStatements(); i.hasNext();) {
			tryRemove( (Statement) i.next());
		}
	}
	
	public OntModel getPelletModel() {
		return this.pelletModel;
	}

	private String renderStatement(Statement stmt) {
		String subjStr = (stmt.getSubject().getURI() != null) ? stmt.getSubject().getURI() : stmt.getSubject().getId().toString();
		String predStr = stmt.getPredicate().getURI();
		String objStr = "";
		RDFNode obj = stmt.getObject();
		if (obj.isLiteral()) {
			objStr = "\""+(((Literal)obj).getLexicalForm());
		} else {
			objStr = (((Resource)stmt.getObject()).getURI() != null) ? ((Resource)stmt.getObject()).getURI() : ((Resource)stmt.getObject()).getId().toString();
		}
		return (subjStr+" : "+predStr+" : "+objStr);
	}

}
