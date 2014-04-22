/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.searchengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocumentList;

/**
 * Parse this search response, creating a map of values for each document.
 * 
 * The search response field names in the document are replaced by json field
 * names in the parsed results, according to the fieldMap.
 */
public class SearchResultsParser {
	private static final Log log = LogFactory.getLog(SearchResultsParser.class);

	private final SearchResponse queryResponse;
	private final Map<String, String> fieldNameMapping;

	public SearchResultsParser(SearchResponse queryResponse, FieldMap fieldMap) {
		this.queryResponse = queryResponse;
		this.fieldNameMapping = fieldMap.map();
	}

	/**
	 * Parse the entire response into a list of maps.
	 */
	public List<Map<String, String>> parse() {
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();

		if (queryResponse == null) {
			log.debug("Query response for a search was null");
			return maps;
		}

		SearchResultDocumentList docs = queryResponse.getResults();
		if (docs == null) {
			log.debug("Docs for a search was null");
			return maps;
		}
		log.debug("Total number of hits = " + docs.getNumFound());

		for (SearchResultDocument doc : docs) {
			maps.add(parseSingleDocument(doc));
		}

		return maps;
	}

	/**
	 * Parse the response, accepting only those maps that are acceptable to the
	 * filter, until we reach the maximum desired number of results (or until we
	 * have parsed the entire response).
	 */
	public List<Map<String, String>> parseAndFilterResponse(
			SearchResponseFilter filter, int maxNumberOfResults) {
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();

		if (queryResponse == null) {
			log.debug("Query response for a search was null");
			return maps;
		}

		SearchResultDocumentList docs = queryResponse.getResults();
		if (docs == null) {
			log.debug("Docs for a search was null");
			return maps;
		}
		log.debug("Total number of hits = " + docs.getNumFound());

		for (SearchResultDocument doc : docs) {
			Map<String, String> map = parseSingleDocument(doc);
			if (filter.accept(map)) {
				maps.add(map);
			}
			if (maps.size() >= maxNumberOfResults) {
				break;
			}
		}

		return maps;
	}

	/**
	 * Create a map from this document, applying translation on the field names.
	 */
	private Map<String, String> parseSingleDocument(SearchResultDocument doc) {
		Map<String, String> result = new HashMap<String, String>();
		for (String searchResultFieldName : fieldNameMapping.keySet()) {
			String jsonFieldName = fieldNameMapping.get(searchResultFieldName);
			result.put(jsonFieldName,
					parseSingleValue(doc, searchResultFieldName));
		}

		return result;
	}

	/**
	 * Find a single value in the document
	 */
	private String parseSingleValue(SearchResultDocument doc, String key) {
		Object rawValue = doc.getFirstValue(key);

		if (rawValue == null) {
			return "";
		}
		if (rawValue instanceof String) {
			return (String) rawValue;
		}
		return String.valueOf(rawValue);
	}

}
