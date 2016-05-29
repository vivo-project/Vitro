/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.List;
import java.util.Map;

/**
 * The response to a query against the search index. It includes a list of the
 * results, as well as an optional collection of facet fields.
 */
public interface SearchResponse {

	/**
	 * May return an empty list, but never null.
	 */
	SearchResultDocumentList getResults();

	/**
	 * May return an empty map, but never null.
	 */
	Map<String, Map<String, List<String>>> getHighlighting();

	/**
	 * May return null.
	 */
	SearchFacetField getFacetField(String name);

	/**
	 * May return an empty list, but never null.
	 */
	List<SearchFacetField> getFacetFields();

}
