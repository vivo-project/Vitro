/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

	private boolean faceting;
	private final Set<String> facetFields = new HashSet<>();
	private final Set<String> facetQueries = new HashSet<>();
	private int facetMinCount = -1;

	private final Map<String, List<String>> parameterMap = new HashMap<>();

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
	public SearchQuery setFaceting(boolean b) {
		this.faceting = b;
		return this;
	}

	@Override
	public SearchQuery addFacetFields(String... fields) {
		facetFields.addAll(Arrays.asList(fields));
		return this;
	}

	@Override
	public SearchQuery addFacetQueries(String... queries) {
		facetQueries.addAll(Arrays.asList(queries));
		return this;
	}

	@Override
	public SearchQuery setFacetMinCount(int cnt) {
		facetMinCount = cnt;
		return this;
	}

	@Override
	public SearchQuery addParameter(String name, String... values) {
		parameterMap.put(name, Collections.unmodifiableList(new ArrayList<>(
				Arrays.asList(values))));
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
	public boolean isFaceting() {
		return faceting;
	}

	@Override
	public Set<String> getFacetFields() {
		return Collections.unmodifiableSet(facetFields);
	}

	@Override
	public Set<String> getFacetQueries() {
		return Collections.unmodifiableSet(facetQueries);
	}

	@Override
	public int getFacetMinCount() {
		return facetMinCount;
	}

	@Override
	public Map<String, List<String>> getParameterMap() {
		return Collections.unmodifiableMap(parameterMap);
	}

	@Override
	public String toString() {
		return "BaseSearchQuery [queryText=" + queryText + ", start=" + start
				+ ", rows=" + rows + ", fieldsToReturn=" + fieldsToReturn
				+ ", sortFields=" + sortFields + ", filters=" + filters
				+ ", faceting=" + faceting + ", facetFields=" + facetFields
				+ ", facetQueries=" + facetQueries + ", facetMinCount="
				+ facetMinCount + ", parameterMap=" + parameterMap + "]";
	}

}
