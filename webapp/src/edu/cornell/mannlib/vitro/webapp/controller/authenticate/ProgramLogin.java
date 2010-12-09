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
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_PASSWORD = "password";
	public static final String PARAM_NEW_PASSWORD = "newPassword";
	public static final int ERROR_CODE = 403;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Authenticator auth = Authenticator.getInstance(req);

		String username = req.getParameter(PARAM_USERNAME);
		String password = req.getParameter(PARAM_PASSWORD);
		String newPassword = req.getParameter(PARAM_NEW_PASSWORD);

		// username is required
		if ((username == null) || username.isEmpty()) {
			resp.sendError(ERROR_CODE, PARAM_USERNAME
					+ " parameter is required.");
			return;
		}

		// password is required
		if ((password == null) || password.isEmpty()) {
			resp.sendError(ERROR_CODE, PARAM_PASSWORD
					+ " parameter is required.");
			return;
		}

		// user must exist and password must be correct
		if (!auth.isExistingUser(username)
				|| (!auth.isCurrentPassword(username, password))) {
			resp.sendError(ERROR_CODE, PARAM_USERNAME + " or " + PARAM_PASSWORD
					+ " is incorrect.");
			return;
		}

		User user = auth.getUserByUsername(username);
		boolean firstTime = (user.getLoginCount() == 0);

		if (firstTime) {
			// on first-time login, new password is required
			if ((newPassword == null) || newPassword.isEmpty()) {
				resp.sendError(ERROR_CODE, "first-time login: "
						+ PARAM_NEW_PASSWORD + " parameter is required.");
				return;
			}

			// on first-time login, new password must be correct length
			if ((newPassword.length() < MIN_PASSWORD_LENGTH)
					|| (newPassword.length() > MAX_PASSWORD_LENGTH)) {
				resp.sendError(ERROR_CODE, PARAM_PASSWORD + " must be between "
						+ MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH
						+ " characters.");
				return;
			}

			// on first-time login, new password must be different from old
			if (auth.isCurrentPassword(username, newPassword)) {
				resp.sendError(ERROR_CODE, PARAM_NEW_PASSWORD
						+ " must not be the same as " + PARAM_PASSWORD);
				return;
			}

			auth.recordNewPassword(username, newPassword);
			auth.recordLoginAgainstUserAccount(username, INTERNAL);
			sendSuccess(resp, "first-time login successful.");
			return;
		} else {
			// not first-time login, new password is not allowed
			if ((newPassword != null) && (!newPassword.isEmpty())) {
				resp.sendError(ERROR_CODE, "not first-time login: "
						+ PARAM_NEW_PASSWORD + " parameter is not allowed.");
				return;
			}

			auth.recordLoginAgainstUserAccount(username, INTERNAL);
			sendSuccess(resp, "login successful.");
			return;
		}
	}

	private void sendSuccess(HttpServletResponse resp, String message)
			throws IOException {
		PrintWriter writer = resp.getWriter();
		writer.println(message);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
}
