/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;

import edu.cornell.mannlib.vitro.webapp.beans.SelfEditingConfiguration;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.AbstractAjaxResponder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;

/**
 * Get a list of Profiles with last names that begin with this search term, and
 * that have no matching property, unless it matches the current externalAuthId.
 * (So a Profile that is currently associated with the user is not excluded from
 * the list.)
 * 
 * For each such Profile, return the label, the URL and the URI.
 * 
 * If the matching property is not defined, or if the search term is empty, or
 * if an error occurs, return an empty result.
 */
class ProfileAutoCompleter extends AbstractAjaxResponder {
	private static final Log log = LogFactory
			.getLog(ProfileAutoCompleter.class);

	private static final String PARAMETER_SEARCH_TERM = "term";
	private static final String PARAMETER_ETERNAL_AUTH_ID = "externalAuthId";

	private static final Syntax SYNTAX = Syntax.syntaxARQ;

	private static final String QUERY_TEMPLATE = "" //
			+ "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" + "\n" //
			+ "SELECT DISTINCT ?uri ?fn ?ln \n" //
			+ "WHERE {\n" //
			+ "    ?uri rdf:type foaf:Person ; \n" //
			+ "         foaf:firstName ?fn ; \n" //
			+ "         foaf:lastName ?ln . \n" //
			+ "    OPTIONAL { ?uri <%matchingPropertyUri%> ?id} \n" //
			+ "    FILTER ( !bound(?id) || (?id = '%externalAuthId%') ) \n" //
			+ "    FILTER ( REGEX(?ln, '%searchTerm%', 'i') ) \n" //
			+ "} \n" //
			+ "ORDER BY ?ln ?fn \n" //
			+ "LIMIT 20 \n";

	private final String term;
	private final String externalAuthId;
	private final String selfEditingIdMatchingProperty;
	private final OntModel fullModel;

	public ProfileAutoCompleter(HttpServlet parent, VitroRequest vreq,
			HttpServletResponse resp) {
		super(parent, vreq, resp);
		term = getStringParameter(PARAMETER_SEARCH_TERM, "");
		externalAuthId = getStringParameter(PARAMETER_ETERNAL_AUTH_ID, "");

		// TODO This seems to expose the matching property and mechanism too
		// much. Can this be done within SelfEditingConfiguration somehow?
		selfEditingIdMatchingProperty = SelfEditingConfiguration.getBean(vreq)
				.getMatchingPropertyUri();

		fullModel = vreq.getJenaOntModel();
	}

	@Override
	public String prepareResponse() throws IOException, JSONException {
		if (term.isEmpty()) {
			return EMPTY_RESPONSE;
		}
		if (selfEditingIdMatchingProperty == null) {
			return EMPTY_RESPONSE;
		}
		if (selfEditingIdMatchingProperty.isEmpty()) {
			return EMPTY_RESPONSE;
		}
		return doSparqlQueryAndParseResult();
	}

	private String doSparqlQueryAndParseResult() throws JSONException {
		String queryStr = prepareQueryString();

		QueryExecution qe = null;
		List<ProfileInfo> results;
		try {
			Query query = QueryFactory.create(queryStr, SYNTAX);
			qe = QueryExecutionFactory.create(query, fullModel);
			results = parseResults(qe.execSelect());
		} catch (Exception e) {
			log.error("Failed to execute the query: " + queryStr, e);
			results = Collections.emptyList();
		} finally {
			if (qe != null) {
				qe.close();
			}
		}

		JSONArray jsonArray = prepareJsonArray(results);
		return jsonArray.toString();
	}

	private String prepareQueryString() {
		String queryString = QUERY_TEMPLATE
				.replace("%matchingPropertyUri%", selfEditingIdMatchingProperty)
				.replace("%searchTerm%", term)
				.replace("%externalAuthId%", externalAuthId);
		log.debug("Query string is '" + queryString + "'");
		return queryString;
	}

	private List<ProfileInfo> parseResults(ResultSet results) {
		List<ProfileInfo> profiles = new ArrayList<ProfileInfo>();
		while (results.hasNext()) {
			QuerySolution solution = results.next();
			ProfileInfo pi = parseSolution(solution);
			profiles.add(pi);
		}
		log.debug("Results are: " + profiles);
		return profiles;
	}

	private ProfileInfo parseSolution(QuerySolution solution) {
		String uri = solution.getResource("uri").getURI();
		String url = UrlBuilder.getIndividualProfileUrl(uri, vreq);

		String firstName = solution.getLiteral("fn").getString();
		String lastName = solution.getLiteral("ln").getString();
		String label = lastName + ", " + firstName;

		return new ProfileInfo(uri, url, label);
	}

	private JSONArray prepareJsonArray(List<ProfileInfo> results)
			throws JSONException {
		JSONArray jsonArray = new JSONArray();

		for (int i = 0; i < results.size(); i++) {
			ProfileInfo profile = results.get(i);

			Map<String, String> map = new HashMap<String, String>();
			map.put("label", profile.label);
			map.put("uri", profile.uri);
			map.put("url", profile.url);

			jsonArray.put(i, map);
		}

		return jsonArray;
	}

	private static class ProfileInfo {
		final String uri;
		final String url;
		final String label;

		public ProfileInfo(String uri, String url, String label) {
			this.uri = uri;
			this.url = url;
			this.label = label;
		}

		@Override
		public String toString() {
			return "ProfileInfo[label=" + label + ", uri=" + uri + ", url="
					+ url + "]";
		}
	}

}