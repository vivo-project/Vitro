/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;

/**
 * This extends HttpServlet instead of VitroHttpServlet because we want to have
 * full control over the response:
 * <ul>
 * <li>No redirecting to the login page if not authorized</li>
 * <li>No redirecting to the home page on insufficient authorization</li>
 * <li>No support for GET or HEAD requests, only POST.</li>
 * </ul>
 * 
 * So these responses will be produced:
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
public class SparqlUpdateApiController extends HttpServlet {
	private static final Log log = LogFactory
			.getLog(SparqlUpdateApiController.class);

	private static final Actions REQUIRED_ACTIONS = SimplePermission.USE_SPARQL_UPDATE_API.ACTIONS;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			checkAuthorization(req);
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
	}

	private void checkAuthorization(HttpServletRequest req)
			throws AuthException {
		String email = req.getParameter("email");
		String password = req.getParameter("password");

		Authenticator auth = Authenticator.getInstance(req);
		UserAccount account = auth.getAccountForInternalAuth(email);
		if (!auth.isCurrentPassword(account, password)) {
			log.debug("Invalid: '" + email + "'/'" + password + "'");
			throw new AuthException("email/password combination is not valid");
		}

		if (!PolicyHelper.isAuthorizedForActions(req, email, password,
				REQUIRED_ACTIONS)) {
			log.debug("Not authorized: '" + email + "'");
			throw new AuthException("Account is not authorized");
		}

		log.debug("Authorized for '" + email + "'");
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
		ServletContext ctx = req.getSession().getServletContext();
		VitroRequest vreq = new VitroRequest(req);

		IndexBuilder.getBuilder(ctx).pause();
		try {
			Dataset ds = new RDFServiceDataset(vreq.getUnfilteredRDFService());
			GraphStore graphStore = GraphStoreFactory.create(ds);
			UpdateAction.execute(parsed, graphStore);
		} finally {
			IndexBuilder.getBuilder(ctx).unpause();
		}
	}

	private void do200response(HttpServletResponse resp) throws IOException {
		doResponse(resp, SC_OK, "SPARQL update accepted.");
	}

	private void do403response(HttpServletResponse resp, AuthException e)
			throws IOException {
		doResponse(resp, SC_FORBIDDEN, e.getMessage());
	}

	private void do400response(HttpServletResponse resp, ParseException e)
			throws IOException {
		if (e.getCause() == null) {
			doResponse(resp, SC_BAD_REQUEST, e.getMessage());
		} else {
			doResponse(resp, SC_BAD_REQUEST, e.getMessage(), e.getCause());
		}
	}

	private void do500response(HttpServletResponse resp, Exception e)
			throws IOException {
		doResponse(resp, SC_INTERNAL_SERVER_ERROR, "Unknown error", e);
	}

	private void doResponse(HttpServletResponse resp, int statusCode,
			String message) throws IOException {
		resp.setStatus(statusCode);
		PrintWriter writer = resp.getWriter();
		writer.println("<H1>" + statusCode + " " + message + "</H1>");
	}

	private void doResponse(HttpServletResponse resp, int statusCode,
			String message, Throwable e) throws IOException {
		resp.setStatus(statusCode);
		PrintWriter writer = resp.getWriter();
		writer.println("<H1>" + statusCode + " " + message + "</H1>");
		writer.println("<pre>");
		e.printStackTrace(writer);
		writer.println("</pre>");
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class AuthException extends Exception {
		private AuthException(String message) {
			super(message);
		}
	}

	private static class ParseException extends Exception {
		private ParseException(String message) {
			super(message);
		}

		private ParseException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
