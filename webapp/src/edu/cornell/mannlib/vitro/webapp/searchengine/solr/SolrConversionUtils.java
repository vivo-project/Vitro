/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchengine.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchFacetField;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchFacetField.BaseCount;
import edu.cornell.mannlib.vitro.webapp.searchengine.base.BaseSearchResponse;

/**
 * Utility method for converting from Solr-specific instances to Search-generic
 * instances, and back.
 */
public class SolrConversionUtils {

	// ----------------------------------------------------------------------
	// Convert input documents to Solr-specific.
	// ----------------------------------------------------------------------

	static List<SolrInputDocument> convertToSolrInputDocuments(
			Collection<SearchInputDocument> docs) {
		List<SolrInputDocument> solrDocs = new ArrayList<>();
		for (SearchInputDocument doc : docs) {
			solrDocs.add(convertToSolrInputDocument(doc));
		}
		return solrDocs;
	}

	private static SolrInputDocument convertToSolrInputDocument(
			SearchInputDocument doc) {
		SolrInputDocument solrDoc = new SolrInputDocument(
				convertToSolrInputFieldMap(doc.getFieldMap()));
		solrDoc.setDocumentBoost(doc.getDocumentBoost());
		return solrDoc;
	}

	private static Map<String, SolrInputField> convertToSolrInputFieldMap(
			Map<String, SearchInputField> fieldMap) {
		Map<String, SolrInputField> solrFieldMap = new HashMap<>();
		for (String fieldName : fieldMap.keySet()) {
			solrFieldMap.put(fieldName,
					convertToSolrInputField(fieldMap.get(fieldName)));
		}
		return solrFieldMap;
	}

	private static SolrInputField convertToSolrInputField(
			SearchInputField searchInputField) {
		SolrInputField solrField = new SolrInputField(
				searchInputField.getName());
		solrField.addValue(searchInputField.getValues(),
				searchInputField.getBoost());
		return solrField;
	}

	// ----------------------------------------------------------------------
	// Convert queries to Solr-specific.
	// ----------------------------------------------------------------------

	/**
	 * Convert from a SearchQuery to a SolrQuery, so the Solr server may execute
	 * it.
	 */
	static SolrQuery convertToSolrQuery(SearchQuery query) {
		SolrQuery solrQuery = new SolrQuery(query.getQuery());
		solrQuery.setStart(query.getStart());

		int rows = query.getRows();
		if (rows >= 0) {
			solrQuery.setRows(rows);
		}

		for (String fieldToReturn : query.getFieldsToReturn()) {
			solrQuery.addField(fieldToReturn);
		}

		Map<String, Order> sortFields = query.getSortFields();
		for (String sortField : sortFields.keySet()) {
			solrQuery.addSortField(sortField,
					convertToSolrOrder(sortFields.get(sortField)));
		}

		for (String filter : query.getFilters()) {
			solrQuery.addFilterQuery(filter);
		}

		solrQuery.setFacet(query.isFaceting());

		for (String facetField : query.getFacetFields()) {
			solrQuery.addFacetField(facetField);
		}

		for (String facetQuery : query.getFacetQueries()) {
			solrQuery.addFacetQuery(facetQuery);
		}

		int minCount = query.getFacetMinCount();
		if (minCount >= 0) {
			solrQuery.setFacetMinCount(minCount);
		}

		Map<String, List<String>> parameterMap = query.getParameterMap();
		for (String parameterName : parameterMap.keySet()) {
			String[] values = parameterMap.get(parameterName).toArray(
					new String[0]);
			solrQuery.add(parameterName, values);
		}

		return solrQuery;
	}

	private static ORDER convertToSolrOrder(Order order) {
		if (order == Order.DESC) {
			return ORDER.desc;
		} else {
			return ORDER.asc;
		}
	}

	// ----------------------------------------------------------------------
	// Convert responses to Search-generic
	// ----------------------------------------------------------------------

	static SearchResponse convertToSearchResponse(QueryResponse response) {
		return new BaseSearchResponse(response.getHighlighting(),
				convertToSearchFacetFieldMap(response.getFacetFields()),
				new SolrSearchResultDocumentList(response.getResults()));
	}

	private static Map<String, SearchFacetField> convertToSearchFacetFieldMap(
			List<FacetField> facetFields) {
		Map<String, SearchFacetField> map = new HashMap<>();
		if (facetFields != null) {
			for (FacetField facetField : facetFields) {
				map.put(facetField.getName(),
						convertToSearchFacetField(facetField));
			}
		}
		return map;
	}

	private static SearchFacetField convertToSearchFacetField(
			FacetField facetField) {
		return new BaseSearchFacetField(facetField.getName(),
				convertToSearchFacetFieldCounts(facetField.getValues()));
	}

	private static List<BaseCount> convertToSearchFacetFieldCounts(
			List<FacetField.Count> solrCounts) {
		List<BaseCount> searchCounts = new ArrayList<>();
		if (solrCounts != null) {
			for (FacetField.Count solrCount : solrCounts) {
				searchCounts.add(new BaseCount(solrCount.getName(), solrCount
						.getCount()));
			}
		}
		return searchCounts;
	}

}
