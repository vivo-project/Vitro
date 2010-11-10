/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;

/**
 * This servlet acts as the interface to the Shibboleth authentication server.
 * 
 * If the request has the "setup" property, it is coming from the Vivo login
 * screen and going to the Shibboleth server.
 * 
 * Otherwise, the request is coming from the Shibboleth server and going back to
 * Vivo.
 */
public class LoginShibboleth extends HttpServlet {
	private static final Log log = LogFactory.getLog(LoginShibboleth.class);

	private static final String CLASSNAME = LoginShibboleth.class.getName();

	/** This session attribute tells where we came from. */
	private static final String ATTRIBUTE_REFERRER = CLASSNAME + ".referrer";

	/** This request parameter indicates that we are setting up the login. */
	private static final String PARAMETER_SETUP = "setup";

	/** This http header holds the referring page. */
	private static final String HEADING_REFERRER = "referer";

	/** On return froma Shibboleth login, this header holds the provider name. */
	private static final String HEADING_SHIBBOLETH_PROVIDER = "shib-identity-provider";

	/** On return froma Shibboleth login, this header holds the user name. */
	private static final String HEADING_SHIBBOLETH_USERNAME = "glid";

	/** The configuration property that points to the Shibboleth server. */
	private static final String PROPERTY_SHIBBOLETH_SERVER_URL = "shibboleth.server.url";

	/** The configuration property that tells what provider name we expect. */
	private static final String PROPERTY_SHIBBOLETH_PROVIDER = "shibboleth.provider";

	private static final Message MESSAGE_NO_SHIBBOLETH_SERVER = new LoginProcessBean.Message(
			"deploy.properties doesn't contain a value for '"
					+ PROPERTY_SHIBBOLETH_SERVER_URL + "'",
			LoginProcessBean.MLevel.ERROR);

	private static final Message MESSAGE_NO_SHIBBOLETH_PROVIDER = new LoginProcessBean.Message(
			"deploy.properties doesn't contain a value for '"
					+ PROPERTY_SHIBBOLETH_PROVIDER + "'",
			LoginProcessBean.MLevel.ERROR);

	private static final Message MESSAGE_LOGIN_FAILED = new LoginProcessBean.Message(
			"Shibboleth login failed.", LoginProcessBean.MLevel.ERROR);

	private static final Message MESSAGE_NO_SUCH_USER = new LoginProcessBean.Message(
			"Shibboleth login succeeded, but user {0} is unknown to VIVO.",
			LoginProcessBean.MLevel.ERROR);

	private static final String ERROR_NO_PARAMETERS = "Likely error in the template: "
			+ "'setup' parameter was not found, "
			+ "but there was no info from the Shibboleth server either.";

	private final LoginRedirector loginRedirector = new LoginRedirector();

	private String shibbolethServerUrl;
	private String shibbolethProvider;
	private static boolean isFirstCallToServlet = true;

	/** Get the configuration properties. */
	@Override
	public void init() throws ServletException {
		shibbolethServerUrl = ConfigurationProperties
				.getProperty(PROPERTY_SHIBBOLETH_SERVER_URL);
		shibbolethProvider = ConfigurationProperties
				.getProperty(PROPERTY_SHIBBOLETH_PROVIDER);
	}

	/**
	 * <pre>
	 * The first request to this servlet must include the setup parameter. 
	 *      If it doesn't, it is totally bogus and will cause a complaint.
	 *      This means that the login form isn't coded correctly, 
	 *      	We try to notify the sysadmin in a meaningful way.
	 *      
	 * On setup, write down the referring page and redirect to the shib server URL
	 * 		a URL that comes from the deploy.properties via the widget code.
	 * 			if no such property, set error message and return to referring page.
	 * 		it returns to this page
	 * 
	 * Not on setup
	 * 		check for the site name and the username
	 *      if either is missing,
	 * 			return to the referring page with an error message: login failed.
	 * 		if both there and the provider is wrong
	 * 			return to the referring page with an error message: login failed.
	 *      if both there and the user doesn't exist
	 * 			return to the referring page with an error message: no such user.
	 * 		otherwise (successful)
	 * 			record the login and redirect like we would on a normal login.
	 * </pre>
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		setupLoginProcessBean(req);

		boolean setupParmIsSet = checkSetupParameterIsSet(req);

		if (requestIsTotallyBogus(setupParmIsSet)) {
			resp.sendError(500, ERROR_NO_PARAMETERS);
		} else if (setupParmIsSet) {
			settingUpShibbolethLogin(req, resp);
		} else {
			returningFromShibbolethLogin(req, resp);
		}
	}

	/** Record that the login is in progress. */
	private void setupLoginProcessBean(HttpServletRequest req) {
		LoginProcessBean bean = LoginProcessBean.getBean(req);
		bean.setState(LoginProcessBean.State.LOGGING_IN);
	}

