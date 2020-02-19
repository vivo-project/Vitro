/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.ajax;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.ResultSetParser;

/**
 * A base class for AJAX responder objects, to be instantiated and invoked by
 * AJAX servlets.
 */
public abstract class AbstractAjaxResponder {
	private static final Log log = LogFactory
			.getLog(AbstractAjaxResponder.class);

	protected static final String EMPTY_RESPONSE = "[]";

	protected final HttpServlet servlet;
	protected final VitroRequest vreq;
	protected final HttpServletResponse resp;
	protected final IndividualDao indDao;
	protected final UserAccountsDao uaDao;

	public AbstractAjaxResponder(HttpServlet servlet, VitroRequest vreq,
			HttpServletResponse resp) {
		this.servlet = servlet;
		this.vreq = vreq;
		this.resp = resp;
		this.indDao = vreq.getWebappDaoFactory().getIndividualDao();
		this.uaDao = vreq.getWebappDaoFactory().getUserAccountsDao();
	}

	public final void processRequest() {
		try {
			resp.getWriter().write(prepareResponse());
		} catch (Exception e) {
			log.error("Problem with AJAX response", e);
		}
	}

	protected abstract String prepareResponse() throws IOException;

	protected String getStringParameter(String key, String defaultValue) {
		String value = vreq.getParameter(key);
		return (value == null) ? defaultValue : value;
	}

	protected Collection<String> getStringParameters(String key) {
		String[] values = vreq.getParameterValues(key);
		if (values == null) {
			return Collections.emptySet();
		} else {
			return new HashSet<String>(Arrays.asList(values));
		}
	}

	/**
	 * Assemble a list of maps into a single String representing a JSON array of
	 * objects with fields.
	 */
	protected String assembleJsonResponse(List<Map<String, String>> maps) {
		ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
		for (Map<String, String> map : maps) {
			ObjectNode jsonObj = JsonNodeFactory.instance.objectNode();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				jsonObj.put(entry.getKey(), entry.getValue());
			}
			jsonArray.add(jsonObj);
		}
		return jsonArray.toString();
	}

	/**
	 * AJAX responders can use a parser that extends this class. The parser must
	 * implement "parseSolutionRow()"
	 */
	protected abstract static class JsonArrayParser extends
			ResultSetParser<ArrayNode> {
		@Override
		protected ArrayNode defaultValue() {
			return JsonNodeFactory.instance.arrayNode();
		}

		@Override
		protected ArrayNode parseResults(String queryStr, ResultSet results) {
			ArrayNode jsonArray = JsonNodeFactory.instance.arrayNode();
			while (results.hasNext()) {
				Map<String, String> map = parseSolutionRow(results.next());
				if (map != null) {
					ObjectNode jsonObj = JsonNodeFactory.instance.objectNode();
					for (Map.Entry<String, String> entry : map.entrySet()) {
						jsonObj.put(entry.getKey(), entry.getValue());
					}
					jsonArray.add(jsonObj);
				}
			}
			return jsonArray;
		}

		/**
		 * Subclasses must implement. Return a map of field names and values,
		 * which will become a row in the result.
		 *
		 * Or return null, and no row will be created in the result.
		 */
		protected abstract Map<String, String> parseSolutionRow(
				QuerySolution solution);
	}

}
