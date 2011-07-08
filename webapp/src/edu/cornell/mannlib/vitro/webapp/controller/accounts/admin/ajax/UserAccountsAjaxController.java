/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageUserAccounts;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;

/**
 * Handle the AJAX functions that are specific to the UserAccounts pages.
 */
public class UserAccountsAjaxController extends VitroAjaxController {
	private static final Log log = LogFactory
			.getLog(UserAccountsAjaxController.class);

	private static final String PARAMETER_ACTION = "action";

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return new Actions(new ManageUserAccounts());
	}

	@Override
	protected void doRequest(VitroRequest vreq, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String function = vreq.getParameter(PARAMETER_ACTION);
			if ("checkExternalAuth".equals(function)) {
				new ExternalAuthChecker(this, vreq, resp).processRequest();
			} else if ("autoCompleteProfile".equals(function)) {
				new ProfileAutoCompleter(this, vreq, resp).processRequest();
			} else {
				resp.getWriter().write("[]");
			}
		} catch (Exception e) {
			log.error(e, e);
			resp.getWriter().write("[]");
		}
	}

	static abstract class AjaxResponder {
		protected static final String EMPTY_RESPONSE = "[]";

		protected final HttpServlet parent;
		protected final VitroRequest vreq;
		protected final HttpServletResponse resp;
		protected final IndividualDao indDao;
		protected final UserAccountsDao uaDao;

		public AjaxResponder(HttpServlet parent, VitroRequest vreq,
				HttpServletResponse resp) {
			this.parent = parent;
			this.vreq = vreq;
			this.resp = resp;
			this.indDao = vreq.getWebappDaoFactory().getIndividualDao();
			this.uaDao = vreq.getWebappDaoFactory().getUserAccountsDao();
		}

		public final void processRequest() {
			try {
				resp.getWriter().write(prepareResponse());
			} catch (Exception e) {
				log.error("Problem with AJAX response", e);
			}
		}

		protected abstract String prepareResponse() throws IOException,
				JSONException;

		protected String getStringParameter(String key, String defaultValue) {
			String value = vreq.getParameter(key);
			return (value == null) ? defaultValue : value;
		}

	}

}
