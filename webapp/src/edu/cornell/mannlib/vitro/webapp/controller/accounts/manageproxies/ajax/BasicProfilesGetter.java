/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ajax;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.AbstractAjaxResponder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.utils.ImageUtil;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryUtils;

/**
 * Get the basic auto-complete info for the profile selection.
 */
public class BasicProfilesGetter extends AbstractAjaxResponder {
	private static final Log log = LogFactory.getLog(BasicProfilesGetter.class);

	private static final String PROPERTY_PROFILE_TYPES = "proxy.eligibleTypeList";
	private static final String PARAMETER_SEARCH_TERM = "term";

	private static final String QUERY_BASIC_PROFILES = "" //
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" //
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?uri ?label ?classLabel ?imageUrl \n" //
			+ "WHERE { \n" //
			+ "    %typesUnion% \n" //
			+ "    ?uri rdfs:label ?label ; \n" //
			+ "    FILTER (REGEX(str(?label), '^%term%', 'i')) \n" //
			+ "} \n" //
			+ "ORDER BY ASC(?label) \n" //
			+ "LIMIT 25 \n";

	private final String term;
	private final OntModel fullModel;
	private final String placeholderImageUrl;

	public BasicProfilesGetter(HttpServlet servlet, VitroRequest vreq,
			HttpServletResponse resp) {
		super(servlet, vreq, resp);
		fullModel = vreq.getJenaOntModel();

		term = getStringParameter(PARAMETER_SEARCH_TERM, "");

		placeholderImageUrl = UrlBuilder.getUrl(ImageUtil
				.getPlaceholderImagePathForType(VitroVocabulary.USERACCOUNT));
	}

	@Override
	public String prepareResponse() throws IOException, JSONException {
		log.debug("search term is '" + term + "'");
		if (term.isEmpty()) {
			return EMPTY_RESPONSE;
		} else {
			String cleanTerm = SparqlQueryUtils.escapeForRegex(term);
			String queryStr = QUERY_BASIC_PROFILES.replace("%typesUnion%",
					buildTypeClause()).replace("%term%", cleanTerm);

			JSONArray jsonArray = new SparqlQueryRunner<JSONArray>(fullModel,
					new BasicProfileInfoParser()).executeQuery(queryStr);

			String response = jsonArray.toString();
			log.debug(response);
			return response;
		}
	}

	private String buildTypeClause() {
		String typesString = ConfigurationProperties.getBean(vreq).getProperty(
				PROPERTY_PROFILE_TYPES, "http://www.w3.org/2002/07/owl#Thing");
		String[] types = typesString.split(",");

		String typeClause = "{ ?uri rdf:type <" + types[0].trim() + "> }";
		for (int i = 1; i < types.length; i++) {
			typeClause += " UNION { ?uri rdf:type <" + types[i].trim() + "> }";
		}
		
		return typeClause;
	}


	/** Parse a query row into a map of keys and values. */
	private static class BasicProfileInfoParser extends JsonArrayParser {
		@Override
		protected Map<String, String> parseSolutionRow(QuerySolution solution) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("uri", solution.getResource("uri").getURI());
			map.put("label", ifLiteralPresent(solution, "label", ""));
			map.put("classLabel", "");
			map.put("imageUrl", "");
			return map;
		}
	}

}
