/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ajax;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.AC_NAME_STEMMED;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_LOWERCASE_SINGLE_VALUED;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_RAW;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_UNSTEMMED;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.RDFTYPE;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.URI;
import static edu.cornell.mannlib.vitro.webapp.utils.searchengine.SearchQueryUtils.Conjunction.OR;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.AbstractAjaxResponder;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.AutoCompleteWords;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.FieldMap;
import edu.cornell.mannlib.vitro.webapp.utils.searchengine.SearchQueryUtils;

/**
 * Get the basic auto-complete info for the profile selection.
 * 
 */
public class BasicProfilesGetter extends AbstractAjaxResponder {
	private static final Log log = LogFactory.getLog(BasicProfilesGetter.class);

	private static final String WORD_DELIMITER = "[, ]+";
	private static final FieldMap RESPONSE_FIELDS = SearchQueryUtils
			.fieldMap().put(URI, "uri").put(NAME_RAW, "label")
			.put("bogus", "classLabel").put("bogus", "imageUrl");

	private static final String PROPERTY_PROFILE_TYPES = "proxy.eligibleTypeList";
	private static final String PARAMETER_SEARCH_TERM = "term";
	private static final String DEFAULT_PROFILE_TYPES = "http://www.w3.org/2002/07/owl#Thing";

	private final String term;
	private final AutoCompleteWords searchWords;
	private final List<String> profileTypes;

	public BasicProfilesGetter(HttpServlet servlet, VitroRequest vreq,
			HttpServletResponse resp) {
		super(servlet, vreq, resp);

		this.term = getStringParameter(PARAMETER_SEARCH_TERM, "");
		this.searchWords = SearchQueryUtils.parseForAutoComplete(term,
				WORD_DELIMITER);

		this.profileTypes = figureProfileTypes();

		log.debug(this);
	}

	private List<String> figureProfileTypes() {
		String typesString = ConfigurationProperties.getBean(vreq).getProperty(
				PROPERTY_PROFILE_TYPES, DEFAULT_PROFILE_TYPES);
		List<String> list = SearchQueryUtils.parseWords(typesString,
				WORD_DELIMITER);
		if (list.isEmpty()) {
			log.error("No types configured for profile pages in "
					+ PROPERTY_PROFILE_TYPES);
		}
		return list;
	}

	@Override
	public String prepareResponse() throws IOException, JSONException {
		log.debug("search term is '" + term + "'");
		if (this.term.isEmpty() || this.profileTypes.isEmpty()) {
			return EMPTY_RESPONSE;
		}

		try {
			SearchEngine search = ApplicationUtils.instance().getSearchEngine();
			SearchQuery query = buildSearchQuery();
			SearchResponse queryResponse = search.query(query);

			List<Map<String, String>> parsed = SearchQueryUtils
					.parseResponse(queryResponse, RESPONSE_FIELDS);

			String response = assembleJsonResponse(parsed);
			log.debug(response);
			return response;
		} catch (SearchEngineException e) {
			log.error("Failed to get basic profile info", e);
			return EMPTY_RESPONSE;
		}
	}

	private SearchQuery buildSearchQuery() {
		SearchQuery q = ApplicationUtils.instance().getSearchEngine().createQuery();
		q.addFields(NAME_RAW, URI);
		q.addSortField(NAME_LOWERCASE_SINGLE_VALUED, Order.ASC);
		q.addFilterQuery(SearchQueryUtils.assembleConjunctiveQuery(RDFTYPE, profileTypes, OR));
		q.setStart(0);
		q.setRows(30);
		q.setQuery(searchWords.assembleQuery(NAME_UNSTEMMED, AC_NAME_STEMMED));
		return q;
	}

	@Override
	public String toString() {
		return "BasicProfilesGetter[term=" + term + ", searchWords="
				+ searchWords + ", profileTypes=" + profileTypes + "]";
	}

}
