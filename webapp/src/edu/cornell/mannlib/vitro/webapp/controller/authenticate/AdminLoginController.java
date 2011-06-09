/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import static edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource.INTERNAL;
import static edu.cornell.mannlib.vitro.webapp.beans.UserAccount.MAX_PASSWORD_LENGTH;
import static edu.cornell.mannlib.vitro.webapp.beans.UserAccount.MIN_PASSWORD_LENGTH;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
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
	private static final Log log = LogFactory
			.getLog(AdminLoginController.class);

	public static final String PARAMETER_EMAIL_ADDRESS = "email";
	public static final String PARAMETER_PASSWORD = "password";
	public static final String PARAMETER_NEW_PASSWORD = "newPassword";
	public static final String PARAMETER_CONFIRM_PASSWORD = "confirmPassword";

	public static final String URL_THIS = "/admin/login";
	public static final String URL_HOME_PAGE = "/";

	public static final String TEMPLATE_NAME = "adminLogin.ftl";

	private static final String MESSAGE_NO_EMAIL_ADDRESS = "errorNoEmail";
	private static final String MESSAGE_NO_PASSWORD = "errorNoPassword";
	private static final String MESSAGE_LOGIN_FAILED = "errorLoginFailed";
	private static final String MESSAGE_NEW_PASSWORD_REQUIRED = "newPasswordRequired";
	private static final String MESSAGE_NEW_PASSWORD_WRONG_LENGTH = "errorNewPasswordWrongLength";
	private static final String MESSAGE_NEW_PASSWORDS_DONT_MATCH = "errorNewPasswordsDontMatch";
	private static final String MESSAGE_NEW_PASSWORD_MATCHES_OLD = "errorNewPasswordMatchesOld";

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return Actions.AUTHORIZED; // No requirements to use this page.
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

		private final String emailAddress;
		private final String password;
		private final String newPassword;
		private final String confirmPassword;
		private final UserAccount userAccount;

		public Core(VitroRequest vreq) {
			this.auth = Authenticator.getInstance(vreq);

			this.emailAddress = nonNull(vreq
					.getParameter(PARAMETER_EMAIL_ADDRESS));
			this.password = nonNull(vreq.getParameter(PARAMETER_PASSWORD));
			this.newPassword = nonNull(vreq
					.getParameter(PARAMETER_NEW_PASSWORD));
			this.confirmPassword = nonNull(vreq
					.getParameter(PARAMETER_CONFIRM_PASSWORD));

			log.debug("Parameters: email='" + emailAddress + "', password='"
					+ password + "', newPassword='" + newPassword
					+ "', confirmPassword='" + confirmPassword + "'");

			this.userAccount = this.auth
					.getAccountForInternalAuth(emailAddress);
		}

		public ResponseValues process() {
			if (emailAddress.isEmpty() && password.isEmpty()) {
				return showForm();
			}
			if (emailAddress.isEmpty()) {
				return showForm(MESSAGE_NO_EMAIL_ADDRESS);
			}
			if (password.isEmpty()) {
				return showForm(MESSAGE_NO_PASSWORD);
			}
			if (newPasswordRequired()) {
				if (newPassword.isEmpty()) {
					return showForm(MESSAGE_NEW_PASSWORD_REQUIRED);
				}
				if (!isPasswordValidLength(newPassword)) {
					return showForm(MESSAGE_NEW_PASSWORD_REQUIRED,
							MESSAGE_NEW_PASSWORD_WRONG_LENGTH);
				}
				if (newPassword.equals(password)) {
					return showForm(MESSAGE_NEW_PASSWORD_REQUIRED,
							MESSAGE_NEW_PASSWORD_MATCHES_OLD);
				}
				if (!newPassword.equals(confirmPassword)) {
					return showForm(MESSAGE_NEW_PASSWORD_REQUIRED,
							MESSAGE_NEW_PASSWORDS_DONT_MATCH);
				}
			}

			boolean loggedIn = tryToLogin();
			if (loggedIn) {
				return goToHomePage();
			}

			return showForm(MESSAGE_LOGIN_FAILED);
		}

		private boolean newPasswordRequired() {
			return auth.isCurrentPassword(userAccount, password)
					&& (userAccount.isPasswordChangeRequired());
		}

		private boolean isPasswordValidLength(String pw) {
			return (pw.length() >= MIN_PASSWORD_LENGTH)
					&& (pw.length() <= MAX_PASSWORD_LENGTH);
		}

		private boolean tryToLogin() {
			if (auth.isCurrentPassword(userAccount, password)) {
				auth.recordLoginAgainstUserAccount(userAccount, INTERNAL);

				if (!newPassword.isEmpty()) {
					auth.recordNewPassword(userAccount, newPassword);
				}

				return true;
			} else {
				return false;
			}
		}

		private ResponseValues showForm(String... codes) {
			Map<String, Object> body = new HashMap<String, Object>();
			body.put("controllerUrl", UrlBuilder.getUrl(URL_THIS));
			body.put("email", emailAddress);
			body.put("password", password);
			body.put("newPassword", newPassword);
			body.put("confirmPassword", confirmPassword);

			for (String code : codes) {
				body.put(code, Boolean.TRUE);
			}

			log.debug("showing form with values: " + body);

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
