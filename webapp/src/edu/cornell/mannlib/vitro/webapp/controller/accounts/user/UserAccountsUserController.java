/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.user;

import static edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource.EXTERNAL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.EditOwnAccount;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginRedirector;
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
	private static final String ACTION_MY_ACCOUNT = "/myAccount";
	private static final String ACTION_FIRST_TIME_EXTERNAL = "/firstTimeExternal";

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		String action = vreq.getPathInfo();

		if (ACTION_MY_ACCOUNT.equals(action)) {
			return new Actions(new EditOwnAccount());
		} else {
			return Actions.AUTHORIZED;
		}
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		if (log.isDebugEnabled()) {
			dumpRequestParameters(vreq);
		}

		String action = vreq.getPathInfo();
		log.debug("action = '" + action + "'");

		if (ACTION_MY_ACCOUNT.equals(action)) {
			return handleMyAccountRequest(vreq);
		} else if (ACTION_CREATE_PASSWORD.equals(action)) {
			return handleCreatePasswordRequest(vreq);
		} else if (ACTION_RESET_PASSWORD.equals(action)) {
			return handleResetPasswordRequest(vreq);
		} else if (ACTION_FIRST_TIME_EXTERNAL.equals(action)) {
			return handleFirstTimeLoginFromExternalAccount(vreq);
		} else {
			return handleInvalidRequest(vreq);
		}
	}

	private ResponseValues handleMyAccountRequest(VitroRequest vreq) {
		UserAccountsMyAccountPage page = new UserAccountsMyAccountPage(vreq);
		if (page.isSubmit() && page.isValid()) {
			page.updateAccount();
		}
		return page.showPage();
	}

	private ResponseValues handleCreatePasswordRequest(VitroRequest vreq) {
		UserAccountsCreatePasswordPage page = new UserAccountsCreatePasswordPage(
				vreq);
		if (page.isBogus()) {
			return showHomePage(vreq, page.getBogusMessage());
		} else if (page.isSubmit() && page.isValid()) {
			page.createPassword();
			return showHomePage(vreq, page.getSuccessMessage());
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
			return showHomePage(vreq, page.getSuccessMessage());
		} else {
			return page.showPage();
		}

	}

	private ResponseValues handleFirstTimeLoginFromExternalAccount(
			VitroRequest vreq) {
		UserAccountsFirstTimeExternalPage page = new UserAccountsFirstTimeExternalPage(
				vreq);
		if (page.isBogus()) {
			return showHomePage(vreq, page.getBogusMessage());
		} else if (page.isSubmit() && page.isValid()) {
			UserAccount userAccount = page.createAccount();
			Authenticator auth = Authenticator.getInstance(vreq);
			auth.recordLoginAgainstUserAccount(userAccount, EXTERNAL);
			return showLoginRedirection(vreq, page.getAfterLoginUrl());
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

	private ResponseValues showLoginRedirection(VitroRequest vreq,
			String afterLoginUrl) {
		LoginRedirector lr = new LoginRedirector(vreq, afterLoginUrl);
		DisplayMessage.setMessage(vreq, lr.assembleWelcomeMessage());
		String uri = lr.getRedirectionUriForLoggedInUser();
		return new RedirectResponseValues(stripContextPath(vreq, uri));
	}

	/**
	 * TODO The LoginRedirector gives a URI that includes the context path. But
	 * the RedirectResponseValues wants a URI that does not include the context
	 * path.
	 * 
	 * Bridge the gap.
	 */
	private String stripContextPath(VitroRequest vreq, String uri) {
		if ((uri == null) || uri.isEmpty() || uri.equals(vreq.getContextPath())) {
			return "/";
		}
		if (uri.contains("://")) {
			return uri;
		}
		if (uri.startsWith(vreq.getContextPath() + '/')) {
			return uri.substring(vreq.getContextPath().length());
		}
		return uri;
	}
}
