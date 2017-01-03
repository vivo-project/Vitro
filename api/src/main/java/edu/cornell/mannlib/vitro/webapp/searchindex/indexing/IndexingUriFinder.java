/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import java.util.List;

import org.apache.jena.rdf.model.Statement;

/**
 * Interface to use with IndexBuilder to find more URIs to index given a changed
 * statement. The statement may have been added or removed from the model.
 * 
 * Implementing classes must be threadsafe, as multiple threads are used to
 * complete the task.
 * 
 * The life-cycle is as follows: startIndexing(), 0 or more calls to
 * findAdditionalURIsToIndex(), endIndexing().
 * 
 * Repeat as desired.
 */
public interface IndexingUriFinder {

	/**
	 * For the domain that is the responsibility of the given implementation,
	 * calculate the URIs that need to be updated in the search index. The URIs
	 * in the list will be updated by the IndexBuilder, which will handle URIs
	 * of new individuals, URIs of individuals that have changes, and URIs of
	 * individuals that have been removed from the model.
	 * 
	 * @return List of URIs. Never return null.
	 */
	List<String> findAdditionalURIsToIndex(Statement stmt);

	/**
	 * Indicates that a new collection of statements is about to be processed.
	 */
	void startIndexing();

	/**
	 * Indicates that the collection of statements being processed is complete.
	 */
	void endIndexing();
}
