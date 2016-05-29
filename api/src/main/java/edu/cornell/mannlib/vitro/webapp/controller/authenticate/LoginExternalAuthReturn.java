/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import static edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginExternalAuthSetup.ATTRIBUTE_REFERRER;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.user.UserAccountsFirstTimeExternalPage;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator.LoginNotPermitted;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;

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
	 * - The header name was not configured in runtime.properties. Complain.
	 * - No username: the login failed. Complain 
	 * - User corresponds to a User acocunt. Record the login. 
	 * - User corresponds to an Individual (self-editor). 
	 * - User is not recognized.
	 * 
	 * On entry, we expect to find:
	 * - A LoginProcessBean, which will give us the afterLoginUrl if the login
	 *      succeeds.
	 * - A referrer URL, to which we will redirect if the login fails.
	 *      TODO: is this equal to LoginProcessBean.getLoginPageUrl()?
	 * These are removed on exit.
	 * </pre>
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (log.isDebugEnabled()) {
			@SuppressWarnings("unchecked")
			Enumeration<String> names = req.getHeaderNames();

			log.debug("------------request:" + req.getRequestURL());
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				log.debug(name + "=" + req.getHeader(name));
			}
		}

		String externalAuthId = ExternalAuthHelper.getHelper(req)
				.getExternalAuthId(req);
		log.debug("externalAuthID='" + externalAuthId + "'");

		if (externalAuthId == null) {
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					messageLoginFailed(req));
			return;
		}

		String afterLoginUrl = LoginProcessBean.getBean(req).getAfterLoginUrl();
		removeLoginProcessArtifacts(req);

		UserAccount userAccount = getAuthenticator(req)
				.getAccountForExternalAuth(externalAuthId);

		if (!getAuthenticator(req).isUserPermittedToLogin(userAccount)) {
			log.debug("Logins disabled for " + userAccount);
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					messageLoginDisabled(req));
			return;
		}

		if (userAccount == null) {
			log.debug("Creating new account for " + externalAuthId
					+ ", return to '" + afterLoginUrl + "'");
			UserAccountsFirstTimeExternalPage.setExternalLoginInfo(req,
					externalAuthId, afterLoginUrl);
			resp.sendRedirect(UrlBuilder.getUrl("/accounts/firstTimeExternal"));
			return;
		}

		try {
			log.debug("Logging in as " + userAccount.getUri());
			getAuthenticator(req).recordLoginAgainstUserAccount(userAccount,
					AuthenticationSource.EXTERNAL);
			new LoginRedirector(req, afterLoginUrl).redirectLoggedInUser(resp);
			return;
		} catch (LoginNotPermitted e) {
			// should have been caught by isUserPermittedToLogin()
			log.debug("Logins disabled for " + userAccount);
			complainAndReturnToReferrer(req, resp, ATTRIBUTE_REFERRER,
					messageLoginDisabled(req));
			return;
		}
	}

	@Override
	protected void complainAndReturnToReferrer(HttpServletRequest req,
			HttpServletResponse resp, String sessionAttributeForReferrer,
			Message message) throws IOException {
		DisplayMessage.setMessage(req, message.getText());
		super.complainAndReturnToReferrer(req, resp,
				sessionAttributeForReferrer, message);
	}

	private void removeLoginProcessArtifacts(HttpServletRequest req) {
		req.getSession().removeAttribute(ATTRIBUTE_REFERRER);
		LoginProcessBean.removeBean(req);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
}
