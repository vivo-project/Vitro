/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LogoutRedirector;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;

/**
 * Provide a means for programmatic logout.
 */
@WebServlet(name = "logout", urlPatterns = {"/logout"})
public class Logout extends HttpServlet {
	private static final Log log = LogFactory.getLog(Logout.class.getName());

	/** This http header holds the referring page. */
	private static final String HEADING_REFERRER = "referer";

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			String referrer = getReferringPage(request);
			String redirectUrl = LogoutRedirector.getRedirectUrl(request, response, referrer);

			Authenticator.getInstance(request).recordUserIsLoggedOut();
			DisplayMessage.setMessage(request, I18n.bundle(request).text("logged_out"));

			response.sendRedirect(redirectUrl);
		} catch (Exception ex) {
			log.error(ex, ex);
		}
	}

	private String getReferringPage(HttpServletRequest request) {
		String referrer = request.getHeader(HEADING_REFERRER);
		if (referrer == null) {
			referrer = "/.";
		}
		log.debug("Referring page is '" + referrer + "'");
		return referrer;
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doPost(request, response);
	}
}
