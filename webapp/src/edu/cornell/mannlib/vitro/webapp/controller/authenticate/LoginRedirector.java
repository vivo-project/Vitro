/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;

/**
 * A user has just completed the login process. What page do we direct them to?
 */
public class LoginRedirector {
	private static final Log log = LogFactory.getLog(LoginRedirector.class);

	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private final HttpSession session;

	private final String uriOfAssociatedIndividual;
	private final String afterLoginPage;

	public LoginRedirector(HttpServletRequest request,
			HttpServletResponse response) {
		this.request = request;
		this.session = request.getSession();
		this.response = response;

		uriOfAssociatedIndividual = getAssociatedIndividualUri();

		LoginProcessBean processBean = LoginProcessBean.getBean(request);
		log.debug("process bean is: " + processBean);
		afterLoginPage = processBean.getAfterLoginUrl();
	}

	/** Is there an Individual associated with this user? */
	private String getAssociatedIndividualUri() {
		String username = LoginStatusBean.getBean(request).getUsername();
		if (username == null) {
			log.warn("Not logged in? How did we get here?");
			return null;
		}

		List<String> uris = Authenticator.getInstance(request)
				.getAssociatedIndividualUris(username);
		if (uris.isEmpty()) {
			log.debug("'" + username
					+ "' is not associated with an individual.");
			return null;
		} else {
			String uri = uris.get(0);
			log.debug("'" + username + "' is associated with an individual: "
					+ uri);
			return uri;
		}
	}

	public void redirectLoggedInUser() throws IOException {
		DisplayMessage.setMessage(request, assembleWelcomeMessage());

		try {
			if (isSelfEditorWithIndividual()) {
				log.debug("Going to Individual home page.");
				response.sendRedirect(getAssociatedIndividualHomePage());
			} else if (isMerelySelfEditor()) {
				log.debug("User not recognized. Going to application home.");
				response.sendRedirect(getApplicationHomePageUrl());
			} else {
				if (isLoginPage(afterLoginPage)) {
					log.debug("Coming from /login. Going to site admin page.");
					response.sendRedirect(getSiteAdminPageUrl());
				} else if (null != afterLoginPage) {
					log.debug("Returning to requested page: " + afterLoginPage);
					response.sendRedirect(afterLoginPage);
				} else {
					log.debug("Don't know what to do. Go home.");
					response.sendRedirect(getApplicationHomePageUrl());
				}
			}
			LoginProcessBean.removeBean(request);
		} catch (IOException e) {
			log.debug("Problem with re-direction", e);
			response.sendRedirect(getApplicationHomePageUrl());
		}
	}

	private String assembleWelcomeMessage() {
		if (isMerelySelfEditor() && !isSelfEditorWithIndividual()) {
			// A special message for unrecognized self-editors:
			return "You have logged in, "
					+ "but the system contains no profile for you.";
		}

		LoginStatusBean bean = LoginStatusBean.getBean(request);
		Authenticator auth = Authenticator.getInstance(request);
		User user = auth.getUserByUsername(bean.getUsername());

		String backString = "";
		String greeting = bean.getUsername();

		if (user != null) {
			if (user.getLoginCount() > 1) {
				backString = " back";
			}
			String name = user.getFirstName();
			if ((name != null) && (name.length() > 0)) {
				greeting = name;
			}
		}

		return "Welcome" + backString + ", " + greeting;
	}

	public void redirectCancellingUser() throws IOException {
		try {
			if (isLoginPage(afterLoginPage)) {
				log.debug("Coming from /login. Going to home.");
				response.sendRedirect(getApplicationHomePageUrl());
			} else if (null != afterLoginPage) {
				log.debug("Returning to requested page: " + afterLoginPage);
				response.sendRedirect(afterLoginPage);
			} else {
				log.debug("Don't know what to do. Go home.");
				response.sendRedirect(getApplicationHomePageUrl());
			}
			LoginProcessBean.removeBean(request);
		} catch (IOException e) {
			log.debug("Problem with re-direction", e);
			response.sendRedirect(getApplicationHomePageUrl());
		}
	}

	public void redirectUnrecognizedExternalUser(String username)
			throws IOException {
		log.debug("Redirecting unrecognized external user: " + username);
		DisplayMessage.setMessage(request,
				"VIVO cannot find a profile for your account.");
		response.sendRedirect(getApplicationHomePageUrl());
	}

	private boolean isMerelySelfEditor() {
		RoleLevel role = RoleLevel.getRoleFromLoginStatus(request);
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
