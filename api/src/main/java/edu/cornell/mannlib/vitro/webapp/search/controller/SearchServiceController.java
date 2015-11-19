/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.search.controller;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.api.NotAuthorizedToUseApiException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexer;

/**
 * Accepts requests to update a set of URIs in the search index.
 */
@SuppressWarnings("serial")
public class SearchServiceController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(SearchServiceController.class);

	/** Limit file size to 1 Gigabyte. */
	@Override
	public long maximumMultipartFileSize() {
		return 1024 * 1024 * 1024;
	}

	/**
	 * Handle the different actions. If not specified, the default action is to
	 * show the help page.
	 */
	@Override
	protected ResponseValues processRequest(VitroRequest req) {
		try {
			// Check the authorization here, because we don't want to redirect
			// to the login page if they are not authorized. (The file upload
			// would be lost.
			confirmAuthorization(req);

			switch (figureActionVerb(req)) {
			case UPDATE_URIS_IN_SEARCH:
				return doUpdateUrisInSearch(req);
			default:
				return doHelpForm();
			}
		} catch (NotAuthorizedToUseApiException e) {
			Map<String, Object> map = new HashMap<>();
			map.put("errorMessage", e.getMessage());
			return new TemplateResponseValues("error-message.ftl", map,
					SC_FORBIDDEN);
		} catch (Exception e) {
			return new ExceptionResponseValues(e, SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Process requests to the web service to update a list of URIs in the
	 * search index.
	 */
	private ResponseValues doUpdateUrisInSearch(HttpServletRequest req)
			throws IOException, ServletException {
		SearchIndexer indexer = ApplicationUtils.instance().getSearchIndexer();
		int uriCount = new UpdateUrisInIndex().doUpdateUris(req, indexer);

		Map<String, Object> body = new HashMap<>();
		body.put("msg", "Received " + uriCount + " URIs.");

		return new TemplateResponseValues("searchService-help.ftl", body);
	}

	private ResponseValues doHelpForm() {
		return new TemplateResponseValues("searchService-help.ftl");
	}

	private void confirmAuthorization(VitroRequest vreq)
			throws NotAuthorizedToUseApiException {
		Verb verb = figureActionVerb(vreq);
		String pw = vreq.getParameter("password");
		String email = vreq.getParameter("email");

		// If you just want the help screen, no problem.
		if (verb == Verb.VIEW_HELP_FORM) {
			return;
		}
		// For other functions, your credentials must have moxie.
		if (PolicyHelper.isAuthorizedForActions(vreq, email, pw,
				SimplePermission.MANAGE_SEARCH_INDEX.ACTION)) {
			return;
		}
		// Otherwise, you can't do this.
		throw new NotAuthorizedToUseApiException(email
				+ " is not authorized to manage the search index.");
	}

	private Verb figureActionVerb(VitroRequest vreq) {
		String pathInfo = vreq.getPathInfo();
		if (pathInfo == null) {
			pathInfo = "";
		}
		if (pathInfo.startsWith("/")) {
			pathInfo = pathInfo.substring(1);
		}
		return Verb.fromString(pathInfo);
	}

	// ----------------------------------------------------------------------
	// Helper class
	// ----------------------------------------------------------------------

	public enum Verb {
		VIEW_HELP_FORM("viewHelpForm"), UPDATE_URIS_IN_SEARCH(
				"updateUrisInSearch");

		public final String verb;

		Verb(String verb) {
			this.verb = verb;
		}

		static Verb fromString(String s) {
			for (Verb v : values()) {
				if (v.verb.equals(s)) {
					return v;
				}
			}
			return VIEW_HELP_FORM;
		}
	}
}
