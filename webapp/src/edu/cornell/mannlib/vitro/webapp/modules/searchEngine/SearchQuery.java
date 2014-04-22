/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A collection of search terms that will be used to query the search index.
 * 
 * Each of the "set" and "add" methods will return the searchQuery itself, so
 * calls can easily be chained together.
 */
public interface SearchQuery {
	public enum Order {
		ASC, DESC
	}

	/**
	 * Set the text of the query. This will be parsed using Lucene query syntax.
	 */
	SearchQuery setQuery(String query);

	/**
	 * Where in the ordered list of result documents should the response begin?
	 * That is, how many of the results should be skipped? (allows paging of
	 * results)
	 */
	SearchQuery setStart(int start);

	/**
	 * What is the maximum number of documents that will be returned from the
	 * query?
	 */
	SearchQuery setRows(int rows);

	/**
	 * Which fields should be returned from the query?
	 */
	SearchQuery addFields(String... names);

	/**
	 * Which fields should be returned from the query?
	 */
	SearchQuery addFields(Collection<String> names);

	/**
	 * What field should be used to sort the results, and in what order?
	 */
	SearchQuery addSortField(String name, Order order);

	/**
	 * Restrict the results by thisw query.
	 */
	SearchQuery addFilterQuery(String filterQuery);

	/**
	 * Restrict the results by these queries.
	 */
	SearchQuery addFilterQueries(String... filterQueries);

	/**
	 * Should the results be faceted?
	 */
	SearchQuery setFaceting(boolean b);

	/**
	 * What fields should be used to facet the results?
	 */
	SearchQuery addFacetFields(String... fields);

	/**
	 * Add queries that can be used to facet the results.
	 */
	SearchQuery addFacetQueries(String... queries);

	/**
	 * Facet having fewer hits will be excluded from the list.
	 */
	SearchQuery setFacetMinCount(int cnt);

	/**
	 * Add a system-dependent parameter to the query.
	 */
	SearchQuery addParameter(String name, String... values);

	/**
	 * Get the text of the query.
	 */
	String getQuery();

	int getStart();

	/**
	 * A value of -1 means that no limit has been specified.
	 */
	int getRows();

	/**
	 * May return an empty set, but never null.
	 */
	Set<String> getFieldsToReturn();

	/**
	 * May return an empty map, but never null.
	 */
	Map<String, SearchQuery.Order> getSortFields();

	/**
	 * May return an empty set, but never null.
	 */
	Set<String> getFilters();

	boolean isFaceting();

	/**
	 * May return an empty set, but never null.
	 */
	Set<String> getFacetFields();

	/**
	 * May return an empty set, but never null.
	 */
	Set<String> getFacetQueries();

	/**
	 * A value of -1 means that no limit has been specified.
	 */
	int getFacetMinCount();

	/**
	 * May return an empty map, but never null.
	 */
	Map<String, List<String>> getParameterMap();
}
