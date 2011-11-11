/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies.ajax;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QuerySolution;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.AbstractAjaxResponder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.utils.ImageUtil;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryRunner;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryUtils;

/**
 * Get the basic auto-complete info for the proxy selection.
 */
public class BasicProxiesGetter extends AbstractAjaxResponder {
	private static final Log log = LogFactory.getLog(BasicProxiesGetter.class);

	private static final String PARAMETER_SEARCH_TERM = "term";

	private static final String QUERY_BASIC_PROXIES = "" //
			+ "PREFIX fn: <http://www.w3.org/2005/xpath-functions#> \n" //
			+ "PREFIX auth: <http://vitro.mannlib.cornell.edu/ns/vitro/authorization#> \n" //
			+ "\n" //
			+ "SELECT DISTINCT ?uri ?label \n" //
			+ "WHERE { \n" //
			+ "    ?uri a auth:UserAccount ; \n" //
			+ "            auth:firstName ?firstName ; \n" //
			+ "            auth:lastName ?lastName . \n" //
			+ "    LET ( ?label := fn:concat(?lastName, ', ', ?firstName) )" //
			+ "    FILTER (REGEX(?label, '^%term%', 'i')) \n" //
			+ "} \n" //
			+ "ORDER BY ASC(?lastName) ASC(?firstName) \n" //
			+ "LIMIT 25 \n"; //

	private final String term;
	private final OntModel userAccountsModel;
	private final String placeholderImageUrl;

	public BasicProxiesGetter(HttpServlet servlet, VitroRequest vreq,
			HttpServletResponse resp) {
		super(servlet, vreq, resp);
		term = getStringParameter(PARAMETER_SEARCH_TERM, "");

		ServletContext ctx = vreq.getSession().getServletContext();
		OntModelSelector oms = ModelContext.getUnionOntModelSelector(ctx);
		userAccountsModel = oms.getUserAccountsModel();

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
			String queryStr = QUERY_BASIC_PROXIES.replace("%term%", cleanTerm);

			JSONArray jsonArray = new SparqlQueryRunner<JSONArray>(
					userAccountsModel, new BasicProxyInfoParser(
							placeholderImageUrl)).executeQuery(queryStr);

			String response = jsonArray.toString();
			log.debug(response);
			return response;
		}
	}

	/** Parse a query row into a map of keys and values. */
	private static class BasicProxyInfoParser extends JsonArrayParser {
		private final String placeholderImageUrl;

		public BasicProxyInfoParser(String placeholderImageUrl) {
			this.placeholderImageUrl = placeholderImageUrl;
		}

		@Override
		protected Map<String, String> parseSolutionRow(QuerySolution solution) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("uri", solution.getResource("uri").getURI());
			map.put("label", ifLiteralPresent(solution, "label", ""));
			map.put("classLabel", "");
			map.put("imageUrl", placeholderImageUrl);
			return map;
		}
	}

}
