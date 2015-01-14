/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.tasks;

import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.START_REBUILD;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event.Type.STOP_REBUILD;
import static edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.State.REBUILDING;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer.Event;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.RebuildCounts;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerStatus.State;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.ListenerList;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.Task;
import edu.cornell.mannlib.vitro.webapp.searchindex.SearchIndexerImpl.WorkerThreadPool;
import edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.DocumentModifierList;
import edu.cornell.mannlib.vitro.webapp.searchindex.exclusions.SearchIndexExcluderList;

/**
 * Get the URIs of all individuals in the model. Update each of their search
 * documents.
 * 
 * Delete all search documents that have not been updated since this rebuild
 * began. That removes all obsolete documents from the index.
 */
public class RebuildIndexTask implements Task {
	private static final Log log = LogFactory.getLog(RebuildIndexTask.class);

	private final IndividualDao indDao;
	private final SearchIndexExcluderList excluders;
	private final DocumentModifierList modifiers;
	private final ListenerList listeners;
	private final WorkerThreadPool pool;
	private final SearchEngine searchEngine;

	private final Date requestedAt;
	private final int documentsBefore;

	private volatile SearchIndexerStatus status;

	public RebuildIndexTask(SearchIndexExcluderList excluders,
			DocumentModifierList modifiers, IndividualDao indDao,
			ListenerList listeners, WorkerThreadPool pool) {
		this.excluders = excluders;
		this.modifiers = modifiers;
		this.indDao = indDao;
		this.listeners = listeners;
		this.pool = pool;

		this.searchEngine = ApplicationUtils.instance().getSearchEngine();

		this.requestedAt = new Date();
		this.documentsBefore = getDocumentCount();
		this.status = buildStatus(REBUILDING, 0);
	}

	@Override
	public void run() {
		listeners.fireEvent(new Event(START_REBUILD, status));

		Collection<String> uris = getAllUrisInTheModel();

		if (!isInterrupted()) {
			updateTheUris(uris);
			if (!isInterrupted()) {
				deleteOutdatedDocuments();
			}
		}

		status = buildStatus(REBUILDING, getDocumentCount());
		listeners.fireEvent(new Event(STOP_REBUILD, status));
	}

	private boolean isInterrupted() {
		if (Thread.interrupted()) {
			Thread.currentThread().interrupt();
			return true;
		} else {
			return false;
		}
	}

	private Collection<String> getAllUrisInTheModel() {
		return indDao.getAllIndividualUris();
	}

	private void updateTheUris(Collection<String> uris) {
		new UpdateUrisTask(uris, excluders, modifiers, indDao, listeners, pool)
				.run();
	}

	private void deleteOutdatedDocuments() {
		String query = "indexedTime:[ * TO " + requestedAt.getTime() + " ]";
		try {
			searchEngine.deleteByQuery(query);
			searchEngine.commit();
		} catch (SearchEngineException e) {
			log.warn("Failed to delete outdated documents "
					+ "from the search index", e);
		}
	}

	private int getDocumentCount() {
		try {
			return searchEngine.documentCount();
		} catch (SearchEngineException e) {
			log.warn("Failed to get docoument count from the search index.", e);
			return 0;
		}
	}

	private SearchIndexerStatus buildStatus(State state, int documentsAfter) {
		return new SearchIndexerStatus(state, new Date(), new RebuildCounts(
				documentsBefore, documentsAfter));
	}

	@Override
	public SearchIndexerStatus getStatus() {
		return status;
	}

	@Override
	public void notifyWorkUnitCompletion(Runnable workUnit) {
		// We don't submit any work units, so we won't see any calls to this.
		log.error("Why was this called?");
	}

	@Override
	public String toString() {
		return "RebuildIndexTask[requestedAt="
				+ new SimpleDateFormat().format(requestedAt) + "]";
	}

}
