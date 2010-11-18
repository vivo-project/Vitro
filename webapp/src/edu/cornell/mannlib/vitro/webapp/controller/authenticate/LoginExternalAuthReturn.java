/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import static edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginExternalAuthSetup.ATTRIBUTE_REFERRER;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

/**
 * Handle the return from the external authorization login server. If we are
 * successful, record the login. Otherwise, display the failure.
 */
public class LoginExternalAuthReturn extends BaseLoginServlet {
	private static final Log log = LogFactory
			.getLog(LoginExternalAuthReturn.class);

	/* This configuration property tells us what header contains the username. */
	public static final String PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER = "externalAuth.headerName";

	/** The complaint we make if there is no such property. */
	private static final Message MESSAGE_NO_EXTERNAL_AUTH_USERNAME = new LoginProcessBean.Message(
			"deploy.properties doesn't contain a value for '"
					+ PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER + "'",
			LoginProcessBean.MLevel.ERROR);

	private static final Message MESSAGE_LOGIN_FAILED = new LoginProcessBean.Message(
			"External login failed.", LoginProcessBean.MLevel.ERROR);

	private final LoginRedirector loginRedirector = new LoginRedirector();
	private String externalAuthUsernameHeader;

	/** Get the configuration properties. */
	@Override
	public void init() throws ServletException {
		externalAuthUsernameHeader = ConfigurationProperties
				.getProperty(PROPERTY_EXTERNAL_AUTH_USERNAME_HEADER);
	}

	/**
	 * <pre>
	 * Returning from the external authorization server. If we were successful,
	 * the header will contain the name of the user who just logged in.
	 * 
	 * Deal with these possibilities: 
	 * - The header name was not configured in deploy.properties. Complain.
	 * - No username: the login failed. Complain 
	 * - User corresponds to a User acocunt. Record the login. 
	 * - User corresponds to an Individual (self-editor). 
	 * - User is not recognized.
	 * </pre>
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (externalAuthUsernameHeader == null) {
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_NO_EXTERNAL_AUTH_USERNAME);
			return;
		}

		String username = req.getHeader(externalAuthUsernameHeader);
		String uri = getAssociatedIndividualUri(username, req);

		if (username == null) {
			log.debug("No username.");
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_LOGIN_FAILED);
		} else if (getAuthenticator(req).isExistingUser(username)) {
			log.debug("Logging in as " + username);
			getAuthenticator(req).recordUserIsLoggedIn(username);
			removeLoginProcessArtifacts(req);
			loginRedirector.redirectLoggedInUser(req, resp);
		} else if (uri != null) {
			log.debug("Recognize '' as self-editor for " + uri);
			removeLoginProcessArtifacts(req);
			loginRedirector.redirectSelfEditingUser(req, resp, uri);
		} else {
			log.debug("User is not recognized: " + username);
			removeLoginProcessArtifacts(req);
			loginRedirector.redirectUnrecognizedUser(req, resp, username);
		}
	}

	private String getAssociatedIndividualUri(String username,
			HttpServletRequest req) {
		if (username == null) {
			return null;
		}
		IndividualDao indDao = new VitroRequest(req).getWebappDaoFactory()
				.getIndividualDao();
		return ExternalAuthHelper.getBean(req).getIndividualUriFromNetId(
				indDao, username);
	}

	private void removeLoginProcessArtifacts(HttpServletRequest req) {
		LoginProcessBean.removeBean(req);
		req.getSession().removeAttribute(ATTRIBUTE_REFERRER);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
