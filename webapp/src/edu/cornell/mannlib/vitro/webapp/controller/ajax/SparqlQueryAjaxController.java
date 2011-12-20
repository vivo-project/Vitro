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

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.querymodel.QueryFullModel;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.querymodel.QueryUserAccountsModel;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;

/**
 * Handle an AJAX request for a SPARQL query. On entry, the "query" parameter
 * contains the query string.
 * 
 * The result is delivered in JSON format.
 */
public class SparqlQueryAjaxController extends VitroAjaxController {
	private static final Log log = LogFactory
			.getLog(SparqlQueryAjaxController.class);

	public static final String PARAMETER_QUERY = "query";
	public static final String RESPONSE_MIME_TYPE = "application/javascript";

	public static final String PARAMETER_MODEL = "model";
	public static final String OPTION_MODEL_FULL = "full";
	public static final String OPTION_MODEL_USER_ACCOUNTS = "userAccounts";

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		String modelParam = getModelParam(vreq);
		if (OPTION_MODEL_USER_ACCOUNTS.equals(modelParam)) {
			return new Actions(new QueryUserAccountsModel());
		} else {
			return new Actions(new QueryFullModel());
		}
	}

	@Override
	protected void doRequest(VitroRequest vreq, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String modelParam = getModelParam(vreq);
			Model model = locateModel(vreq, modelParam);
			String queryParam = locateQueryParam(vreq);
			Query query = createQuery(queryParam);
			executeQuery(response, query, model);
			return;
		} catch (AjaxControllerException e) {
			log.error(e.getMessage());
			response.sendError(e.getStatusCode());
		}
	}

	private String getModelParam(HttpServletRequest req) {
		String modelParam = req.getParameter(PARAMETER_MODEL);
		log.debug("modelParam was: " + modelParam);
		if ((modelParam != null) && (!modelParam.isEmpty())) {
			return modelParam;
		} else {
			return OPTION_MODEL_FULL;
		}

	}

	private Model locateModel(VitroRequest vreq, String modelParam)
			throws AjaxControllerException {
		Object o = getServletContext().getAttribute("baseOntModelSelector");
		if (!(o instanceof OntModelSelector)) {
			throw new AjaxControllerException(SC_INTERNAL_SERVER_ERROR,
					"OntModelSelector not found");
		}
		OntModelSelector oms = (OntModelSelector) o;

		Model model = null;
		if (OPTION_MODEL_USER_ACCOUNTS.equals(modelParam)) {
			model = oms.getUserAccountsModel();
		} else {
			// TODO What is the appropriate way to do this?
			// model = oms.getFullModel();
			model = vreq.getJenaOntModel();
		}
		if (model == null) {
			throw new AjaxControllerException(SC_INTERNAL_SERVER_ERROR,
					"Model '' not found.");
		}

		return model;
	}

	private String locateQueryParam(VitroRequest vreq)
			throws AjaxControllerException {
		String queryParam = vreq.getParameter(PARAMETER_QUERY);
		log.debug("queryParam was: " + queryParam);
		if ((queryParam != null) && (!queryParam.isEmpty())) {
			return queryParam;
		} else {
			throw new AjaxControllerException(SC_NOT_FOUND, "'"
					+ PARAMETER_QUERY + "' parameter is required");
		}
	}

	private Query createQuery(String queryParam) throws AjaxControllerException {
		Query query = QueryFactory.create(queryParam, Syntax.syntaxARQ);
		if (!query.isSelectType()) {
			throw new AjaxControllerException(SC_NOT_FOUND,
					"Only 'select' queries are allowed.");
		}
		return query;
	}

	private void executeQuery(HttpServletResponse response, Query query,
			Model model) throws IOException {
		Dataset dataset = DatasetFactory.create(model);
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

	private static class AjaxControllerException extends Exception {
		private final int statusCode;

		AjaxControllerException(int statusCode, String message) {
			super(message);
			this.statusCode = statusCode;
		}

		public int getStatusCode() {
			return statusCode;
		}
	}
}
