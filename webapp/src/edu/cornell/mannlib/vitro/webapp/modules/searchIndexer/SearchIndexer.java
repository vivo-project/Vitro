/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchIndexer;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.modules.Application;

/**
 * Interface for the code that controls the contents of the search index.
 * 
 * The only calls that are valid after shutdown are shutdown(), getStatus() and
 * removeListener().
 */
public interface SearchIndexer extends Application.Module {
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
	 * @throws IllegalStateException
	 *             if called after shutdown()
	 */
	void rebuildIndex();

	/**
	 * Stop processing new tasks. Requests will be queued until a call to
	 * unpause().
	 * 
	 * @throws IllegalStateException
	 *             if called after shutdown()
	 */
	void pause();

	/**
	 * Resume processing new tasks. Any requests that were received since the
	 * call to pause() will now be scheduled for processing.
	 * 
	 * Has no effect if called after shutdown().
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

			START_PROCESSING_URIS, STOP_PROCESSING_URIS,

			START_PROCESSING_STATEMENTS, STOP_PROCESSING_STATEMENTS,

			REBUILD_REQUESTED, REBUILD_COMPLETE,

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
	}

}
