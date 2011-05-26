/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

/**
 * Parcel out the different actions required of the UserAccounts GUI.
 */
public class UserAccountsUserController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(UserAccountsUserController.class);

	public static final String BOGUS_STANDARD_MESSAGE = "Request failed. Please contact your system administrator.";

	private static final String ACTION_CREATE_PASSWORD = "/createPassword";
	private static final String ACTION_RESET_PASSWORD = "/resetPassword";

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return Actions.AUTHORIZED;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		if (log.isDebugEnabled()) {
			dumpRequestParameters(vreq);
		}

		String action = vreq.getPathInfo();
		log.debug("action = '" + action + "'");

		if (ACTION_CREATE_PASSWORD.equals(action)) {
			return handleCreatePasswordRequest(vreq);
		} else if (ACTION_RESET_PASSWORD.equals(action)) {
			return handleResetPasswordRequest(vreq);
		} else {
			return handleInvalidRequest(vreq);
		}
	}

	private ResponseValues handleCreatePasswordRequest(VitroRequest vreq) {
		UserAccountsCreatePasswordPage page = new UserAccountsCreatePasswordPage(
				vreq);
		if (page.isBogus()) {
			return showHomePage(vreq, page.getBogusMessage());
		} else if (page.isSubmit() && page.isValid()) {
			page.createPassword();
			return showHomePage(vreq,
					"Your password has been saved. Please log in.");
		} else {
			return page.showPage();
		}

	}

	private ResponseValues handleResetPasswordRequest(VitroRequest vreq) {
		UserAccountsResetPasswordPage page = new UserAccountsResetPasswordPage(
				vreq);
		if (page.isBogus()) {
			return showHomePage(vreq, page.getBogusMessage());
		} else if (page.isSubmit() && page.isValid()) {
			page.resetPassword();
			return showHomePage(vreq,
					"Your password has been saved. Please log in.");
		} else {
			return page.showPage();
		}

	}

	private ResponseValues handleInvalidRequest(VitroRequest vreq) {
		return showHomePage(vreq, BOGUS_STANDARD_MESSAGE);
	}

	private ResponseValues showHomePage(VitroRequest vreq, String message) {
		DisplayMessage.setMessage(vreq, message);
		return new RedirectResponseValues("/");
	}

}
