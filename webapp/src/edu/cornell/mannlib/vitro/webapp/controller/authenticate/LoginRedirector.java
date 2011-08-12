/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasRoleLevel;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsRootUser;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;

/**
 * A user has just completed the login process. What page do we direct them to?
 */
public class LoginRedirector {
	private static final Log log = LogFactory.getLog(LoginRedirector.class);

	private final HttpServletRequest request;
	private final HttpSession session;

	private final String uriOfAssociatedIndividual;
	private final String afterLoginPage;

	public LoginRedirector(HttpServletRequest request, String afterLoginPage) {
		this.request = request;
		this.session = request.getSession();
		this.afterLoginPage = afterLoginPage;

		uriOfAssociatedIndividual = getAssociatedIndividualUri();
	}

	/** Is there an Individual associated with this user? */
	private String getAssociatedIndividualUri() {
		UserAccount userAccount = LoginStatusBean.getCurrentUser(request);
		if (userAccount == null) {
			log.debug("Not logged in? Must be cancelling the password change");
			return null;
		}

		List<String> uris = Authenticator.getInstance(request)
				.getAssociatedIndividualUris(userAccount);
		if (uris.isEmpty()) {
			log.debug("'" + userAccount.getEmailAddress()
					+ "' is not associated with an individual.");
			return null;
		} else {
			String uri = uris.get(0);
			log.debug("'" + userAccount.getEmailAddress()
					+ "' is associated with an individual: " + uri);
			return uri;
		}
	}

	public String getRedirectionUriForLoggedInUser() {
		if (isSelfEditorWithIndividual()) {
			log.debug("Going to Individual home page.");
			return getAssociatedIndividualHomePage();
		} else if (isMerelySelfEditor()) {
			log.debug("User not recognized. Going to application home.");
			return getApplicationHomePageUrl();
		} else {
			if (isLoginPage(afterLoginPage)) {
				log.debug("Coming from /login. Going to site admin page.");
				return getSiteAdminPageUrl();
			} else if (null != afterLoginPage) {
				log.debug("Returning to requested page: " + afterLoginPage);
				return afterLoginPage;
			} else {
				log.debug("Don't know what to do. Go home.");
				return getApplicationHomePageUrl();
			}
		}
	}

	public String getRedirectionUriForCancellingUser() {
		if (isLoginPage(afterLoginPage)) {
			log.debug("Coming from /login. Going to home.");
			return getApplicationHomePageUrl();
		} else if (null != afterLoginPage) {
			log.debug("Returning to requested page: " + afterLoginPage);
			return afterLoginPage;
		} else {
			log.debug("Don't know what to do. Go home.");
			return getApplicationHomePageUrl();
		}
	}

	public void redirectLoggedInUser(HttpServletResponse response)
			throws IOException {
		try {
			DisplayMessage.setMessage(request, assembleWelcomeMessage());
			response.sendRedirect(getRedirectionUriForLoggedInUser());
		} catch (IOException e) {
			log.debug("Problem with re-direction", e);
			response.sendRedirect(getApplicationHomePageUrl());
		}
	}

	public String assembleWelcomeMessage() {
		if (isMerelySelfEditor() && !isSelfEditorWithIndividual()) {
			// A special message for unrecognized self-editors:
			return "You have logged in, "
					+ "but the system contains no profile for you.";
		}

		String backString = "";
		String greeting = "";

		UserAccount userAccount = LoginStatusBean.getCurrentUser(request);
		if (userAccount != null) {
			greeting = userAccount.getEmailAddress();
			if (userAccount.getLoginCount() > 1) {
				backString = " back";
			}
			String name = userAccount.getFirstName();
			if (!StringUtils.isEmpty(name)) {
				greeting = name;
			}
		}

		return "Welcome" + backString + ", " + greeting;
	}

	public void redirectCancellingUser(HttpServletResponse response)
			throws IOException {
		try {
			response.sendRedirect(getRedirectionUriForCancellingUser());
		} catch (IOException e) {
			log.debug("Problem with re-direction", e);
			response.sendRedirect(getApplicationHomePageUrl());
		}
	}

	private boolean isMerelySelfEditor() {
		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(request);
		if (IsRootUser.isRootUser(ids)) {
			return false;
		}
		
		RoleLevel role = HasRoleLevel.getUsersRoleLevel(ids);
		return role == RoleLevel.PUBLIC || role == RoleLevel.SELF;
	}

	private boolean isLoginPage(String page) {
		return ((page != null) && page.endsWith(request.getContextPath()
				+ Controllers.LOGIN));
	}

	private String getSiteAdminPageUrl() {
		String contextPath = request.getContextPath();
		return contextPath + Controllers.SITE_ADMIN;
	}

	private boolean isSelfEditorWithIndividual() {
		return uriOfAssociatedIndividual != null;
	}

	private String getAssociatedIndividualHomePage() {
		try {
			return request.getContextPath() + "/individual?uri="
					+ URLEncoder.encode(uriOfAssociatedIndividual, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("No UTF-8 encoding? Really?", e);
		}
	}

	/**
	 * The application home page can be overridden by an attribute in the
	 * ServletContext. Further, it can either be an absolute URL, or it can be
	 * relative to the application. Weird.
	 */
	private String getApplicationHomePageUrl() {
		String contextRedirect = (String) session.getServletContext()
				.getAttribute("postLoginRequest");
		if (contextRedirect != null) {
			if (contextRedirect.indexOf(":") == -1) {
				return request.getContextPath() + contextRedirect;
			} else {
				return contextRedirect;
			}
		}
		return request.getContextPath();
	}
}
