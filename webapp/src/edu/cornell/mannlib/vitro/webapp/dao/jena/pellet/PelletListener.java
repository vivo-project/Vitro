/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena.pellet;

import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mindswap.pellet.exceptions.InconsistentOntologyException;
import org.mindswap.pellet.jena.PelletInfGraph;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ConfiguredReasonerListener;
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
	
	private Status status = Status.SUCCESS;
	
	public boolean isConsistent() {
		return this.status.isConsistent();
	}
	
	public String getExplanation() {
		return this.status.getExplanation();
	}
	
	public boolean isInErrorState() {
		return this.status.isInErrorState();
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
				try {
					pelletListener.setDirty(false);
					log.info("Getting new inferences");
					long startTime = System.currentTimeMillis();
					
					PatternListBuilder patternListBuilder = new PatternListBuilder(
							reasonerConfiguration, pelletModel,
							deletedObjectProperties, deletedDataProperties);
					LinkedList<ReasonerStatementPattern> irpl = patternListBuilder
							.build();
					
					pelletModel.enterCriticalSection(Lock.WRITE);
					try {	
						pelletModel.rebind();
						pelletModel.prepare();
					} finally {
						pelletModel.leaveCriticalSection();
					} 
					
					InferenceModelUpdater inferenceModelUpdater = new InferenceModelUpdater(
							pelletModel, inferenceModel, fullModel, listener);
					inferenceModelUpdater.update(irpl);					 
					 
					this.pelletListener.status = Status.SUCCESS;
					 if (log.isInfoEnabled()) {
						 log.info("Added "+inferenceModelUpdater.getAddCount()+" statements entailed by assertions");					 
						 log.info("Retracted "+inferenceModelUpdater.getRetractCount()+" statements no longer entailed by assertions");						 
						 log.info("Done getting new inferences: "+(System.currentTimeMillis()-startTime)/1000+" seconds");
					 }
				} catch (InconsistentOntologyException ioe) {
					String explanation = ((PelletInfGraph)pelletModel.getGraph()).getKB().getExplanation();
					this.pelletListener.status = Status.inconsistent(explanation);
					log.error(ioe);
					log.error(explanation);
				} catch (Exception e) {
					this.pelletListener.status = Status.ERROR;
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
