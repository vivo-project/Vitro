/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;

/**
 * A foundation class for implementing SearchResponse;
 */
public class BaseSearchResponse implements SearchResponse {
	private final Map<String, Map<String, List<String>>> highlighting;
	private final Map<String, SearchFacetField> facetFields;
	private final SearchResultDocumentList results;

	public BaseSearchResponse(
			Map<String, Map<String, List<String>>> highlighting,
			Map<String, SearchFacetField> facetFields,
			SearchResultDocumentList results) {
		this.highlighting = highlighting;
		this.facetFields = facetFields;
		this.results = results;
	}

	@Override
	public SearchResultDocumentList getResults() {
		return results;
	}

	@Override
	public Map<String, Map<String, List<String>>> getHighlighting() {
		return Collections.unmodifiableMap(highlighting);
	}

	@Override
	public SearchFacetField getFacetField(String name) {
		return facetFields.get(name);
	}

	@Override
	public List<SearchFacetField> getFacetFields() {
		return new ArrayList<>(facetFields.values());
	}

	@Override
	public String toString() {
		return "BaseSearchResponse[highlighting=" + highlighting
				+ ", facetFields=" + facetFields + ", results=" + results + "]";
	}

}
