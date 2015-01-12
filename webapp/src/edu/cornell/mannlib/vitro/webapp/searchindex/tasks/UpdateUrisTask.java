/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.tasks;

import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.PROGRESS;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.START_PROCESSING_URIS;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.STOP_PROCESSING_URIS;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.State.PROCESSING_URIS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.UriCounts;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerUtils;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.ListenerList;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.Task;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.WorkerThreadPool;
import edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.DocumentModifier;
import edu.cornell.mannlib.vitro.webapp.searchindex.exclusions.SearchIndexExcluder;

/**
 * Given a list of URIs, remove the ones that don't belong in the index and
 * update the ones that do belong.
 * 
 * A URI doesn't belong in the index if there is no individual with that URI, or
 * if the individual has no VClasses assigned to it, or if the individual is
 * excluded by one of the excluders.
 * 
 * Deletions are done synchronously, but updates are scheduled to run on the
 * thread pool.
 */
public class UpdateUrisTask implements Task {
	private static final Log log = LogFactory.getLog(UpdateUrisTask.class);

	private final Set<String> uris;
	private final IndividualDao indDao;
	private final List<SearchIndexExcluder> excluders;
	private final List<DocumentModifier> modifiers;
	private final ListenerList listeners;
	private final WorkerThreadPool pool;

	private final Status status;
	private final SearchEngine searchEngine;

	public UpdateUrisTask(Collection<String> uris,
			Collection<SearchIndexExcluder> excluders,
			Collection<DocumentModifier> modifiers, IndividualDao indDao,
			ListenerList listeners, WorkerThreadPool pool) {
		this.uris = new HashSet<>(uris);
		this.excluders = new ArrayList<>(excluders);
		this.modifiers = new ArrayList<>(modifiers);
		this.indDao = indDao;
		this.listeners = listeners;
		this.pool = pool;

		this.status = new Status(uris.size(), 200, listeners);

		this.searchEngine = ApplicationUtils.instance().getSearchEngine();
	}

	@Override
	public void run() {
		listeners.fireEvent(new Event(START_PROCESSING_URIS, status
				.getSearchIndexerStatus()));
		for (String uri : uris) {
			if (isInterrupted()) {
				log.info("Interrupted: " + status.getSearchIndexerStatus());
				return;
			} else {
				Individual ind = getIndividual(uri);
				if (ind == null || hasNoClass(ind) || isExcluded(ind)) {
					deleteDocument(uri);
				} else {
					updateDocument(ind);
				}
			}
		}
		pool.waitUntilIdle();
		commitChanges();
		listeners.fireEvent(new Event(STOP_PROCESSING_URIS, status
				.getSearchIndexerStatus()));
	}

	private void commitChanges() {
		try {
			searchEngine.commit();
		} catch (SearchEngineException e) {
			log.error("Failed to commit the changes.");
		}
	}

	private boolean isInterrupted() {
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt();
			return true;
		} else {
			return false;
		}
	}

	private Individual getIndividual(String uri) {
		Individual ind = indDao.getIndividualByURI(uri);
		if (ind == null) {
			log.debug("Found no individual for '" + uri + "'");
		}
		return ind;
	}

	private boolean hasNoClass(Individual ind) {
		List<VClass> vclasses = ind.getVClasses(false);
		if (vclasses == null || vclasses.isEmpty()) {
			log.debug("Individual " + ind + " has no classes.");
			return true;
		}
		return false;
	}

	private boolean isExcluded(Individual ind) {
		for (SearchIndexExcluder excluder : excluders) {
			String message = excluder.checkForExclusion(ind);
			if (message != SearchIndexExcluder.DONT_EXCLUDE) {
				log.debug("Excluded " + ind + " because " + message);
				return true;
			}
		}
		return false;
	}

	/** A delete is fast enough to be done synchronously. */
	private void deleteDocument(String uri) {
		try {
			searchEngine.deleteById(SearchIndexerUtils.getIdForUri(uri));
			status.incrementDeletes();
			log.debug("deleted '" + uri + "' from search index.");
		} catch (SearchEngineException e) {
			log.warn("Failed to delete '" + uri + "' from search index", e);
		}
	}

	private void updateDocument(Individual ind) {
		Runnable workUnit = new UpdateDocumentWorkUnit(ind, modifiers);
		pool.submit(workUnit, this);
		log.debug("scheduled update to " + ind);
	}

	@Override
	public void notifyWorkUnitCompletion(Runnable workUnit) {
		log.debug("completed update to "
				+ ((UpdateDocumentWorkUnit) workUnit).getInd());
		status.incrementUpdates();
	}

	@Override
	public SearchIndexerStatus getStatus() {
		return status.getSearchIndexerStatus();
	}

	// ----------------------------------------------------------------------
	// helper classes
	// ----------------------------------------------------------------------

	/**
	 * A thread-safe collection of status information. All methods are
	 * synchronized.
	 */
	private static class Status {
		private final int total;
		private final int progressInterval;
		private final ListenerList listeners;
		private int updated = 0;
		private int deleted = 0;
		private Date since = new Date();

		public Status(int total, int progressInterval, ListenerList listeners) {
			this.total = total;
			this.progressInterval = progressInterval;
			this.listeners = listeners;
		}

		public synchronized void incrementUpdates() {
			updated++;
			since = new Date();
			maybeFireProgressEvent();
		}

		public synchronized void incrementDeletes() {
			deleted++;
			since = new Date();
		}

		private void maybeFireProgressEvent() {
			if (updated > 0 && updated % progressInterval == 0) {
				listeners.fireEvent(new Event(PROGRESS,
						getSearchIndexerStatus()));
			}
		}

		public synchronized SearchIndexerStatus getSearchIndexerStatus() {
			int remaining = total - updated - deleted;
			return new SearchIndexerStatus(PROCESSING_URIS, since,
					new UriCounts(deleted, updated, remaining, total));
		}

	}

}
