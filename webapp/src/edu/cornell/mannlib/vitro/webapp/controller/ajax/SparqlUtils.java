/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.ajax;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;
import java.io.OutputStream;

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

/**
 * Handle an AJAX request for a SPARQL query. On entry, the "query" parameter
 * contains the query string.
 * 
 * The result is delivered in JSON format.
 */
public class SparqlUtils {
	private static final Log log = LogFactory
			.getLog(SparqlQueryAjaxController.class);

	public static final String RESPONSE_MIME_TYPE = "application/javascript";

	public static final String PARAMETER_MODEL = "model";
	public static final String OPTION_MODEL_FULL = "full";
	public static final String OPTION_MODEL_USER_ACCOUNTS = "userAccounts";


	public static Query createQuery(String queryParam) throws AjaxControllerException {
		Query query = QueryFactory.create(queryParam, Syntax.syntaxARQ);
		if (!query.isSelectType()) {
			throw new AjaxControllerException(SC_NOT_FOUND,
					"Only 'select' queries are allowed.");
		}
		return query;
	}

	public static void executeQuery(HttpServletResponse response, Query query,
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


	public static class AjaxControllerException extends Exception {
		private final int statusCode;

		public AjaxControllerException(int statusCode, String message) {
			super(message);
			this.statusCode = statusCode;
		}

		public int getStatusCode() {
			return statusCode;
		}
	}
}
