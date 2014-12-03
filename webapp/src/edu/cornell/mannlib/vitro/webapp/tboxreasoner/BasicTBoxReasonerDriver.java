/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedOntModel;

/**
 * The basic implementation of the TBoxReasonerDriver. 
 */
public class BasicTBoxReasonerDriver implements TBoxReasonerDriver {
	private static final Log log = LogFactory
			.getLog(BasicTBoxReasonerDriver.class);

	private final LockableOntModel lockableAssertionsModel;
	private final LockableModel lockableInferencesModel;
	private final LockableOntModel lockableFullModel;

	private final ReasonerConfiguration reasonerConfiguration;

	private final ConfiguredReasonerListener listener;

	private final Set<TBoxChanges> pendingChangeSets;
	
	private final ExecutorService executorService;
	
	private final TBoxReasoner reasoner;
	
	private TBoxReasonerDriver.Status status;

	public BasicTBoxReasonerDriver(OntModel assertionsModel,
			Model inferencesModel, OntModel fullModel, TBoxReasoner reasoner,
			ReasonerConfiguration reasonerConfiguration) {
		this.lockableAssertionsModel = new LockableOntModel(assertionsModel);
		this.lockableInferencesModel = new LockableModel(inferencesModel);
		this.lockableFullModel = new LockableOntModel(fullModel);
		this.reasoner = reasoner;
		this.reasonerConfiguration = reasonerConfiguration;
		
		this.listener = new ConfiguredReasonerListener(reasonerConfiguration,
				this);
		
		this.pendingChangeSets = Collections.synchronizedSet(new HashSet<TBoxChanges>());
		
		this.executorService = Executors.newFixedThreadPool(1);

		assertionsModel.getBaseModel().register(listener);
		fullModel.getBaseModel().register(listener);
		
		doInitialReasoning();
	}

	private void doInitialReasoning() {
		try (LockedOntModel assertionsModel = lockableAssertionsModel.read()) {
			for (ReasonerStatementPattern pat : reasonerConfiguration
					.getInferenceDrivingPatternAllowSet()) {
				listener.addedStatements(assertionsModel.listStatements(
						(Resource) null, pat.getPredicate(), (RDFNode) null));
			}
		}
		listener.notifyEvent(null, new EditEvent(null, false));
	}
	
	

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public boolean isReasoning() {
		return !pendingChangeSets.isEmpty();
	}

	@Override
	public void runSynchronizer(TBoxChanges changeSet) {
		if (!changeSet.isEmpty()) {
			executorService.execute(new ReasoningTask(changeSet));
		}
	}
	
	private  class ReasoningTask implements Runnable {
		private final TBoxChanges changes;
		private List<ReasonerStatementPattern> patternList;

		public ReasoningTask(TBoxChanges changes) {
			this.changes = changes;
			pendingChangeSets.add(changes);
		}

		@Override
		public void run() {
			try {
				reasoner.updateReasonerModel(changes);
				status = reasoner.performReasoning();
				
				buildPatternList();
				updateInferencesModel();
			} finally {
				pendingChangeSets.remove(changes);
			}
		}

		private void buildPatternList() {
			PatternListBuilder patternListBuilder = new PatternListBuilder(
					reasonerConfiguration, reasoner, changes);
			this.patternList = patternListBuilder.build();
		}

		private void updateInferencesModel() {
			InferenceModelUpdater inferenceModelUpdater = new InferenceModelUpdater(
					reasoner, lockableInferencesModel, lockableFullModel, listener);
			inferenceModelUpdater.update(patternList);
		}
	}

}