	/** Does the request contain a "setup" parameter? */
	private boolean checkSetupParameterIsSet(HttpServletRequest req) {
		String setupParm = req.getParameter(PARAMETER_SETUP);
		log.debug("setup=" + setupParm);
		if ((setupParm == null) || setupParm.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/** If the first call doesn't include "setup", the template is broken. */
	private boolean requestIsTotallyBogus(boolean setupParmIsSet) {
		boolean bogosity = isFirstCallToServlet && !setupParmIsSet;
		isFirstCallToServlet = false;
		return bogosity;
	}

	/** On setup, hand over to the Shibboleth server. */
	private void settingUpShibbolethLogin(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		storeTheReferringPage(req);
		if (shibbolethServerUrl == null) {
			log.debug("No shibboleth server in deploy.properties");
			complainAndReturnToReferrer(req, resp, MESSAGE_NO_SHIBBOLETH_SERVER);
		} else if (shibbolethProvider == null) {
			log.debug("No shibboleth provider in deploy.properties");
			complainAndReturnToReferrer(req, resp,
					MESSAGE_NO_SHIBBOLETH_PROVIDER);
		} else {
			log.debug("Sending to shibboleth server.");
			resp.sendRedirect(buildShibbolethRedirectUrl(req));
		}
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
			String returnUrl = req.getRequestURL().toString();
			String encodedReturnUrl = URLEncoder.encode(returnUrl, "UTF-8");
			String shibbolethUrl = shibbolethServerUrl + "?target="
					+ encodedReturnUrl;
			log.debug("shibbolethURL is '" + shibbolethUrl + "'");
			return shibbolethUrl;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); // No UTF-8? Really?
		}
	}

	/** On return from the Shibboleth server, try to apply the results. */
	private void returningFromShibbolethLogin(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		String provider = req.getHeader(HEADING_SHIBBOLETH_PROVIDER);
		String user = req.getHeader(HEADING_SHIBBOLETH_USERNAME);
		log.debug("Info from Shibboleth: user=" + user + ", provider="
				+ provider);

		if ((provider == null) || (user == null)) {
			complainAndReturnToReferrer(req, resp, MESSAGE_LOGIN_FAILED);
		} else if (!this.shibbolethProvider.equals(provider)) {
			log.error("Wrong shibboleth provider: " + provider);
			complainAndReturnToReferrer(req, resp, MESSAGE_LOGIN_FAILED);
		} else if (!getAuthenticator(req).isExistingUser(user)) {
			log.debug("No such user: " + user);
			complainAndReturnToReferrer(req, resp, MESSAGE_NO_SUCH_USER, user);
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
		req.getSession().removeAttribute(ATTRIBUTE_REFERRER);
		loginRedirector.redirectLoggedInUser(req, resp);
	}

	/** Store an error message in the login bean and go back where we came from. */
	private void complainAndReturnToReferrer(HttpServletRequest req,
			HttpServletResponse resp, Message message, Object... args)
			throws IOException {
		log.debug(message.getMessageLevel() +": "+ message.formatMessage(args));
		LoginProcessBean.getBean(req).setMessage(message, args);

		String referrer = (String) req.getSession().getAttribute(
				ATTRIBUTE_REFERRER);
		log.debug("returning to referrer: " + referrer);
		if (referrer == null) {
			referrer = figureHomePageUrl(req);
			log.debug("returning to home page: " + referrer);
		}

		req.getSession().removeAttribute(ATTRIBUTE_REFERRER);
		resp.sendRedirect(referrer);
	}

	private String figureHomePageUrl(HttpServletRequest req) {
		StringBuffer url = req.getRequestURL();
		String uri = req.getRequestURI();
		int authLength = url.length() - uri.length();
		String auth = url.substring(0, authLength);
		return auth + req.getContextPath();
	}

	private Authenticator getAuthenticator(HttpServletRequest req) {
		return Authenticator.getInstance(req);
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
