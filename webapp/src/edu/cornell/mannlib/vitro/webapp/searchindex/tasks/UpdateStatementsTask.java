/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.tasks;

import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.PROGRESS;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.START_STATEMENTS;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.STOP_STATEMENTS;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.State.PROCESSING_STMTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.StatementCounts;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.DeferrableTask;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.ListenerList;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.Task;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.WorkerThreadPool;
import edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.DocumentModifierList;
import edu.cornell.mannlib.vitro.webapp.searchindex.exclusions.SearchIndexExcluderList;
import edu.cornell.mannlib.vitro.webapp.searchindex.indexing.IndexingUriFinderList;

/**
 * Receive a collection of statements that have been added to the model, or
 * delete from it.
 * 
 * Find the URIs of search documents that may have been affected by these
 * changes, and update those documents.
 * 
 * -------------------
 * 
 * It would be nice to stream this whole thing, finding the URIs affected by
 * each statement and updating those documents before proceding. However, it is
 * very common for several statements within a group to affect the same
 * document, so that method would result in rebuilding the document several
 * times.
 * 
 * Instead, we final all of the URIs affected by all statements, store them in a
 * Set to remove duplicates, and then process the URIs in the set.
 */
public class UpdateStatementsTask implements Task {
	private static final Log log = LogFactory
			.getLog(UpdateStatementsTask.class);

	private final List<Statement> changes;
	private final IndexingUriFinderList uriFinders;
	private final SearchIndexExcluderList excluders;
	private final DocumentModifierList modifiers;
	private final IndividualDao indDao;
	private final ListenerList listeners;
	private final WorkerThreadPool pool;

	private final Set<String> uris;
	private final Status status;

    public static class Deferrable implements DeferrableTask {
        List<Statement> changes;

        public Deferrable(List<Statement> changes) { this.changes = changes; }

        @Override
        public Task makeRunnable(IndexingUriFinderList uriFinders, SearchIndexExcluderList excluders, DocumentModifierList modifiers, IndividualDao indDao, ListenerList listeners, WorkerThreadPool pool) {
            return new UpdateStatementsTask(changes, uriFinders, excluders, modifiers, indDao, listeners, pool);
        }
    }

    public UpdateStatementsTask(List<Statement> changes,
			IndexingUriFinderList uriFinders,
			SearchIndexExcluderList excluders, DocumentModifierList modifiers,
			IndividualDao indDao, ListenerList listeners, WorkerThreadPool pool) {
		this.changes = new ArrayList<>(changes);
		this.uriFinders = uriFinders;
		this.excluders = excluders;
		this.modifiers = modifiers;
		this.indDao = indDao;
		this.listeners = listeners;
		this.pool = pool;

		this.uris = Collections.synchronizedSet(new HashSet<String>());

		this.status = new Status(changes.size(), 500, listeners);
	}

	@Override
	public void run() {
		listeners.fireEvent(new Event(START_STATEMENTS, getStatus()));

		findAffectedUris();

		updateTheUris();
		listeners.fireEvent(new Event(STOP_STATEMENTS, getStatus()));
	}

	private void findAffectedUris() {
		log.debug("Tell finders we are starting.");
		uriFinders.startIndexing();

		for (Statement stmt : changes) {
			if (isInterrupted()) {
				log.info("Interrupted: " + status.getSearchIndexerStatus());
				return;
			} else {
				findUrisForStatement(stmt);
			}
		}
		waitForWorkUnitsToComplete();

		log.debug("Tell finders we are stopping.");
		uriFinders.stopIndexing();
	}

	private boolean isInterrupted() {
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt();
			return true;
		} else {
			return false;
		}
	}

	private void findUrisForStatement(Statement stmt) {
		Runnable workUnit = new FindUrisForStatementWorkUnit(stmt, uriFinders);
		pool.submit(workUnit, this);
		log.debug("scheduled uri finders for " + stmt);
	}

	private void waitForWorkUnitsToComplete() {
		pool.waitUntilIdle();
	}

	private void updateTheUris() {
		new UpdateUrisTask(uris, excluders, modifiers, indDao, listeners, pool)
				.run();
	}

	@Override
	public SearchIndexerStatus getStatus() {
		return status.getSearchIndexerStatus();
	}

	@Override
	public void notifyWorkUnitCompletion(Runnable workUnit) {
		FindUrisForStatementWorkUnit worker = (FindUrisForStatementWorkUnit) workUnit;

		Set<String> foundUris = worker.getUris();
		Statement stmt = worker.getStatement();
		log.debug("Found " + foundUris.size() + " uris for statement: " + stmt);

		uris.addAll(foundUris);
		status.incrementProcessed();
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * A thread-safe collection of status information. All methods are
	 * synchronized.
	 */
	private static class Status {
		private final int total;
		private final int progressInterval;
		private final ListenerList listeners;
		private int processed = 0;
		private Date since = new Date();

		public Status(int total, int progressInterval, ListenerList listeners) {
			this.total = total;
			this.progressInterval = progressInterval;
			this.listeners = listeners;
		}

		public synchronized void incrementProcessed() {
			processed++;
			since = new Date();
			maybeFireProgressEvent();
		}

		private void maybeFireProgressEvent() {
			if (processed > 0 && processed % progressInterval == 0) {
				listeners.fireEvent(new Event(PROGRESS,
						getSearchIndexerStatus()));
			}
		}

		public synchronized SearchIndexerStatus getSearchIndexerStatus() {
			int remaining = total - processed;
			return new SearchIndexerStatus(PROCESSING_STMTS, since,
					new StatementCounts(processed, remaining, total));
		}

	}

}
