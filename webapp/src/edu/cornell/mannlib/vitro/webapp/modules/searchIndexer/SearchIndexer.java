/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchIndexer;

import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.modules.Application;

/**
 * Interface for the code that controls the contents of the search index.
 * 
 * If calls are made to schedule tasks prior to startup(), they will be queued,
 * since the indexer is created in paused mode.
 * 
 * The only calls that are valid after shutdown are shutdown(), getStatus() and
 * removeListener().
 */
public interface SearchIndexer extends Application.Module {
	/**
	 * Update any search documents that are affected by these statements.
	 * 
	 * These statements are a mixture of additions and deletions. In either
	 * case, we feed them to the URI finders to see what individuals might have
	 * been affected by the change.
	 * 
	 * We accumulate a batch of affected URIs, removing duplicates if they
	 * occur, and then submit them for updates.
	 * 
	 * If called before startup or while paused, this task will be queued.
	 * 
	 * @param urls
	 *            if null or empty, this call has no effect.
	 * @throws IllegalStateException
	 *             if called after shutdown()
	 */
	void scheduleUpdatesForStatements(List<Statement> changes);

	/**
	 * Update the search documents for these URIs.
	 * 
	 * For each URI that belongs in the index, a new search document is built,
	 * replacing any document that may already exist for that URI. For each URI
	 * that does not belong in the index, any existing document is removed.
	 * 
	 * A URI belongs in the index if it refers to an existing individual in the
	 * model, and is not excluded.
	 * 
	 * If called before startup or while paused, this task will be queued.
	 * 
	 * @param uris
	 *            if null or empty, this call has no effect.
	 * @throws IllegalStateException
	 *             if called after shutdown()
	 */
	void scheduleUpdatesForUris(Collection<String> uris);

	/**
	 * Remove all of the existing documents in the index, and schedule updates
	 * for all of the individuals in the model.
	 * 
	 * If a rebuild is already pending or in progress, this method has no
	 * effect.
	 * 
	 * If called before startup or while paused, this task will be queued.
	 * 
	 * @throws IllegalStateException
	 *             if called after shutdown()
	 */
	void rebuildIndex();

	/**
	 * Stop processing new tasks. Requests will be queued until a call to
	 * unpause(). Fires a PAUSED event to listeners.
	 * 
	 * The SearchIndexer is paused when created. When fully initialized, it
	 * should be unpaused.
	 * 
	 * If already paused, this call has no effect.
	 * 
	 * @throws IllegalStateException
	 *             if called after shutdown()
	 */
	void pause();

	/**
	 * Resume processing new tasks. Any requests that were received since the
	 * call to pause() will now be scheduled for processing. Fires an UNPAUSED
	 * event to listeners.
	 * 
	 * The SearchIndexer is paused when created. When fully initialized, it
	 * should be unpaused.
	 * 
	 * Has no effect if called after shutdown() or if not paused.
	 */
	void unpause();

	/**
	 * What is the current status of the indexer?
	 * 
	 * Still valid after shutdown().
	 */
	SearchIndexerStatus getStatus();

	/**
	 * Add this listener, allowing it to receive events from the indexer. If
	 * this listener has already been added, this method has no effect.
	 * 
	 * @param listener
	 *            if null, this method has no effect.
	 * @throws IllegalStateException
	 *             if called after shutdown()
	 */
	void addListener(Listener listener);

	/**
	 * Remove this listener, meaning that it will no longer receive events from
	 * the indexer. If this listener is not active, this method has no effect.
	 * 
	 * Has no effect if called after shutdown().
	 * 
	 * @param listener
	 *            if null, this method has no effect.
	 */
	void removeListener(Listener listener);

	/**
	 * Stop processing and release resources. This call should block until the
	 * dependent threads are stopped.
	 * 
	 * Repeated calls have no effect.
	 */
	@Override
	void shutdown(Application app);

	/**
	 * A listener that will be notified of events from the SearchIndexer.
	 */
	public static interface Listener {
		void receiveSearchIndexerEvent(Event event);
	}

	/**
	 * An immutable event object. The event type describes just what happened.
	 * The status object describes what the indexer is doing now.
	 */
	public static class Event {
		public enum Type {
			STARTUP, PROGRESS,

			PAUSE, UNPAUSE,

			START_PROCESSING_URIS, STOP_PROCESSING_URIS,

			START_PROCESSING_STATEMENTS, STOP_PROCESSING_STATEMENTS,

			START_REBUILD, STOP_REBUILD,

			SHUTDOWN_REQUESTED, SHUTDOWN_COMPLETE
		}

		private final Type type;
		private final SearchIndexerStatus status;

		public Event(Type type, SearchIndexerStatus status) {
			this.type = type;
			this.status = status;
		}

		public Type getType() {
			return type;
		}

		public SearchIndexerStatus getStatus() {
			return status;
		}

		@Override
		public String toString() {
			return type + ", " + status;
		}
	}

}
