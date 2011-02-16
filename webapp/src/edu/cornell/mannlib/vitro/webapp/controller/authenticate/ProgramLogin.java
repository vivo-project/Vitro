/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import static edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource.INTERNAL;
import static edu.cornell.mannlib.vitro.webapp.beans.User.MAX_PASSWORD_LENGTH;
import static edu.cornell.mannlib.vitro.webapp.beans.User.MIN_PASSWORD_LENGTH;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.beans.User;

/**
 * Provide a means for programmatic login If they provide the right parameters,
 * log them in and send 200. Otherwise, send 403 error.
 */
public class ProgramLogin extends HttpServlet {
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
		public static final String PARAM_USERNAME = "username";
		public static final String PARAM_PASSWORD = "password";
		public static final String PARAM_NEW_PASSWORD = "newPassword";
		public static final int ERROR_CODE = 403;

		private static final String MESSAGE_NEED_USERNAME = PARAM_USERNAME
				+ " parameter is required.";
		private static final String MESSAGE_NEED_PASSWORD = PARAM_PASSWORD
				+ " parameter is required.";
		private static final String MESSAGE_WRONG_USER_OR_PASSWORD = PARAM_USERNAME
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

		private final String username;
		private final String password;
		private final String newPassword;

		ProgramLoginCore(HttpServletRequest req, HttpServletResponse resp) {
			this.req = req;
			this.resp = resp;

			this.username = getParameter(PARAM_USERNAME);
			this.password = getParameter(PARAM_PASSWORD);
			this.newPassword = getParameter(PARAM_NEW_PASSWORD);

			this.auth = Authenticator.getInstance(req);
		}

		void process() throws IOException {
			if (username.isEmpty()) {
				sendError(MESSAGE_NEED_USERNAME);
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

			boolean passwordChangeRequired = isFirstTimeLogin();

			if (!passwordChangeRequired) {
				if (!newPassword.isEmpty()) {
					sendError(MESSAGE_NEW_PASSWORD_NOT_NEEDED);
					return;
				}
				recordLogin();
				sendSuccess(MESSAGE_SUCCESS);
				return;
			}

			if (passwordChangeRequired) {
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
			return auth.isExistingUser(username)
					&& auth.isCurrentPassword(username, password);
		}

		private boolean newPasswordIsValidPasswordLength() {
			return (newPassword.length() >= MIN_PASSWORD_LENGTH)
					&& (newPassword.length() <= MAX_PASSWORD_LENGTH);
		}

		private boolean newPasswordMatchesCurrentPassword() {
			return newPassword.equals(password);
		}

		private boolean isFirstTimeLogin() {
			User user = auth.getUserByUsername(username);
			return (user.getLoginCount() == 0);
		}

		private void recordLogin() {
			auth.recordLoginAgainstUserAccount(username, INTERNAL);
		}

		private void recordLoginWithPasswordChange() {
			auth.recordNewPassword(username, newPassword);
			auth.recordLoginAgainstUserAccount(username, INTERNAL);
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