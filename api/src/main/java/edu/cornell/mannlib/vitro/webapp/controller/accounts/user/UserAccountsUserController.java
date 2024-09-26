/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.user;

import static edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource.EXTERNAL;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ContextPath;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator.LoginNotPermitted;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginRedirector;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;

/**
 * Parcel out the different actions required of the UserAccounts GUI.
 */
@WebServlet(name = "AccountsUser", urlPatterns = {"/accounts/*"} )
public class UserAccountsUserController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(UserAccountsUserController.class);

	private static final String ACTION_CREATE_PASSWORD = "/createPassword";
	private static final String ACTION_RESET_PASSWORD = "/resetPassword";
	private static final String ACTION_MY_ACCOUNT = "/myAccount";
	private static final String ACTION_FIRST_TIME_EXTERNAL = "/firstTimeExternal";

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		String action = vreq.getPathInfo();

		if (ACTION_MY_ACCOUNT.equals(action)) {
			return SimplePermission.EDIT_OWN_ACCOUNT.ACTION;
		} else {
			return AuthorizationRequest.AUTHORIZED;
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
			try {
				UserAccount userAccount = page.createAccount();
				Authenticator auth = Authenticator.getInstance(vreq);
				auth.recordLoginAgainstUserAccount(userAccount, EXTERNAL);
				return showLoginRedirection(vreq, page.getAfterLoginUrl());
			} catch (LoginNotPermitted e) {
				// This should have been anticipated by the page.
				return showHomePage(vreq, getBogusStandardMessage(vreq));
			}
		} else {
			return page.showPage();
		}
	}

	private ResponseValues handleInvalidRequest(VitroRequest vreq) {
		return showHomePage(vreq, getBogusStandardMessage(vreq));
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
		String contextPath = ContextPath.getPath(vreq);
        if ((uri == null) || uri.isEmpty() || uri.equals(contextPath)) {
			return "/";
		}
		if (uri.contains("://")) {
			return uri;
		}
		if (uri.startsWith(contextPath + '/')) {
			return uri.substring(contextPath.length());
		}
		return uri;
	}

	public static String getBogusStandardMessage(HttpServletRequest req) {
		return I18n.bundle(req).text("request_failed");
	}
}
