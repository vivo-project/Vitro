/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.indexing;

import java.util.Set;

import org.apache.jena.rdf.model.Statement;

/**
 * An ordered list of IndexingUriFinder objects, in a handy package.
 * 
 * Implementations should make a protective copy of the list of
 * IndexingUriFinders. Implementations must be thread-safe.
 * 
 * The life-cycle is:
 * 
 * <pre>
 * startIndexing(), 
 * 0 or more findAdditionalUris() by multiple threads, 
 * stopIndexing().
 * </pre>
 */
public interface IndexingUriFinderList {

	/**
	 * Do any required setup on the individual finders.
	 */
	void startIndexing();

	/**
	 * Do any required teardown on the individual finders.
	 */
	void stopIndexing();

	/**
	 * Exercise the list of finders, and return a set of the URIs that they
	 * found for this statement.
	 */
	Set<String> findAdditionalUris(Statement stmt);

}
