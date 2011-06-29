/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageUserAccounts;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;

/**
 * Handle the AJAX functions that are specific to the UserAccounts pages.
 */
public class UserAccountsAjaxController extends VitroAjaxController {
	private static final String PARAMETER_FUNCTION = "function";

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return new Actions(new ManageUserAccounts());
	}

	@Override
	protected void doRequest(VitroRequest vreq, HttpServletResponse resp)
			throws ServletException, IOException {
		String function = vreq.getParameter(PARAMETER_FUNCTION);
		if ("checkExternalAuth".equals(function)) {
			new ExternalAuthChecker(this, vreq, resp).processRequest();
		} else {
			new ErrorResponder(this, vreq, resp).processRequest();
		}
	}

	static abstract class AjaxResponder {
		protected final HttpServlet parent;
		protected final VitroRequest vreq;
		protected final HttpServletResponse resp;

		public AjaxResponder(HttpServlet parent, VitroRequest vreq,
				HttpServletResponse resp) {
			this.parent = parent;
			this.vreq = vreq;
			this.resp = resp;
		}

		public abstract void processRequest() throws IOException;

		protected String getStringParameter(String key, String defaultValue) {
			String value = vreq.getParameter(key);
			return (value == null) ? defaultValue : value;
		}

	}

	/**
	 * What is our reaction to this possible External Auth ID?
	 * 
	 * Is somebody already using it (other than ourselves)? Does it match an
	 * existing Profile? Neither?
	 */
	private static class ExternalAuthChecker extends AjaxResponder {
		private static final String PARAMETER_USER_ACCOUNT_URI = "userAccountUri";
		private static final String PARAMETER_ETERNAL_AUTH_ID = "externalAuthId";
		private static final String RESPONSE_ID_IN_USE = "idInUse";
		private static final String RESPONSE_MATCHES_PROFILE = "matchesProfile";
		private static final String RESPONSE_PROFILE_URI = "profileUri";
		private static final String RESPONSE_PROFILE_URL = "profileUrl";
		private static final String RESPONSE_PROFILE_LABEL = "profileLabel";

		private final String userAccountUri;
		private final String externalAuthId;

		public ExternalAuthChecker(HttpServlet parent, VitroRequest vreq,
				HttpServletResponse resp) {
			super(parent, vreq, resp);
			userAccountUri = getStringParameter(PARAMETER_USER_ACCOUNT_URI, "");
			externalAuthId = getStringParameter(PARAMETER_ETERNAL_AUTH_ID, "");
		}

		@Override
		public void processRequest() throws IOException {
			// TODO For now, a totally bogus response:
			// If "A", somebody else is already using the externalAuthId
			// If "B", matches "Joe Blow"
			// Anything else, no match.
			try {
				if ("A".equals(externalAuthId)) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(RESPONSE_ID_IN_USE, true);
					resp.getWriter().write(jsonObject.toString());
				} else if ("B".equals(externalAuthId)) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put(RESPONSE_MATCHES_PROFILE, true);
					jsonObject.put(RESPONSE_PROFILE_URI,
							"http://some.bogus.profile");
					jsonObject.put(RESPONSE_PROFILE_URL,
					"http://some.bogus.profileUrl");
					jsonObject.put(RESPONSE_PROFILE_LABEL, "bogus label");
					resp.getWriter().write(jsonObject.toString());
				} else {
					resp.getWriter().write("[]");
				}
			} catch (JSONException e) {
				resp.getWriter().write("[]");
			}
		}
	}

	private static class ErrorResponder extends AjaxResponder {
		public ErrorResponder(HttpServlet parent, VitroRequest vreq,
				HttpServletResponse resp) {
			super(parent, vreq, resp);
		}

		@Override
		public void processRequest() throws IOException {
			resp.getWriter().write("[]");
		}
	}

}
