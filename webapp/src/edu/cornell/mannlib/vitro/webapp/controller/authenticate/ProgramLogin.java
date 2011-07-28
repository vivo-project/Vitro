/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import static edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource.INTERNAL;
import static edu.cornell.mannlib.vitro.webapp.beans.UserAccount.MAX_PASSWORD_LENGTH;
import static edu.cornell.mannlib.vitro.webapp.beans.UserAccount.MIN_PASSWORD_LENGTH;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Provide a means for programmatic login If they provide the right parameters,
 * log them in and send 200. Otherwise, send 403 error.
 */
public class ProgramLogin extends HttpServlet {
	private static final Log log = LogFactory.getLog(ProgramLogin.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		new ProgramLoginCore(req, resp).process();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		new ProgramLoginCore(req, resp).process();
	}

	static class ProgramLoginCore {
		public static final String PARAM_EMAIL_ADDRESS = "email";
		public static final String PARAM_PASSWORD = "password";
		public static final String PARAM_NEW_PASSWORD = "newPassword";
		public static final int ERROR_CODE = 403;

		private static final String MESSAGE_NEED_EMAIL_ADDRESS = PARAM_EMAIL_ADDRESS
				+ " parameter is required.";
		private static final String MESSAGE_NEED_PASSWORD = PARAM_PASSWORD
				+ " parameter is required.";
		private static final String MESSAGE_WRONG_USER_OR_PASSWORD = PARAM_EMAIL_ADDRESS
				+ " or " + PARAM_PASSWORD + " is incorrect.";
		private static final String MESSAGE_NEED_NEW_PASSWORD = "first-time login: "
				+ PARAM_NEW_PASSWORD + " parameter is required.";
		private static final String MESSAGE_NEW_PASSWORD_NOT_NEEDED = "not first-time login: "
				+ PARAM_NEW_PASSWORD + " parameter is not allowed.";
		private static final String MESSAGE_NEW_PASSWORD_WRONG_LENGTH = PARAM_NEW_PASSWORD
				+ " must be between "
				+ MIN_PASSWORD_LENGTH
				+ " and "
				+ MAX_PASSWORD_LENGTH + " characters.";
		private static final String MESSAGE_PASSWORD_MUST_BE_DIFFERENT = PARAM_NEW_PASSWORD
				+ " must not be the same as " + PARAM_PASSWORD;
		private static final String MESSAGE_SUCCESS_FIRST_TIME = "first-time login successful.";
		private static final String MESSAGE_SUCCESS = "login successful.";

		private final HttpServletRequest req;
		private final HttpServletResponse resp;
		private final Authenticator auth;

		private final String emailAddress;
		private final String password;
		private final String newPassword;
		private final UserAccount userAccount;

		ProgramLoginCore(HttpServletRequest req, HttpServletResponse resp) {
			this.req = req;
			this.resp = resp;

			this.emailAddress = getParameter(PARAM_EMAIL_ADDRESS);
			this.password = getParameter(PARAM_PASSWORD);
			this.newPassword = getParameter(PARAM_NEW_PASSWORD);

			log.debug("request: email='" + emailAddress + "', password='"
					+ password + "', newPassword='" + newPassword + "'");

			this.auth = Authenticator.getInstance(req);

			this.userAccount = auth
					.getAccountForInternalAuth(this.emailAddress);
		}

		void process() throws IOException {
			if (emailAddress.isEmpty()) {
				sendError(MESSAGE_NEED_EMAIL_ADDRESS);
				return;
			}
			if (password.isEmpty()) {
				sendError(MESSAGE_NEED_PASSWORD);
				return;
			}
			if (!usernameAndPasswordAreValid()) {
				sendError(MESSAGE_WRONG_USER_OR_PASSWORD);
				return;
			}

			if (!isPasswordChangeRequired()) {
				if (!newPassword.isEmpty()) {
					sendError(MESSAGE_NEW_PASSWORD_NOT_NEEDED);
					return;
				}
				recordLogin();
				sendSuccess(MESSAGE_SUCCESS);
				return;
			}

			if (isPasswordChangeRequired()) {
				if (newPassword.isEmpty()) {
					sendError(MESSAGE_NEED_NEW_PASSWORD);
					return;
				}
				if (!newPasswordIsValidPasswordLength()) {
					sendError(MESSAGE_NEW_PASSWORD_WRONG_LENGTH);
					return;
				}
				if (newPasswordMatchesCurrentPassword()) {
					sendError(MESSAGE_PASSWORD_MUST_BE_DIFFERENT);
					return;
				}
				recordLoginWithPasswordChange();
				sendSuccess(MESSAGE_SUCCESS_FIRST_TIME);
				return;
			}

		}

		private String getParameter(String key) {
			String value = req.getParameter(key);
			if (value != null) {
				return value.trim();
			} else {
				return "";
			}
		}

		private boolean usernameAndPasswordAreValid() {
			return auth.isCurrentPassword(userAccount, password);
		}

		private boolean newPasswordIsValidPasswordLength() {
			return (newPassword.length() >= MIN_PASSWORD_LENGTH)
					&& (newPassword.length() <= MAX_PASSWORD_LENGTH);
		}

		private boolean newPasswordMatchesCurrentPassword() {
			return newPassword.equals(password);
		}

		private boolean isPasswordChangeRequired() {
			return (userAccount.isPasswordChangeRequired());
		}

		private void recordLogin() {
			auth.recordLoginAgainstUserAccount(userAccount, INTERNAL);
		}

		private void recordLoginWithPasswordChange() {
			auth.recordNewPassword(userAccount, newPassword);
			auth.recordLoginAgainstUserAccount(userAccount, INTERNAL);
		}

		private void sendError(String message) throws IOException {
			resp.sendError(ERROR_CODE, message);
		}

		private void sendSuccess(String message) throws IOException {
			PrintWriter writer = resp.getWriter();
			writer.println(message);
		}
	}
}