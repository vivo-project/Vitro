/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils.searchengine;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchFacetField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputField;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;

/**
 * Produce readable displays of objects related to the search engine.
 */
public class Formatter {
	public static String format(SearchInputDocument doc) {
		StringBuilder sb = new StringBuilder();
		sb.append("SearchInputDocument: Name='").append(getValueFromField(doc, "nameRaw")).append("', URI='").append(getValueFromField(doc, "URI")).append("', boost='").append(doc.getDocumentBoost()).append("', ").append(doc.getFieldMap().size()).append(" fields \n");

		for (SearchInputField field : new TreeMap<>(doc.getFieldMap()).values()) {
			sb.append(format(field, "   ").append('\n'));
		}

		return sb.toString();
	}

	public static String getValueFromField(SearchInputDocument doc,
			String fieldName) {
		SearchInputField field = doc.getField(fieldName);
		if (field == null) {
			return "UNKNOWN";
		} else {
			Object value = field.getFirstValue();
			return (value == null) ? "UNKNOWN" : String.valueOf(value);
		}
	}

	public static String format(SearchInputField field) {
		return format(field, "").toString();
	}

	private static StringBuilder format(SearchInputField field, String padding) {
		StringBuilder sb = new StringBuilder();
		sb.append(padding).append("SearchInputField: Name='").append(field.getName()).append("', boost='").append(field.getBoost()).append("', ").append(field.getValues().size()).append(" values \n");

		for (Object value : field.getValues()) {
			sb.append(padding).append("   '").append(String.valueOf(value))
					.append("' \n");
		}
		return sb;
	}

	public static String format(SearchQuery query) {
		Set<String> returnFields = query.getFieldsToReturn();
		Map<String, Order> sortFields = query.getSortFields();
		Set<String> filters = query.getFilters();
		Set<String> facets = query.getFacetFields();
		return "SearchQuery start='"
				+ query.getStart()
				+ "', rows='"
				+ query.getRows()
				+ "', text='"
				+ query.getQuery()
				+ (returnFields.isEmpty() ? "', No return fields"
						: ("', return fields=" + returnFields))
				+ (sortFields.isEmpty() ? ", No sort fields"
						: (", sort fields=" + sortFields))
				+ (filters.isEmpty() ? ", No filters"
						: (", filters=" + filters))
				+ (facets.isEmpty() ? ", No facets" : (", facets=" + facets
						+ " facet limit=" + query.getFacetLimit()
						+ ", facet minimum=" + query.getFacetMinCount()))
				+ "\n";
	}

	public static String format(SearchResponse response) {
		StringBuilder sb = new StringBuilder();
		sb.append("SearchResponse: ").append(response.getResults().size()).append(" results, ").append(response.getFacetFields().size()).append(" facet fields, ").append(response.getHighlighting().size()).append(" highlights\n");
		for (SearchFacetField facet : response.getFacetFields()) {
			sb.append(format(facet, "      "));
		}
		for (SearchResultDocument result : response.getResults()) {
			sb.append(format(result, "   "));
		}
		return sb.toString();
	}

	private static StringBuilder format(SearchFacetField facet, String padding) {
		StringBuilder sb = new StringBuilder();
		sb.append(padding).append("SearchFacetField: name=")
				.append(facet.getName()).append(", ")
				.append(facet.getValues().size()).append(" values");
		return sb;
	}

	private static String format(SearchResultDocument doc, String padding) {
		StringBuilder sb = new StringBuilder();
		int valuesCount = 0;
		for (Collection<Object> values : doc.getFieldValuesMap().values()) {
			valuesCount += values.size();
		}
		sb.append("SearchResultDocument: Name='").append(getValueFromField(doc, "nameRaw")).append("', URI='").append(getValueFromField(doc, "URI")).append("', ").append(doc.getFieldNames().size()).append(" fields with ").append(valuesCount).append(" values \n");

		for (String fieldName : new TreeMap<>(doc.getFieldValuesMap()).keySet()) {
			sb.append(padding).append("Field: Name='").append(fieldName).append("', ").append(doc.getFieldValues(fieldName).size()).append(" values \n");

			for (Object value : doc.getFieldValues(fieldName)) {
				sb.append(padding).append("   '").append(String.valueOf(value))
						.append("' \n");
			}
		}

		return sb.toString();
	}

	public static String getValueFromField(SearchResultDocument doc,
			String fieldName) {
		Object value = doc.getFirstValue(fieldName);
		return (value == null) ? "UNKNOWN" : String.valueOf(value);
	}

}
