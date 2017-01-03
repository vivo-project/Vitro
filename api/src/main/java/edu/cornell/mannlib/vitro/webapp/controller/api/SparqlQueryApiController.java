/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.query.QueryParseException;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.InvalidQueryTypeException;
import edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery.SparqlQueryApiExecutor;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;

/**
 * Process SPARQL queries as an API.
 * 
 * Supports GET or POST requests. May produce these responses:
 * 
 * <pre>
 * 200 Success
 * 400 Failed to parse SPARQL query
 * 400 SPARQL query type is not SELECT, ASK, CONSTRUCT, or DESCRIBE.
 * 403 username/password combination is not valid
 * 403 Account is not authorized
 * 406 Accept header does not include any available result formats
 * 500 Unknown error
 * </pre>
 */
public class SparqlQueryApiController extends VitroApiServlet {

	private static final AuthorizationRequest REQUIRED_ACTIONS = SimplePermission.USE_SPARQL_QUERY_API.ACTION;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		RDFService rdfService = ModelAccess.on(getServletContext())
				.getRDFService();

		String acceptHeader = req.getHeader("Accept");
		String queryString = req.getParameter("query");
		try {
			confirmAuthorization(req, REQUIRED_ACTIONS);
			confirmQueryIsPresent(queryString);

			SparqlQueryApiExecutor core = SparqlQueryApiExecutor.instance(
					rdfService, queryString, acceptHeader);
			resp.setContentType(core.getMediaType());
			core.executeAndFormat(resp.getOutputStream());
		} catch (AuthException e) {
			sendShortResponse(SC_FORBIDDEN, e.getMessage(), resp);
		} catch (BadParameterException e) {
			sendShortResponse(SC_BAD_REQUEST, e.getMessage(), resp);
		} catch (InvalidQueryTypeException e) {
			sendShortResponse(SC_BAD_REQUEST,
					"Query type is not SELECT, ASK, CONSTRUCT, "
							+ "or DESCRIBE: '" + queryString + "'", resp);
		} catch (QueryParseException e) {
			sendShortResponse(SC_BAD_REQUEST, "Failed to parse query: '"
					+ queryString + "'", e, resp);
		} catch (NotAcceptableException | AcceptHeaderParsingException e) {
			sendShortResponse(SC_NOT_ACCEPTABLE,
					"The accept header does not include any "
							+ "available content type: " + e.getMessage(), resp);
		} catch (RDFServiceException e) {
			sendShortResponse(SC_INTERNAL_SERVER_ERROR,
					"Problem executing the query.", e, resp);
		} catch (Exception e) {
			sendShortResponse(SC_INTERNAL_SERVER_ERROR, "Unrecognized error.",
					e, resp);
		}

	}

	private void confirmQueryIsPresent(String queryString)
			throws BadParameterException {
		if (queryString == null) {
			throw new BadParameterException("Query string was not supplied.");
		}
		if (queryString.trim().isEmpty()) {
			throw new BadParameterException("Query string is empty.");
		}
	}

}
