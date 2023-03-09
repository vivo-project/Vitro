/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.api;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.apache.jena.riot.web.HttpNames.paramUsingGraphURI;
import static org.apache.jena.riot.web.HttpNames.paramUsingNamedGraphURI;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.modify.UsingList;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

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
@WebServlet(name = "SparqlUpdateApi", urlPatterns = {"/api/sparqlUpdate"})
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
		InputStream update;
		String reqtype = req.getContentType();
		UsingList usingList = processProtocol(req);

		// Support update via both POST body and an 'update' parameter 
		if (reqtype.equalsIgnoreCase("application/sparql-update")) {
			try {
				update = req.getInputStream();
			} catch (IOException e) {
				log.debug("Error parsing POST body.");
				throw new ParseException("Error parsing POST body.");
			}
		}
		else {
			String updateString = req.getParameter("update");
			if (StringUtils.isBlank(updateString)) {
				log.debug("No update parameter.");
				throw new ParseException("No 'update' parameter.");
			}
			if (!StringUtils.containsIgnoreCase(updateString, "GRAPH") && !StringUtils.containsIgnoreCase(updateString, "WITH")) {
				if (log.isDebugEnabled()) {
					log.debug("No GRAPH or WITH uri in '" + updateString + "'");
				}
				throw new ParseException("SPARQL update must specify a GRAPH ( or WITH) URI.");
			}
			try {
				update = org.apache.commons.io.IOUtils.toInputStream(updateString, "UTF-8");
			} catch (IOException e) {
				log.debug("Error parsing POST body.");
				throw new ParseException("Error parsing POST body.");
			}
		}

		try {
			return UpdateFactory.read(usingList, update);
		} catch (Exception e) {
			log.debug("Problem parsing", e);
			throw new ParseException("Failed to parse SPARQL update", e);
		}
	}
	

	private void executeUpdate(HttpServletRequest req, UpdateRequest parsed) {
		VitroRequest vreq = new VitroRequest(req);
		SearchIndexer indexer = ApplicationUtils.instance().getSearchIndexer();
		Dataset ds = new RDFServiceDataset(vreq.getUnfilteredRDFService());
        DatasetGraph dg = DatasetGraphFactory.createTxnMem();
	    try {
	        if(indexer != null) {
	            indexer.pause();
	        }
	        if(ds.supportsTransactions()) {
			    ds.begin(ReadWrite.WRITE);
	        }
            UpdateAction.execute(parsed, dg);
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

	/* 
	 * The method below and the 'createNode' helper were 
	 * adapted from the Fuseki source code 
	 */
    private UsingList processProtocol(HttpServletRequest request) {
        UsingList toReturn = new UsingList();

        String[] usingArgs = request.getParameterValues(paramUsingGraphURI);
        String[] usingNamedArgs = request.getParameterValues(paramUsingNamedGraphURI);
        if ( usingArgs == null && usingNamedArgs == null )
            return toReturn;
        if ( usingArgs == null )
            usingArgs = new String[0];
        if ( usingNamedArgs == null )
            usingNamedArgs = new String[0];
        // Impossible.
//        if ( usingArgs.length == 0 && usingNamedArgs.length == 0 )
//            return;

        for ( String nodeUri : usingArgs ) {
            toReturn.addUsing(createNode(nodeUri));
        }
        for ( String nodeUri : usingNamedArgs ) {
            toReturn.addUsingNamed(createNode(nodeUri));
        }

        return toReturn;
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

	private static Node createNode(String x) {
        try {
            return NodeFactory.createURI(x);
        } catch (Exception ex) {
			log.debug("SPARQL Update: bad IRI: "+x);
            return null;
        }

    }

}
