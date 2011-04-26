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

import edu.cornell.mannlib.vitro.webapp.auth.identifier.FakeSelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages;

/**
 * TODO This is caught in the middle of the transition from LoginFormBean to LoginStatusBean.
 */
public class FakeSelfEditController extends VitroHttpServlet {
	// TODO When the LoginFormBean goes away, these should too.
	private static final String ATTRIBUTE_LOGIN_FORM_BEAN = "loginHandler";
	private static final String ATTRIBUTE_LOGIN_FORM_SAVE = "saveLoginHandler";
	
	private static final String ATTRIBUTE_LOGIN_STATUS_BEAN = "loginStatus";
	private static final String ATTRIBUTE_LOGIN_STATUS_SAVE = "saveLoginStatus";

	private static final Log log = LogFactory
			.getLog(FakeSelfEditController.class.getName());

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		try {
			super.doGet(request, response);
			
			VitroRequest vreq = new VitroRequest(request);
			HttpSession session = request.getSession();

			if (!isAuthorized(vreq, session)) {
				redirectToLoginPage(request, response);
			} else if (vreq.getParameter("force") != null) {
				startFaking(vreq, response);
			} else if (vreq.getParameter("stopfaking") != null) {
				stopFaking(vreq, response, session);
			} else {
				showTheForm(vreq, response, session);
			}
		} catch (Exception e) {
			log.error("FakeSelfEditController could not forward to view.");
			log.error(e, e);
		}
	}

	private boolean isAuthorized(VitroRequest vreq, HttpSession session) {
		boolean isFakingAlready = (session.getAttribute(ATTRIBUTE_LOGIN_STATUS_SAVE) != null);
		boolean isAdmin = PolicyHelper.isAuthorizedForActions(vreq, new UseMiscellaneousAdminPages());
		log.debug("isFakingAlready: " + isFakingAlready + ", isAdmin: "	+ isAdmin);
		return isAdmin || isFakingAlready;
	}

	private void startFaking(VitroRequest vreq, HttpServletResponse response)
			throws IOException {
		HttpSession session = vreq.getSession();
		String id = vreq.getParameter("netid");
		FakeSelfEditingIdentifierFactory.putFakeIdInSession(id, session);

		// Remove the login bean - so we are ONLY self-editing
		moveAttribute(session, ATTRIBUTE_LOGIN_FORM_BEAN,
				ATTRIBUTE_LOGIN_FORM_SAVE);
		moveAttribute(session, ATTRIBUTE_LOGIN_STATUS_BEAN,
				ATTRIBUTE_LOGIN_STATUS_SAVE);

		log.debug("Start faking as " + id);
		response.sendRedirect(vreq.getContextPath() + Controllers.ENTITY
				+ "?netid=" + id);
	}

	private void stopFaking(VitroRequest request, HttpServletResponse response,
			HttpSession session) throws IOException {
		FakeSelfEditingIdentifierFactory.clearFakeIdInSession(session);

		// Restore our original login status.
		restoreAttribute(session, ATTRIBUTE_LOGIN_FORM_BEAN,
				ATTRIBUTE_LOGIN_FORM_SAVE);
		restoreAttribute(session, ATTRIBUTE_LOGIN_STATUS_BEAN,
				ATTRIBUTE_LOGIN_STATUS_SAVE);

		log.debug("Stop faking.");
		response.sendRedirect(request.getContextPath() + "/");
	}

	private void showTheForm(VitroRequest request,
			HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {
		// Logged in as site admin: Form not yet submitted
		request.setAttribute("msg", figureMessage(session));
		request.setAttribute("title", "Self-Edit Test");
		request.setAttribute("bodyJsp", "/admin/fakeselfedit.jsp");
		RequestDispatcher rd = request
				.getRequestDispatcher(Controllers.BASIC_JSP);
		rd.forward(request, response);
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

	private void moveAttribute(HttpSession session,
			String realAttribute, String saveAttribute) {
		Object value = session.getAttribute(realAttribute);
		if (value != null) {
			session.setAttribute(saveAttribute, value);
			session.removeAttribute(realAttribute);
		}
	}

	private void restoreAttribute(HttpSession session,
			String realAttribute, String saveAttribute) {
		Object value = session.getAttribute(saveAttribute);
		if (value != null) {
			session.setAttribute(realAttribute, value);
			session.removeAttribute(saveAttribute);
		}
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
