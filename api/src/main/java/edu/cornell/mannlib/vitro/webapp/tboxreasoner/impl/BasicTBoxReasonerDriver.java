/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.tboxreasoner.impl;

import static edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel.IDLE;
import static edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread.WorkLevel.WORKING;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.modules.tboxreasoner.TBoxReasonerStatus;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ConfiguredReasonerListener;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.InferenceModelUpdater;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.PatternListBuilder;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerConfiguration;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.ReasonerStatementPattern;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxChanges;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasoner;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasoner.Status;
import edu.cornell.mannlib.vitro.webapp.tboxreasoner.TBoxReasonerDriver;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockableOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.jena.criticalsection.LockedOntModel;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * The basic implementation of the TBoxReasonerDriver. It gets help from a
 * listener, an executor, and a reasoner.
 * 
 * Create a listener that listens for changes to the TBox, but filters them
 * according to a ReasonerConfiguration object. The listener accumulates the
 * changes it likes, until it detects an ending EditEvent. Then it passes the
 * change set back to the driver.
 * 
 * Each time a change set is received, a task is created and given to the
 * executor to run. The executor is single-threaded, so the change sets are
 * processed in sequence.
 * 
 * Processing involves the following steps:
 * 
 * 1. Telling the reasoner about the changes, so it can update its own internal
 * ontology model.
 * 
 * 2. Telling the reasoner to re-inference its model. A status is returned.
 * 
 * 3. Asking the reasoner for the inferences from its model. As with the initial
 * changes, these inferences are filtered according to the
 * ReasonerConfiguration.
 * 
 * 4. Synchronizing the applications TBox inferences model with the inferences
 * obtained from the reasoner.
 * 
 * ----------------------
 * 
 * Possible optimization: if change sets come in quickly enough that the third
 * set is received while the first is still being processed, it would be
 * reasonable to merge the second and third sets into one.
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

	private TBoxReasoner.Status innerStatus;

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

		this.pendingChangeSets = Collections
				.synchronizedSet(new HashSet<TBoxChanges>());

		this.executorService = Executors.newFixedThreadPool(1,
				new VitroBackgroundThread.Factory("TBoxReasoner"));

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
	public TBoxReasonerStatus getStatus() {
		return new FullStatus(innerStatus, !pendingChangeSets.isEmpty());
	}

	@Override
	public void runSynchronizer(TBoxChanges changeSet) {
		if (!changeSet.isEmpty()) {
			executorService.execute(new ReasoningTask(changeSet));
		}
	}

	/**
	 * Shut down the thread that runs the reasoning tasks. Don't wait longer
	 * than 1 minute.
	 */
	public void shutdown() {
		executorService.shutdown();
		int waited = 0;
		while (waited < 60 && !executorService.isTerminated()) {
			try {
				log.info("Waiting for TBox reasoner to terminate.");
				executorService.awaitTermination(5, TimeUnit.SECONDS);
				waited += 5;
			} catch (InterruptedException e) {
				// Should never happen.
				e.printStackTrace();
				break;
			}
		}
		if (!executorService.isTerminated()) {
			log.warn("Forcing TBox reasoner to terminate.");
			executorService.shutdownNow();
		}
		if (!executorService.isTerminated()) {
			log.error("TBox reasoner did not terminate.");
		}
	}

	private class ReasoningTask implements Runnable {
		private final TBoxChanges changes;
		private List<ReasonerStatementPattern> patternList;

		public ReasoningTask(TBoxChanges changes) {
			this.changes = changes;
			pendingChangeSets.add(changes);
		}

		@Override
		public void run() {
			try {
				setWorking();

				reasoner.updateReasonerModel(changes);
				innerStatus = reasoner.performReasoning();

				buildPatternList();
				updateInferencesModel();

				setIdle();
			} finally {
				pendingChangeSets.remove(changes);
			}
		}

		private void setWorking() {
			Thread current = Thread.currentThread();
			if (current instanceof VitroBackgroundThread) {
				((VitroBackgroundThread) current).setWorkLevel(WORKING);
			}
		}

		private void setIdle() {
			Thread current = Thread.currentThread();
			if (current instanceof VitroBackgroundThread) {
				((VitroBackgroundThread) current).setWorkLevel(IDLE);
			}
		}

		private void buildPatternList() {
			PatternListBuilder patternListBuilder = new PatternListBuilder(
					reasonerConfiguration, reasoner, changes);
			this.patternList = patternListBuilder.build();
		}

		private void updateInferencesModel() {
			InferenceModelUpdater inferenceModelUpdater = new InferenceModelUpdater(
					reasoner, lockableInferencesModel, lockableFullModel,
					listener);
			inferenceModelUpdater.update(patternList);
		}
	}

	private static class FullStatus implements TBoxReasonerStatus {
		private final TBoxReasoner.Status reasonerStatus;
		private final boolean reasoning;

		public FullStatus(Status reasonerStatus, boolean reasoning) {
			this.reasonerStatus = reasonerStatus;
			this.reasoning = reasoning;
		}

		@Override
		public boolean isReasoning() {
			return reasoning;
		}

		@Override
		public boolean isConsistent() {
			return reasonerStatus.isConsistent();
		}

		@Override
		public boolean isInErrorState() {
			return reasonerStatus.isInErrorState();
		}

		@Override
		public String getExplanation() {
			String explanation = reasonerStatus.getExplanation();
			return explanation == null ? "" : explanation;
		}

	}

}
