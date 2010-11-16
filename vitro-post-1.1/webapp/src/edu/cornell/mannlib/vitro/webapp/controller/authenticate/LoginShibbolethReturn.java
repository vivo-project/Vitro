/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import static edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginShibbolethSetup.ATTRIBUTE_REFERRER;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;

/**
 * Handle the return from the Shibboleth login server. If we are successful,
 * record the login. Otherwise, display the failure.
 */
public class LoginShibbolethReturn extends BaseLoginServlet {
	private static final Log log = LogFactory
			.getLog(LoginShibbolethReturn.class);

	/** The configuration property that tells what provider name we expect. */
	private static final String PROPERTY_SHIBBOLETH_PROVIDER = "shibboleth.provider";

	/** On return froma Shibboleth login, this header holds the provider name. */
	private static final String HEADING_SHIBBOLETH_PROVIDER = "shib-identity-provider";

	/** On return froma Shibboleth login, this header holds the user name. */
	private static final String HEADING_SHIBBOLETH_USERNAME = "glid";

	private static final Message MESSAGE_LOGIN_FAILED = new LoginProcessBean.Message(
			"Shibboleth login failed.", LoginProcessBean.MLevel.ERROR);

	private static final Message MESSAGE_NO_SUCH_USER = new LoginProcessBean.Message(
			"Shibboleth login succeeded, but user {0} is unknown to VIVO.",
			LoginProcessBean.MLevel.ERROR);

	private static final Message MESSAGE_NO_SHIBBOLETH_PROVIDER = new LoginProcessBean.Message(
			"deploy.properties doesn't contain a value for '"
					+ PROPERTY_SHIBBOLETH_PROVIDER + "'",
			LoginProcessBean.MLevel.ERROR);

	private final LoginRedirector loginRedirector = new LoginRedirector();
	private String shibbolethProvider;

	/** Get the configuration properties. */
	@Override
	public void init() throws ServletException {
		shibbolethProvider = ConfigurationProperties
				.getProperty(PROPERTY_SHIBBOLETH_PROVIDER);
	}

	/**
	 * Returning from the Shibboleth server. If we were successful, the headers
	 * will contain the Shibboleth provider name and the name of the user who
	 * just logged in.
	 * 
	 * We report problems if the provider name is missing or we don't know the
	 * correct value for it. We also report problems if the username is missing
	 * or if we don't recognize that user.
	 * 
	 * If there are no problems, record the login and redirect like we would on
	 * a normal login.
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String provider = req.getHeader(HEADING_SHIBBOLETH_PROVIDER);
		String user = req.getHeader(HEADING_SHIBBOLETH_USERNAME);
		log.debug("Info from Shibboleth: user=" + user + ", provider="
				+ provider);

		if ((provider == null) || (user == null)) {
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_LOGIN_FAILED);
		} else if (shibbolethProvider == null) {
			log.debug("No shibboleth provider in deploy.properties");
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_NO_SHIBBOLETH_PROVIDER);
		} else if (!this.shibbolethProvider.equals(provider)) {
			log.error("Wrong shibboleth provider: " + provider);
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_LOGIN_FAILED);
		} else if (!getAuthenticator(req).isExistingUser(user)) {
			log.debug("No such user: " + user);
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_NO_SUCH_USER, user);
		} else {
			log.debug("Logging in as " + user);
			recordLoginAndRedirect(req, resp, user);
		}
	}

	/** Success. Record the login and send them to the appropriate page. */
	private void recordLoginAndRedirect(HttpServletRequest req,
			HttpServletResponse resp, String username)
			throws UnsupportedEncodingException, IOException {
		getAuthenticator(req).recordUserIsLoggedIn(username);
		LoginProcessBean.removeBean(req);
		req.getSession().removeAttribute(ATTRIBUTE_REFERRER);
		loginRedirector.redirectLoggedInUser(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
