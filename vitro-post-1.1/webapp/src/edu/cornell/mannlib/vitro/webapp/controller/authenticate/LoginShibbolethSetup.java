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
 * Set up the Shibboleth login process.
 * 
 * Write down the page that triggered the request, so we can get back to it.
 * 
 * Send a request to the Shibboleth server that will return us to the
 * LoginShibbolethReturn servlet for further processing.
 */
public class LoginShibbolethSetup extends BaseLoginServlet {
	private static final Log log = LogFactory
			.getLog(LoginShibbolethSetup.class);

	/** This session attribute tells where we came from. */
	static final String ATTRIBUTE_REFERRER = LoginShibbolethSetup.class
			.getName() + ".referrer";

	private static final String RETURN_SERVLET_URL = "/loginShibbolethReturn";

	/** This http header holds the referring page. */
	private static final String HEADING_REFERRER = "referer";

	/** The configuration property that points to the Shibboleth server. */
	private static final String PROPERTY_SHIBBOLETH_SERVER_URL = "shibboleth.server.url";

	/** The complaint we make if there is no Shibbolet server property. */
	private static final Message MESSAGE_NO_SHIBBOLETH_SERVER = new LoginProcessBean.Message(
			"deploy.properties doesn't contain a value for '"
					+ PROPERTY_SHIBBOLETH_SERVER_URL + "'",
			LoginProcessBean.MLevel.ERROR);

	private String shibbolethServerUrl;

	/** Get the configuration property. */
	@Override
	public void init() throws ServletException {
		shibbolethServerUrl = ConfigurationProperties
				.getProperty(PROPERTY_SHIBBOLETH_SERVER_URL);
	}

	/**
	 * Write down the referring page, record that we are logging in, and
	 * redirect to the shib server URL.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Record where we came from, so we can get back there.
		storeTheReferringPage(req);

		// If we have no URL for the Shibboleth server, give up.
		if (shibbolethServerUrl == null) {
			log.debug("No shibboleth server in deploy.properties");
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_NO_SHIBBOLETH_SERVER);
			return;
		}

		// Record that we are in the process of logging in.
		LoginProcessBean.getBean(req).setState(
				LoginProcessBean.State.LOGGING_IN);

		// Hand over to Shibboleth.
		log.debug("Sending to shibboleth server.");
		resp.sendRedirect(buildShibbolethRedirectUrl(req));
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

	/** How do we get to the Shibboleth server and back? */
	private String buildShibbolethRedirectUrl(HttpServletRequest req) {
		try {
			String returnUrl = figureHomePageUrl(req) + RETURN_SERVLET_URL;
			String encodedReturnUrl = URLEncoder.encode(returnUrl, "UTF-8");
			String shibbolethUrl = shibbolethServerUrl + "?target="
					+ encodedReturnUrl;
			log.debug("shibbolethURL is '" + shibbolethUrl + "'");
			return shibbolethUrl;
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
