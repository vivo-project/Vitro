/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONException;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.AbstractAjaxResponder;
import edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames;
import edu.cornell.mannlib.vitro.webapp.search.solr.SolrSetup;

/**
 * Get the basic auto-complete info for the profile selection.
 * 
 */
public class BasicProfilesGetter extends AbstractAjaxResponder {
	private static final Log log = LogFactory.getLog(BasicProfilesGetter.class);

	private static final String PROPERTY_PROFILE_TYPES = "proxy.eligibleTypeList";
	private static final String PARAMETER_SEARCH_TERM = "term";
	private static final String DEFAULT_PROFILE_TYPES = "http://www.w3.org/2002/07/owl#Thing";

	private final String term;
	private final List<String> completeWords;
	private final String partialWord;
	private final Collection<String> profileTypes;

	public BasicProfilesGetter(HttpServlet servlet, VitroRequest vreq,
			HttpServletResponse resp) {
		super(servlet, vreq, resp);

		this.term = getStringParameter(PARAMETER_SEARCH_TERM, "");

		List<String> termWords = figureTermWords();
		if (termWords.isEmpty() || this.term.endsWith(" ")) {
			this.completeWords = termWords;
			this.partialWord = null;
		} else {
			this.completeWords = termWords.subList(0, termWords.size() - 1);
			this.partialWord = termWords.get(termWords.size() - 1);
		}

		this.profileTypes = figureProfileTypes();

		log.debug(this);
	}

	private List<String> figureTermWords() {
		List<String> list = new ArrayList<String>();
		String[] array = this.term.split("[, ]+");
		for (String word : array) {
			String trimmed = word.trim();
			if (!trimmed.isEmpty()) {
				list.add(trimmed);
			}
		}
		return Collections.unmodifiableList(list);
	}

	private Collection<String> figureProfileTypes() {
		List<String> list = new ArrayList<String>();
		String typesString = ConfigurationProperties.getBean(vreq).getProperty(
				PROPERTY_PROFILE_TYPES, DEFAULT_PROFILE_TYPES);
		String[] types = typesString.split(",");
		for (String type : types) {
			String trimmed = type.trim();
			if (!trimmed.isEmpty()) {
				list.add(trimmed);
			}
		}
		if (list.isEmpty()) {
			log.error("No types configured for profile pages in "
					+ PROPERTY_PROFILE_TYPES);
		}
		return Collections.unmodifiableCollection(list);
	}

	@Override
	public String prepareResponse() throws IOException, JSONException {
		log.debug("search term is '" + term + "'");
		if (this.term.isEmpty() || this.profileTypes.isEmpty()) {
			return EMPTY_RESPONSE;
		}

		try {
			SolrServer solr = SolrSetup.getSolrServer(servlet
					.getServletContext());
			SolrQuery query = buildSolrQuery();
			QueryResponse queryResponse = solr.query(query);

			JSONArray jsonArray = parseResponse(queryResponse);
			String response = jsonArray.toString();
			log.debug(response);
			return response;
		} catch (SolrServerException e) {
			log.error("Failed to get basic profile info", e);
			return EMPTY_RESPONSE;
		}
	}

	private SolrQuery buildSolrQuery() {
		SolrQuery q = new SolrQuery();
		q.setFields(VitroSearchTermNames.NAME_RAW, VitroSearchTermNames.URI);
		q.setSortField(VitroSearchTermNames.NAME_LOWERCASE_SINGLE_VALUED,
				ORDER.asc);
		q.setFilterQueries(assembleTypeRestrictionQuery());
		q.setStart(0);
		q.setRows(30);
		q.setQuery(buildQueryStringFromSearchTerm());
		return q;
		// use VitroSearchTermNames.NAME_LOWERCASE
		// break the search term into words, then insert AND between the words
		// TODO Auto-generated method stub
	}

	private String assembleTypeRestrictionQuery() {
		List<String> terms = new ArrayList<String>();
		for (String profileType : profileTypes) {
			terms.add(VitroSearchTermNames.RDFTYPE + ":\"" + profileType + "\"");
		}
		String q = StringUtils.join(terms, " OR ");
		log.debug("Type restriction query is '" + q + "'");
		return q;
	}

	private String buildQueryStringFromSearchTerm() {
		List<String> terms = new ArrayList<String>();
		for (String word : completeWords) {
			terms.add(termForCompleteWord(word));
		}
		if (partialWord != null) {
			terms.add(termForPartialWord(partialWord));
		}

		String q = StringUtils.join(terms, " AND ");
		log.debug("Query string is '" + q + "'");
		return q;
	}

	private String termForCompleteWord(String word) {
		return VitroSearchTermNames.NAME_UNSTEMMED + ":\"" + word + "\"";
	}

	private String termForPartialWord(String word) {
		return VitroSearchTermNames.AC_NAME_STEMMED + ":\"" + word + "\"";
	}

	private JSONArray parseResponse(QueryResponse queryResponse) {
		JSONArray jsonArray = new JSONArray();

		if (queryResponse == null) {
			log.error("Query response for a search was null");
			return jsonArray;
		}

		SolrDocumentList docs = queryResponse.getResults();

		if (docs == null) {
			log.error("Docs for a search was null");
			return jsonArray;
		}

		long hitCount = docs.getNumFound();
		log.debug("Total number of hits = " + hitCount);
		if (hitCount < 1) {
			return jsonArray;
		}

		for (SolrDocument doc : docs) {
			try {
				String uri = doc.get(VitroSearchTermNames.URI).toString();

				Object nameRaw = doc.get(VitroSearchTermNames.NAME_RAW);
				String name = null;
				if (nameRaw instanceof List<?>) {
					@SuppressWarnings("unchecked")
					List<String> nameRawList = (List<String>) nameRaw;
					name = nameRawList.get(0);
				} else {
					name = (String) nameRaw;
				}

				jsonArray.put(resultRow(uri, name));
			} catch (Exception e) {
				log.error("problem getting usable individuals from search "
						+ "hits" + e.getMessage());
			}
		}

		return jsonArray;
	}

	private Map<String, String> resultRow(String uri, String name) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("uri", uri);
		map.put("label", name);
		map.put("classLabel", "");
		map.put("imageUrl", "");
		return map;
	}

	@Override
	public String toString() {
		return "BasicProfilesGetter[term=" + term + ", completeWords="
				+ completeWords + ", partialWord=" + partialWord
				+ ", profileTypes=" + profileTypes + "]";
	}

}
