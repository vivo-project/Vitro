/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RoleBasedPolicy.AuthRole;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;

/**
 * A user has just completed the login process. What page do we direct them to?
 */
public class LoginRedirector {
	private static final Log log = LogFactory.getLog(LoginRedirector.class);

	public void redirectSelfEditingUser(HttpServletRequest request,
			HttpServletResponse response, String uri) throws IOException {
		String userHomePage = assembleUserHomePageUrl(request, uri);
		log.debug("Redirecting self-editor to " + userHomePage);
		response.sendRedirect(userHomePage);
	}

	public void redirectUnrecognizedUser(HttpServletRequest request,
			HttpServletResponse response, String username) throws IOException {
		log.debug("Redirecting unrecognized user: " + username);
		response.sendRedirect(request.getContextPath()
				+ "/unrecognizedUser?username=" + username);
	}

	/**
	 * <pre>
	 * The user is logged in. They might go to:
	 * - A one-time redirect, stored in the session, if they had tried to
	 *     bookmark to a page that requires login.
	 * - An application-wide redirect, stored in the servlet context.
	 * - Their home page, if they are a self-editor.
	 * - The site admin page.
	 * </pre>
	 */
	public void redirectLoggedInUser(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// Did they have a one-time redirect stored on the session?
		String sessionRedirect = (String) request.getSession().getAttribute(
				"postLoginRequest");
		if (sessionRedirect != null) {
			request.getSession().removeAttribute("postLoginRequest");
			log.debug("User is logged in. Redirect by session to "
					+ sessionRedirect);
			response.sendRedirect(sessionRedirect);
			return;
		}

		// Is there a login-redirect stored in the application as a whole?
		// It could lead to another page in this app, or to any random URL.
		String contextRedirect = (String) request.getSession()
				.getServletContext().getAttribute("postLoginRequest");
		if (contextRedirect != null) {
			log.debug("User is logged in. Redirect by application to "
					+ contextRedirect);
			if (contextRedirect.indexOf(":") == -1) {
				response.sendRedirect(request.getContextPath()
						+ contextRedirect);
			} else {
				response.sendRedirect(contextRedirect);
			}
			return;
		}

		// If the user is a self-editor, send them to their home page.
		User user = getLoggedInUser(request);
		if (userIsANonEditor(user)) {
			List<String> uris = getAuthenticator(request)
					.asWhomMayThisUserEdit(user);
			if (uris != null && uris.size() > 0) {
				String userHomePage = assembleUserHomePageUrl(request,
						uris.get(0));
				log.debug("User is logged in. Redirect as self-editor to "
						+ userHomePage);
				response.sendRedirect(userHomePage);
				return;
			}
		}

		// If nothing else applies, send them to the Site Admin page.
		log.debug("User is logged in. Redirect to site admin page.");
		response.sendRedirect(getSiteAdminUrl(request));
	}

	/** Is the logged in user an AuthRole.USER? */
	private boolean userIsANonEditor(User user) {
		if (user == null) {
			return false;
		}
		String nonEditorRoleUri = Integer.toString(AuthRole.USER.level());
		return nonEditorRoleUri.equals(user.getRoleURI());
	}

	/**
	 * What user are we logged in as?
	 */
	private User getLoggedInUser(HttpServletRequest request) {
		LoginStatusBean bean = LoginStatusBean.getBean(request);
		if (!bean.isLoggedIn()) {
			log.debug("getLoggedInUser: not logged in");
			return null;
		}
		return getAuthenticator(request).getUserByUsername(bean.getUsername());
	}

	/** What's the URL for the site admin screen? */
	private String getSiteAdminUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		return contextPath + Controllers.SITE_ADMIN;
	}

	/** Get a reference to the Authenticator. */
	private Authenticator getAuthenticator(HttpServletRequest request) {
		return Authenticator.getInstance(request);
	}

	private String assembleUserHomePageUrl(HttpServletRequest request,
			String uri) throws UnsupportedEncodingException {
		return request.getContextPath() + "/individual?uri="
				+ URLEncoder.encode(uri, "UTF-8");
	}
}
