/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import static edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource.INTERNAL;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Provide a "hidden" login page for systems where the Login Widget has been
 * modified to only show the link to an External Authentication system.
 * 
 * This page is only hidden because there is no link to it. Anyone who knows the
 * URL can come here, but they need to pass Internal Authentication to proceed.
 */
public class AdminLoginController extends FreemarkerHttpServlet {
	public static final String PARAMETER_USERNAME = "username";
	public static final String PARAMETER_PASSWORD = "password";
	public static final String PARAMETER_NEW_PASSWORD = "newPassword";

	public static final String URL_THIS = "/admin/login";
	public static final String URL_HOME_PAGE = "/";

	public static final String TEMPLATE_NAME = "adminLogin.ftl";

	private static final String MESSAGE_NO_USERNAME = "errorNoUser";
	private static final String MESSAGE_NO_PASSWORD = "errorNoPassword";
	private static final String MESSAGE_LOGIN_FAILED = "errorLoginFailed";
	private static final String MESSAGE_NEW_PASSWORD_REQUIRED = "newPasswordRequired";

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return Actions.EMPTY; // No requirements to use this page.
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		return new Core(vreq).process();
	}

	/**
	 * A threadsafe holder for the controller logic.
	 */
	private static class Core {
		private final Authenticator auth;

		private final String username;
		private final String password;
		private final String newPassword;

		public Core(VitroRequest vreq) {
			this.auth = Authenticator.getInstance(vreq);

			this.username = nonNull(vreq.getParameter(PARAMETER_USERNAME));
			this.password = nonNull(vreq.getParameter(PARAMETER_PASSWORD));
			this.newPassword = nonNull(vreq
					.getParameter(PARAMETER_NEW_PASSWORD));
		}

		public ResponseValues process() {
			if (username.isEmpty() && password.isEmpty()) {
				return showInitialForm();
			}
			if (username.isEmpty()) {
				return showFormWithMessage(MESSAGE_NO_USERNAME);
			}
			if (password.isEmpty()) {
				return showFormWithMessage(MESSAGE_NO_PASSWORD);
			}
			if (newPasswordRequired() && newPassword.isEmpty()) {
				return showFormWithMessage(MESSAGE_NEW_PASSWORD_REQUIRED);
			}

			boolean loggedIn = tryToLogin();
			if (loggedIn) {
				return goToHomePage();
			}

			return showFormWithMessage(MESSAGE_LOGIN_FAILED);
		}

		private boolean newPasswordRequired() {
			return auth.isCurrentPassword(username, password)
					&& auth.isPasswordChangeRequired(username);
		}

		private boolean tryToLogin() {
			if (auth.isCurrentPassword(username, password)) {
				auth.recordLoginAgainstUserAccount(username, INTERNAL);

				if (auth.isPasswordChangeRequired(username)) {
					auth.recordNewPassword(username, newPassword);
				}

				return true;
			} else {
				return false;
			}
		}

		private ResponseValues showInitialForm() {
			Map<String, Object> body = new HashMap<String, Object>();
			body.put("controllerUrl", UrlBuilder.getUrl(URL_THIS));
			body.put("username", "");
			return new TemplateResponseValues(TEMPLATE_NAME, body);
		}

		private ResponseValues showFormWithMessage(String messageCode) {
			Map<String, Object> body = new HashMap<String, Object>();
			body.put("controllerUrl", UrlBuilder.getUrl(URL_THIS));
			body.put("username", username);
			body.put(messageCode, Boolean.TRUE);
			return new TemplateResponseValues(TEMPLATE_NAME, body);
		}

		private ResponseValues goToHomePage() {
			return new RedirectResponseValues(URL_HOME_PAGE);
		}

		private String nonNull(String s) {
			return (s == null) ? "" : s;
		}
	}
}
