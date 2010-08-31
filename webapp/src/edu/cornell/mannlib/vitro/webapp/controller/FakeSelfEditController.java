/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.FakeSelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

public class FakeSelfEditController extends VitroHttpServlet {

	private static final Log log = LogFactory
			.getLog(FakeSelfEditController.class.getName());

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		try {
			super.doGet(request, response);
			VitroRequest vreq = new VitroRequest(request);
			HttpSession session = request.getSession();

			if (!LoginFormBean.loggedIn(request, LoginFormBean.CURATOR)) {
				// Not logged in as site admin
				session.setAttribute("postLoginRequest", vreq.getRequestURI());
				response.sendRedirect(request.getContextPath()
						+ Controllers.LOGIN + "?login=block");
			} else if (vreq.getParameter("force") != null) {
				// Logged in as site admin: Form to use netid
				VitroRequestPrep.forceToSelfEditing(request);
				String id = request.getParameter("netid");
				FakeSelfEditingIdentifierFactory.clearFakeIdInSession(session);
				FakeSelfEditingIdentifierFactory.putFakeIdInSession(id, session);
				response.sendRedirect(request.getContextPath()
						+ Controllers.ENTITY + "?netid=" + id);
			} else if (request.getParameter("stopfaking") != null) {
				// Logged in as site admin: Form to stop using netid
				VitroRequestPrep.forceOutOfSelfEditing(request);
				FakeSelfEditingIdentifierFactory.clearFakeIdInSession(session);
				response.sendRedirect(request.getContextPath() + "/");
			} else {
				// Logged in as site admin: Form not yet submitted
				request.setAttribute("msg", figureMessage(session));
				request.setAttribute("title", "Self-Edit Test");
				request.setAttribute("bodyJsp", "/admin/fakeselfedit.jsp");
				RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
				rd.forward(request, response);
			}
		} catch (Exception e) {
			log.error("FakeSelfEditController could not forward to view.");
			log.error(e, e);
		}
	}

	/**
	 * Check if already logged in from previous form submission
	 */
	private String figureMessage(HttpSession session) {
		String netid = FakeSelfEditingIdentifierFactory.getFakeIdFromSession(session);
		if (netid != null) {
			return "You are testing self-editing as '" + netid + "'.";
		} else {
			return "You have not configured a netid to test self-editing.";
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
