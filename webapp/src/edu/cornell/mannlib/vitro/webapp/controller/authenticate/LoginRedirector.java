/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;

/**
 * A user has just completed the login process. What page do we direct them to?
 */
public class LoginRedirector {
	private static final Log log = LogFactory.getLog(LoginRedirector.class);

	private static final String ATTRIBUTE_RETURN_FROM_FORCED_LOGIN = "return_from_forced_login";

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final HttpSession session;

	private final String urlOfRestrictedPage;
	private final String uriOfAssociatedIndividual;

	public LoginRedirector(HttpServletRequest request,
			HttpServletResponse response) {
		this.request = request;
		this.session = request.getSession();
		this.response = response;

		urlOfRestrictedPage = getUrlOfRestrictedPage();
		uriOfAssociatedIndividual = getAssociatedIndividualUri();
	}

	/** Were we forced to log in when trying to access a restricted page? */
	private String getUrlOfRestrictedPage() {
		String url = (String) session
				.getAttribute(ATTRIBUTE_RETURN_FROM_FORCED_LOGIN);
		session.removeAttribute(ATTRIBUTE_RETURN_FROM_FORCED_LOGIN);
		log.debug("URL of restricted page is " + url);
		return url;

	}

	/** Is there an Individual associated with this user? */
	private String getAssociatedIndividualUri() {
		String username = LoginStatusBean.getBean(request).getUsername();
		if (username == null) {
			log.warn("Not logged in? How did we get here?");
			return null;
		}

		String uri = Authenticator.getInstance(request)
				.getAssociatedIndividualUri(username);
		log.debug("URI of associated individual is " + uri);
		return uri;
	}

	public void redirectLoggedInUser() throws IOException {
		if (isForcedFromRestrictedPage()) {
			log.debug("Returning to restricted page.");
			response.sendRedirect(urlOfRestrictedPage);
		} else if (isUserEditorOrBetter()) {
			log.debug("Going to site admin page.");
			response.sendRedirect(getSiteAdminPageUrl());
		} else if (isSelfEditorWithIndividual()) {
			log.debug("Going to Individual home page.");
			response.sendRedirect(getAssociatedIndividualHomePage());
		} else {
			log.debug("User not recognized. Going to application home.");
			DisplayMessage.setMessage(request, "You have logged in, "
					+ "but the system contains no profile for you.");
			response.sendRedirect(getApplicationHomePageUrl());
		}
	}

	private boolean isForcedFromRestrictedPage() {
		return urlOfRestrictedPage != null;
	}

	private boolean isUserEditorOrBetter() {
		return LoginStatusBean.getBean(session).isLoggedInAtLeast(
				LoginStatusBean.EDITOR);
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

	public void redirectUnrecognizedExternalUser(String username)
			throws IOException {
		log.debug("Redirecting unrecognized external user: " + username);
		DisplayMessage.setMessage(request,
				"VIVO cannot find a profile for your account.");
		response.sendRedirect(getApplicationHomePageUrl());
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

	// ----------------------------------------------------------------------
	// static helper methods
	// ----------------------------------------------------------------------

	public static void setReturnUrlFromForcedLogin(HttpServletRequest request,
			String url) {
		request.getSession().setAttribute(ATTRIBUTE_RETURN_FROM_FORCED_LOGIN,
				url);
	}
}
