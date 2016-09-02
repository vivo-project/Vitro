/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.ajax;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.USER_ACCOUNTS;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.SparqlUtils.AjaxControllerException;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

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
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		String modelParam = getModelParam(vreq);
		if (OPTION_MODEL_USER_ACCOUNTS.equals(modelParam)) {
			return SimplePermission.QUERY_USER_ACCOUNTS_MODEL.ACTION;
		} else {
			return SimplePermission.QUERY_FULL_MODEL.ACTION;
		}
	}

	@Override
	protected void doRequest(VitroRequest vreq, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String modelParam = getModelParam(vreq);
			Model model = locateModel(vreq, modelParam);
			String queryParam = locateQueryParam(vreq);
			Query query = SparqlUtils.createQuery(queryParam);
			SparqlUtils.executeQuery(response, query, model);
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

	private Model locateModel(VitroRequest vreq, String modelParam) {
		if (OPTION_MODEL_USER_ACCOUNTS.equals(modelParam)) {
			return ModelAccess.on(vreq).getOntModel(USER_ACCOUNTS);
		} else {
			return ModelAccess.on(vreq).getOntModel();
		}
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
	
}
