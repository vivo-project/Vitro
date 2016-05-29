/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;

/**
 * A foundation class for implementing SearchQuery.
 */
public class BaseSearchQuery implements SearchQuery {
	private String queryText;
	private int start = 0;
	private int rows = -1;

	private final Set<String> fieldsToReturn = new HashSet<>();
	private final Map<String, SearchQuery.Order> sortFields = new HashMap<>();
	private final Set<String> filters = new HashSet<>();

	private final Set<String> facetFields = new HashSet<>();
	private int facetLimit = 100;
	private int facetMinCount = -1;

	@Override
	public SearchQuery setQuery(String query) {
		this.queryText = query;
		return this;
	}

	@Override
	public SearchQuery setStart(int start) {
		this.start = start;
		return this;
	}

	@Override
	public SearchQuery setRows(int rows) {
		this.rows = rows;
		return this;
	}

	@Override
	public SearchQuery addFields(String... names) {
		return addFields(Arrays.asList(names));
	}

	@Override
	public SearchQuery addFields(Collection<String> names) {
		this.fieldsToReturn.addAll(names);
		return this;
	}

	@Override
	public SearchQuery addSortField(String name, Order order) {
		sortFields.put(name, order);
		return this;
	}

	@Override
	public SearchQuery addFilterQuery(String filterQuery) {
		filters.add(filterQuery);
		return this;
	}

	@Override
	public SearchQuery addFilterQueries(String... filterQueries) {
		this.filters.addAll(Arrays.asList(filterQueries));
		return this;
	}

	@Override
	public SearchQuery addFacetFields(String... fields) {
		facetFields.addAll(Arrays.asList(fields));
		return this;
	}

	@Override
	public SearchQuery setFacetLimit(int cnt) {
		facetLimit = cnt;
		return this;
	}

	@Override
	public SearchQuery setFacetMinCount(int cnt) {
		facetMinCount = cnt;
		return this;
	}

	@Override
	public String getQuery() {
		return queryText;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getRows() {
		return rows;
	}

	@Override
	public Set<String> getFieldsToReturn() {
		return Collections.unmodifiableSet(fieldsToReturn);
	}

	@Override
	public Map<String, SearchQuery.Order> getSortFields() {
		return Collections.unmodifiableMap(sortFields);
	}

	@Override
	public Set<String> getFilters() {
		return Collections.unmodifiableSet(filters);
	}

	@Override
	public Set<String> getFacetFields() {
		return Collections.unmodifiableSet(facetFields);
	}

	@Override
	public int getFacetLimit() {
		return facetLimit;
	}

	@Override
	public int getFacetMinCount() {
		return facetMinCount;
	}

	@Override
	public String toString() {
		return "BaseSearchQuery[queryText=" + queryText + ", start=" + start
				+ ", rows=" + rows + ", fieldsToReturn=" + fieldsToReturn
				+ ", sortFields=" + sortFields + ", filters=" + filters
				+ ", facetFields=" + facetFields + ", facetLimit=" + facetLimit
				+ ", facetMinCount=" + facetMinCount + "]";
	}

}
