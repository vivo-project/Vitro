/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;

/**
 * Process SPARQL Updates, as an API.
 * 
 * Supports only POST requests, not GET or HEAD. May produce these responses:
 * 
 * <pre>
 * 200 Success
 * 400 Failed to parse SPARQL update
 * 400 SPARQL update must specify a GRAPH URI.
 * 403 username/password combination is not valid
 * 403 Account is not authorized
 * 405 Method not allowed
 * 500 Unknown error
 * </pre>
 */
public class SparqlUpdateApiController extends VitroApiServlet {
	private static final Log log = LogFactory
			.getLog(SparqlUpdateApiController.class);

	private static final AuthorizationRequest REQUIRED_ACTIONS = SimplePermission.USE_SPARQL_UPDATE_API.ACTION;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.debug("Starting update");
		try {
			confirmAuthorization(req, REQUIRED_ACTIONS);
			UpdateRequest parsed = parseUpdateString(req);
			executeUpdate(req, parsed);
			do200response(resp);
		} catch (AuthException e) {
			do403response(resp, e);
		} catch (ParseException e) {
			do400response(resp, e);
		} catch (Exception e) {
			do500response(resp, e);
		}
		log.debug("Update complete");
	}

	private UpdateRequest parseUpdateString(HttpServletRequest req)
			throws ParseException {
		String update = req.getParameter("update");
		if (StringUtils.isBlank(update)) {
			log.debug("No update parameter.");
			throw new ParseException("No 'update' parameter.");
		}

		if (!StringUtils.containsIgnoreCase(update, "GRAPH")) {
			if (log.isDebugEnabled()) {
				log.debug("No GRAPH uri in '" + update + "'");
			}
			throw new ParseException("SPARQL update must specify a GRAPH URI.");
		}

		try {
			return UpdateFactory.create(update);
		} catch (Exception e) {
			log.debug("Problem parsing", e);
			throw new ParseException("Failed to parse SPARQL update", e);
		}
	}

	private void executeUpdate(HttpServletRequest req, UpdateRequest parsed) {
		VitroRequest vreq = new VitroRequest(req);
		SearchIndexer indexer = ApplicationUtils.instance().getSearchIndexer();
		Dataset ds = new RDFServiceDataset(vreq.getUnfilteredRDFService());
		GraphStore graphStore = GraphStoreFactory.create(ds);
	    try {
	        if(indexer != null) {
	            indexer.pause();
	        }
	        if(ds.supportsTransactions()) {
			    ds.begin(ReadWrite.WRITE);
	        }
			UpdateAction.execute(parsed, graphStore);
		} finally {
		    if(ds.supportsTransactions()) {
                ds.commit();
                ds.end();
		    }
			if(indexer != null) {
			    indexer.unpause();
			}
		}
	}

	private void do200response(HttpServletResponse resp) throws IOException {
		sendShortResponse(SC_OK, "SPARQL update accepted.", resp);
	}

	private void do403response(HttpServletResponse resp, AuthException e)
			throws IOException {
		sendShortResponse( SC_FORBIDDEN, e.getMessage(), resp);
	}

	private void do400response(HttpServletResponse resp, ParseException e)
			throws IOException {
		if (e.getCause() == null) {
			sendShortResponse( SC_BAD_REQUEST, e.getMessage(), resp);
		} else {
			sendShortResponse( SC_BAD_REQUEST, e.getMessage(), e.getCause(), resp);
		}
	}

	private void do500response(HttpServletResponse resp, Exception e)
			throws IOException {
		sendShortResponse(SC_INTERNAL_SERVER_ERROR, "Unknown error", e, resp);
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class ParseException extends Exception {
		private ParseException(String message) {
			super(message);
		}

		private ParseException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
