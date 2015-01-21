/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.modules.Application;

/**
 * The principle interface for the SearchEngine. All search-related objects are
 * created by these methods.
 * 
 * All methods that throw SearchEngineException should attempt to distinguish
 * whether the exception is caused because the SearchEngine is not responding.
 * In that case, they should throw a SearchEngineNotRespondingException, so the
 * client code can choose to respond accordingly.
 */
public interface SearchEngine extends Application.Module {

	/**
	 * Check to see whether the SearchEngine is alive.
	 * 
	 * @throws SearchEngineException
	 *             if the SearchEngine does not respond.
	 */
	void ping() throws SearchEngineException;

	// ----------------------------------------------------------------------
	// Indexing operations
	// ----------------------------------------------------------------------

	/**
	 * Create a SearchInputDocument that can be populated and added to the
	 * index.
	 */
	SearchInputDocument createInputDocument();

	/**
	 * Add documents to the search index.
	 */
	void add(SearchInputDocument... docs) throws SearchEngineException;

	/**
	 * Add documents to the search index.
	 */
	void add(Collection<SearchInputDocument> docs) throws SearchEngineException;

	/**
	 * Explicitly commit all pending changes, and wait until they are visible to
	 * the search.
	 */
	void commit() throws SearchEngineException;

	/**
	 * Explicitly commit all pending changes, and optionally wait until they are
	 * visible to the search.
	 */
	void commit(boolean wait) throws SearchEngineException;

	/**
	 * Delete documents from the search index, by unique ID.
	 */
	void deleteById(String... ids) throws SearchEngineException;

	/**
	 * Delete documents from the search index, by unique ID.
	 */
	void deleteById(Collection<String> ids) throws SearchEngineException;

	/**
	 * Delete documents from the search index if they satisfy the query.
	 */
	void deleteByQuery(String query) throws SearchEngineException;

	// ----------------------------------------------------------------------
	// Searching operations
	// ----------------------------------------------------------------------

	/**
	 * Create a SearchQuery that can be populated and used for searching.
	 */
	SearchQuery createQuery();

	/**
	 * Convenience method to create a SearchQuery and set the query text in one
	 * step.
	 */
	SearchQuery createQuery(String queryText);

	/**
	 * Query the search index and return the results. Response is never null.
	 */
	SearchResponse query(SearchQuery query) throws SearchEngineException;

	/**
	 * Find the number of documents in the search index.
	 */
	int documentCount() throws SearchEngineException;
}
