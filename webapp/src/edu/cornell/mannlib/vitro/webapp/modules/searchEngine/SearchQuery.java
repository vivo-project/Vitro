/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

import java.util.Collection;
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
	 * 
	 * @return this query
	 */
	SearchQuery setQuery(String query);

	/**
	 * Where in the ordered list of result documents should the response begin?
	 * That is, how many of the results should be skipped? (allows paging of
	 * results). The default is 0.
	 * 
	 * @return this query
	 */
	SearchQuery setStart(int start);

	/**
	 * What is the maximum number of documents that will be returned from the
	 * query? A negative value means no limit. The default is -1.
	 * 
	 * @return this query
	 */
	SearchQuery setRows(int rows);

	/**
	 * Which fields should be returned from the query?
	 * 
	 * @return this query
	 */
	SearchQuery addFields(String... names);

	/**
	 * Which fields should be returned from the query?
	 * 
	 * @return this query
	 */
	SearchQuery addFields(Collection<String> names);

	/**
	 * What field should be used to sort the results, and in what order?
	 * 
	 * @return this query
	 */
	SearchQuery addSortField(String name, Order order);

	/**
	 * Restrict the results by this query.
	 * 
	 * @return this query
	 */
	SearchQuery addFilterQuery(String filterQuery);

	/**
	 * Restrict the results by these queries.
	 * 
	 * @return this query
	 */
	SearchQuery addFilterQueries(String... filterQueries);

	/**
	 * What fields should be used to facet the results?
	 * 
	 * @return this query
	 */
	SearchQuery addFacetFields(String... fields);

	/**
	 * The maximum number of facet counts that will be returned from the query.
	 * The default is 100. A negative value means no limit.
	 * 
	 * @return this query
	 */
	SearchQuery setFacetLimit(int cnt);

	/**
	 * Facet having fewer hits will be excluded from the list. The default is 0.
	 * 
	 * @return this query
	 */
	SearchQuery setFacetMinCount(int cnt);

	/**
	 * @return The text of the query. May be empty, but never null.
	 */
	String getQuery();

	int getStart();

	/**
	 * @return A negative value means that no limit has been specified.
	 */
	int getRows();

	/**
	 * @return May return an empty set, but never null.
	 */
	Set<String> getFieldsToReturn();

	/**
	 * @return May return an empty map, but never null.
	 */
	Map<String, SearchQuery.Order> getSortFields();

	/**
	 * @return May return an empty set, but never null.
	 */
	Set<String> getFilters();

	/**
	 * @return May return an empty set, but never null.
	 */
	Set<String> getFacetFields();

	/**
	 * @return A negative value means that no limit has been specified.
	 */
	int getFacetLimit();

	/**
	 * @return A negative value means that no limit has been specified.
	 */
	int getFacetMinCount();
}
