/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

/**
 * A collection of results that are returned from a query.
 */
public interface SearchResultDocumentList extends
		Iterable<SearchResultDocument> {

	/**
	 * The number of documents that would satisfy the query
	 */
	long getNumFound();

	/**
	 * The number of documents that are included in this result.
	 */
	int size();

	/**
	 * Retrieve the i'th document, starting with 0.
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	SearchResultDocument get(int i);

}
