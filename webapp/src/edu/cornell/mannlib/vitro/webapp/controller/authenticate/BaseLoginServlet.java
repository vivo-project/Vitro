/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;

/**
 * Base class for all Login servlets, whether Shibboleth, CuWebAuth, etc.
 */
public class BaseLoginServlet extends HttpServlet {
	private static final Log log = LogFactory.getLog(BaseLoginServlet.class);

	/** A general purpose error message for the user to see. */
	protected static final Message MESSAGE_LOGIN_FAILED = new LoginProcessBean.Message(
			"External login failed.", LoginProcessBean.MLevel.ERROR);

	protected Authenticator getAuthenticator(HttpServletRequest req) {
		return Authenticator.getInstance(req);
	}

	/**
	 * Store an error message in the login bean and go back where we came from.
	 * 
	 * Remove the referring URL from the session after using it.
	 */
	protected void complainAndReturnToReferrer(HttpServletRequest req,
			HttpServletResponse resp, String sessionAttributeForReferrer,
			Message message, Object... args) throws IOException {
		log.debug(message.getMessageLevel() + ": "
				+ message.formatMessage(args));
		LoginProcessBean.getBean(req).setMessage(message, args);

		String referrer = (String) req.getSession().getAttribute(
				sessionAttributeForReferrer);
		log.debug("returning to referrer: " + referrer);
		if (referrer == null) {
			referrer = figureHomePageUrl(req);
			log.debug("returning to home page: " + referrer);
		}

		req.getSession().removeAttribute(sessionAttributeForReferrer);
		resp.sendRedirect(referrer);
	}

	/**
	 * If we don't have a referrer, send them to the home page.
	 */
	protected String figureHomePageUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		String uri = req.getRequestURI();
		int authLength = url.length() - uri.length();
		String auth = url.substring(0, authLength);
		return auth + req.getContextPath();
	}
}
