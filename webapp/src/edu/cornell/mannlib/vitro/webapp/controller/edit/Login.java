/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;

/**
 * Provide a means for programmatic login (replaces old login_process.jsp). If
 * they provide the right parameters, send them to be authenticated.
 */
public class Login extends HttpServlet {
	private final static int DEFAULT_PORTAL_ID = 1;

	public static final String PARAM_USERNAME = "loginName";
	public static final String PARAM_PASSWORD = "loginPassword";

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/*
		 * For backward compatibility, if they requested a logout, honor the
		 * request.
		 */
		if ("Log Out".equals(request.getParameter("loginSubmitMode"))) {
			request.getRequestDispatcher("/logout").forward(request, response);
			return;
		}

		String username = request.getParameter(PARAM_USERNAME);
		String password = request.getParameter(PARAM_PASSWORD);

		/*
		 * If either the username or password are empty, send them to the site
		 * admin page.
		 */
		if ((username == null) || (username.equals("")) || (password == null)
				|| (password.equals(""))) {
			response.sendRedirect(request.getContextPath()
					+ Controllers.SITE_ADMIN + "?home="
					+ getPortalIdString(request));
			return;
		}

		/*
		 * Otherwise, set up as if they had filled in the login form, and send
		 * them to authenticate it.
		 */
		LoginProcessBean bean = LoginProcessBean.getBeanFromSession(request);
		bean.setState(LoginProcessBean.State.LOGGING_IN);
		request.getRequestDispatcher(Controllers.AUTHENTICATE).forward(request,
				response);
	}

	private final String getPortalIdString(HttpServletRequest request) {
		String pId = (String) request.getAttribute("home");
		if (pId == null) {
			pId = request.getParameter("home");
		}
		if (pId == null) {
			pId = String.valueOf(DEFAULT_PORTAL_ID);
		}
		return pId;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
