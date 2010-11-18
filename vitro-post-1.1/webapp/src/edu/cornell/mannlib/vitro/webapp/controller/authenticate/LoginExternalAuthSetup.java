/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;

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
	 * The configuration property that points to the external authorization
	 * server.
	 */
	private static final String PROPERTY_EXTERNAL_AUTH_SERVER_URL = "externalAuth.serverUrl";

	/**
	 * The complaint we make if there is no external authorization server
	 * property.
	 */
	private static final Message MESSAGE_NO_EXTERNAL_AUTH_SERVER = new LoginProcessBean.Message(
			"deploy.properties doesn't contain a value for '"
					+ PROPERTY_EXTERNAL_AUTH_SERVER_URL + "'",
			LoginProcessBean.MLevel.ERROR);

	private String extrnalAuthServerUrl;

	/** Get the configuration property. */
	@Override
	public void init() throws ServletException {
		extrnalAuthServerUrl = ConfigurationProperties
				.getProperty(PROPERTY_EXTERNAL_AUTH_SERVER_URL);
	}

	/**
	 * Write down the referring page, record that we are logging in, and
	 * redirect to the external authorization server URL.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		storeTheReferringPage(req);

		if (extrnalAuthServerUrl == null) {
			log.debug("No external authorization server in deploy.properties");
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_NO_EXTERNAL_AUTH_SERVER);
			return;
		}

		LoginProcessBean.getBean(req).setState(
				LoginProcessBean.State.LOGGING_IN);

		log.debug("Sending to external authorization server.");
		resp.sendRedirect(buildExternalAuthRedirectUrl(req));
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

	/** How do we get to the external authorization server and back? */
	private String buildExternalAuthRedirectUrl(HttpServletRequest req) {
		try {
			String returnUrl = figureHomePageUrl(req) + RETURN_SERVLET_URL;
			String encodedReturnUrl = URLEncoder.encode(returnUrl, "UTF-8");
			String externalAuthUrl = extrnalAuthServerUrl + "?target="
					+ encodedReturnUrl;
			log.debug("externalAuthUrl is '" + externalAuthUrl + "'");
			return externalAuthUrl;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); // No UTF-8? Really?
		}
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
