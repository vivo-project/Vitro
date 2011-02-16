/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;

/**
 * Set up the external authorization process.
 * 
 * Write down the page that triggered the request, so we can get back to it.
 * 
 * Send a request to the external authorization server that will return us to
 * the LoginExternalAuthReturn servlet for further processing.
 */
public class LoginExternalAuthSetup extends BaseLoginServlet {
	private static final Log log = LogFactory
			.getLog(LoginExternalAuthSetup.class);

	/** This session attribute tells where we came from. */
	static final String ATTRIBUTE_REFERRER = LoginExternalAuthSetup.class
			.getName() + ".referrer";

	private static final String RETURN_SERVLET_URL = "/loginExternalAuthReturn";

	/** This http header holds the referring page. */
	private static final String HEADING_REFERRER = "referer";

	/**
	 * Write down the referring page, record that we are logging in, and
	 * redirect to the external authorization server URL.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		storeTheReferringPage(req);

		LoginProcessBean.getBean(req).setState(
				LoginProcessBean.State.LOGGING_IN);

		String returnUrl = buildReturnUrl(req);
		String redirectUrl = ExternalAuthHelper.getHelper(req)
				.buildExternalAuthRedirectUrl(returnUrl);

		if (redirectUrl == null) {
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_LOGIN_FAILED);
		}

		log.debug("redirecting to '" + redirectUrl + "'");
		resp.sendRedirect(redirectUrl);
	}

	/** Remember where we came from - we'll need to go back there. */
	private void storeTheReferringPage(HttpServletRequest req) {
		String referrer = req.getHeader(HEADING_REFERRER);
		if (referrer == null) {
			dumpRequestHeaders(req);
			referrer = figureHomePageUrl(req);
		}
		log.debug("Referring page is '" + referrer + "'");
		req.getSession().setAttribute(ATTRIBUTE_REFERRER, referrer);
	}

	/** What is the URL of the LoginExternalAuthReturn servlet? */
	private String buildReturnUrl(HttpServletRequest req) {
		return figureHomePageUrl(req) + RETURN_SERVLET_URL;
	}

	private void dumpRequestHeaders(HttpServletRequest req) {
		if (log.isDebugEnabled()) {
			@SuppressWarnings("unchecked")
			Enumeration<String> names = req.getHeaderNames();
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				log.debug("header: " + name + "=" + req.getHeader(name));
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
