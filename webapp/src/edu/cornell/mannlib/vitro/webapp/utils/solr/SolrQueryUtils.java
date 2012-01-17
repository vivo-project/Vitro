/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.solr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.response.QueryResponse;

/**
 * Some static method to help in constructing Solr queries and parsing the
 * results.
 */
public class SolrQueryUtils {
	public enum Conjunction {
		AND, OR;

		public String joiner() {
			return " " + this.name() + " ";
		}
	}

	/**
	 * Create an AutoCompleteWords object that can be used to build an
	 * auto-complete query.
	 */
	public static AutoCompleteWords parseForAutoComplete(String searchTerm,
			String delimiterPattern) {
		return new AutoCompleteWords(searchTerm, delimiterPattern);
	}

	/**
	 * Create a builder object that can assemble a map of Solr field names to
	 * JSON field names.
	 */
	public static FieldMap fieldMap() {
		return new FieldMap();
	}

	/**
	 * Parse a response into a list of maps, one map for each document.
	 * 
	 * The Solr field names in the document are replaced by json field names in
	 * the result, according to the fieldMap.
	 */
	public static List<Map<String, String>> parseResponse(
			QueryResponse queryResponse, FieldMap fieldMap) {
		return new SolrResultsParser(queryResponse, fieldMap).parse();
	}

	/**
	 * Parse a response into a list of maps, accepting only those maps that pass
	 * a filter, and only up to a maximum number of records.
	 * 
	 * The Solr field names in the document are replaced by json field names in
	 * the result, according to the fieldMap.
	 */
	public static List<Map<String, String>> parseAndFilterResponse(
			QueryResponse queryResponse, FieldMap fieldMap,
			SolrResponseFilter filter, int maxNumberOfResults) {
		return new SolrResultsParser(queryResponse, fieldMap)
				.parseAndFilterResponse(filter, maxNumberOfResults);
	}

	/**
	 * Break a string into a list of words, according to a RegEx delimiter. Trim
	 * leading and trailing white space from each word.
	 */
	public static List<String> parseWords(String typesString,
			String wordDelimiter) {
		List<String> list = new ArrayList<String>();
		String[] array = typesString.split(wordDelimiter);
		for (String word : array) {
			String trimmed = word.trim();
			if (!trimmed.isEmpty()) {
				list.add(trimmed);
			}
		}
		return list;
	}

	/**
	 * Glue these words together into a query on a given field, joined by either
	 * AND or OR.
	 */
	public static String assembleConjunctiveQuery(String fieldName,
			Collection<String> words, Conjunction c) {
		List<String> terms = new ArrayList<String>();
		for (String word : words) {
			terms.add(buildTerm(fieldName, word));
		}
		String q = StringUtils.join(terms, c.joiner());
		return q;
	}

	private static String buildTerm(String fieldName, String word) {
		return fieldName + ":\"" + word + "\"";
	}

}
