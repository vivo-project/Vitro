/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;

/**
 * Handle the AJAX functions that are specific to the UserAccounts pages.
 */
@WebServlet(name = "AccountsAjax", urlPatterns = {"/accountsAjax/*"} )
public class UserAccountsAjaxController extends VitroAjaxController {
	private static final Log log = LogFactory
			.getLog(UserAccountsAjaxController.class);

	private static final String PARAMETER_ACTION = "action";

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.MANAGE_USER_ACCOUNTS.ACTION;
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

}
