/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.ajax;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Handle an AJAX request for a SPARQL query. On entry, the "query" parameter
 * contains the query string.
 * 
 * The result is delivered in JSON format.
 */
public class SparqlQueryAjaxController extends VitroAjaxController {
	private static final Log log = LogFactory
			.getLog(SparqlQueryAjaxController.class);

	private static final String PARAMETER_QUERY = "query";
	private static final String RESPONSE_MIME_TYPE = "application/javascript";

	/**
	 * If you are logged in, you can use this servlet.
	 */
	@Override
	protected boolean testIsAuthorized(HttpServletRequest request) {
		return LoginStatusBean.getBean(request).isLoggedIn();
	}

	@Override
	protected void doRequest(VitroRequest vreq, HttpServletResponse response)
			throws ServletException, IOException {

		Model model = vreq.getJenaOntModel();
		if (model == null) {
			log.error("JenaOntModel not found.");
			response.sendError(SC_INTERNAL_SERVER_ERROR,
					"JenaOntModel not found");
			return;
		}

		String queryParam = vreq.getParameter(PARAMETER_QUERY);
		log.debug("queryParam was : " + queryParam);
		if ((queryParam == null) || queryParam.isEmpty()) {
			response.sendError(SC_NOT_FOUND, "'" + PARAMETER_QUERY
					+ "' parameter is required");
		}

		Query query = QueryFactory.create(queryParam, Syntax.syntaxARQ);
		if (!query.isSelectType()) {
			log.debug("Not a 'select' query.");
			response.sendError(SC_NOT_FOUND,
					"Only 'select' queries are allowed.");
		}

		executeQuery(response, query, DatasetFactory.create(model));
		return;
	}

	private void executeQuery(HttpServletResponse response, Query query,
			Dataset dataset) throws IOException {
		QueryExecution qe = QueryExecutionFactory.create(query, dataset);
		try {
			ResultSet results = qe.execSelect();
			response.setContentType(RESPONSE_MIME_TYPE);
			OutputStream out = response.getOutputStream();
			ResultSetFormatter.outputAsJSON(out, results);
		} finally {
			qe.close();
		}
	}

}
