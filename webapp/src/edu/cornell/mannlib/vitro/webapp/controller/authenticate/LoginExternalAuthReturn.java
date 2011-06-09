/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import static edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginExternalAuthSetup.ATTRIBUTE_REFERRER;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Handle the return from the external authorization login server. If we are
 * successful, record the login. Otherwise, display the failure.
 */
public class LoginExternalAuthReturn extends BaseLoginServlet {
	private static final Log log = LogFactory
			.getLog(LoginExternalAuthReturn.class);

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
		String externalAuthId = ExternalAuthHelper.getHelper(req)
				.getExternalAuthId(req);
		if (externalAuthId == null) {
			log.debug("No externalAuthId.");
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					MESSAGE_LOGIN_FAILED);
			return;
		}

		UserAccount userAccount = getAuthenticator(req)
				.getAccountForExternalAuth(externalAuthId);
		if (userAccount != null) {
			log.debug("Logging in as " + userAccount.getUri());
			getAuthenticator(req).recordLoginAgainstUserAccount(userAccount,
					AuthenticationSource.EXTERNAL);
			removeLoginProcessArtifacts(req);
			new LoginRedirector(req, resp).redirectLoggedInUser();
			return;
		}

		List<String> associatedUris = getAuthenticator(req)
				.getAssociatedIndividualUris(userAccount);
		// TODO JB - this case should lead to creating a new account.
		if (!associatedUris.isEmpty()) {
			log.debug("Recognize '" + externalAuthId + "' as self-editor for "
					+ associatedUris);
			String uri = associatedUris.get(0);

			getAuthenticator(req).recordLoginWithoutUserAccount(uri);
			removeLoginProcessArtifacts(req);
			new LoginRedirector(req, resp).redirectLoggedInUser();
			return;
		}

		log.debug("User is not recognized: " + externalAuthId);
		removeLoginProcessArtifacts(req);
		new LoginRedirector(req, resp)
				.redirectUnrecognizedExternalUser(externalAuthId);
	}

	private void removeLoginProcessArtifacts(HttpServletRequest req) {
		req.getSession().removeAttribute(ATTRIBUTE_REFERRER);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
