/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import static edu.cornell.mannlib.vitro.webapp.beans.UserAccount.MAX_PASSWORD_LENGTH;
import static edu.cornell.mannlib.vitro.webapp.beans.UserAccount.MIN_PASSWORD_LENGTH;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.FORCED_PASSWORD_CHANGE;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGED_IN;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.LOGGING_IN;
import static edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State.NOWHERE;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginInProcessFlag;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.LoginRedirector;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.Message;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LoginLogoutEvent;

public class Authenticate extends VitroHttpServlet {
	private static final Log log = LogFactory.getLog(Authenticate.class
			.getName());

	/**
	 * If this is set at any point in the process, store it as the post-login
	 * destination.
	 * 
	 * NOTE: we expect URL-encoding on this parameter, and will decode it when
	 * we read it.
	 */
	private static final String PARAMETER_AFTER_LOGIN = "afterLogin";

	/**
	 * If this is set at any point in the process, store the referrer as the
	 * post-login destination.
	 */
	private static final String PARAMETER_RETURN = "return";

	/** The username field on the login form. */
	private static final String PARAMETER_USERNAME = "loginName";

	/** The password field on the login form. */
	private static final String PARAMETER_PASSWORD = "loginPassword";

	/** The new password field on the password change form. */
	private static final String PARAMETER_NEW_PASSWORD = "newPassword";

	/** The confirm password field on the password change form. */
	private static final String PARAMETER_CONFIRM_PASSWORD = "confirmPassword";

	/** If this parameter is "true" (ignoring case), cancel the login. */
	private static final String PARAMETER_CANCEL = "cancel";

	/** If this parameter is set, we are not NOWHERE. */
	private static final String PARAMETER_LOGIN_FORM = "loginForm";

	/** Where do we find the User/Session map in the servlet context? */
	public static final String USER_SESSION_MAP_ATTR = "userURISessionMap";

	/**
	 * Find out where they are in the login process, process any input, record
	 * the new state, and show the next page.
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {

		VitroRequest vreq = new VitroRequest(request);

		try {
			if (loginProcessIsRestarting(vreq)) {
				LoginProcessBean.removeBean(vreq);
			}
			if (loginProcessPagesAreEmpty(vreq)) {
				recordLoginProcessPages(vreq);
			}

			// Where do we stand in the process?
			State entryState = getCurrentLoginState(vreq);
			dumpStateToLog("entry", entryState, vreq);

			// Act on any input.
			switch (entryState) {
			case NOWHERE:
				processInputNowhere(vreq);
				break;
			case LOGGING_IN:
				processInputLoggingIn(vreq);
				break;
			case FORCED_PASSWORD_CHANGE:
				processInputChangePassword(vreq);
				break;
			default: // LOGGED_IN:
				processInputLoggedIn(vreq);
				break;
			}

			// Now where do we stand?
			State exitState = getCurrentLoginState(vreq);
			dumpStateToLog("exit", exitState, vreq);

			// Send them on their way.
			switch (exitState) {
			case NOWHERE:
				showLoginCanceled(response, vreq);
				break;
			case LOGGING_IN:
				showLoginScreen(vreq, response);
				break;
			case FORCED_PASSWORD_CHANGE:
				showLoginScreen(vreq, response);
				break;
			default: // LOGGED_IN:
				showLoginComplete(response, vreq);
				break;
			}
		} catch (Exception e) {
			showSystemError(e, response);
		}

	}

	/**
	 * The after-login page or the return flag are supplied only on the first
	 * step in the process. If we see either of them, we conclude that the user
	 * has re-started the login.
	 */
	private boolean loginProcessIsRestarting(HttpServletRequest request) {
		if (isAfterLoginParameterSet(request)) {
			log.debug("after-login parameter is set: restarting the login.");
			return true;
		}
		if (isReturnParameterSet(request)) {
			log.debug("return parameter is set: restarting the login.");
			return true;
		}
		return false;
	}

	/**
	 * Once these URLs have been set, don't change them.
	 */
	private boolean loginProcessPagesAreEmpty(HttpServletRequest request) {
		LoginProcessBean bean = LoginProcessBean.getBean(request);
		return ((bean.getAfterLoginUrl() == null) && (bean.getLoginPageUrl() == null));
	}

	/**
	 * If they supply an after-login page, record it and use the Login page for
	 * the process. Note that we expect it to be URL-encoded.
	 * 
	 * If they supply a return flag, record the current page as the after-login
	 * page and use the Login page for the process.
	 * 
	 * Otherwise, use the current page for the process.
	 * 
	 * The "current page" is the referrer, unless there is no referrer for some
	 * reason. In that case, pretend it's the login page.
	 */
	private void recordLoginProcessPages(HttpServletRequest request) {
		LoginProcessBean bean = LoginProcessBean.getBean(request);

		String afterLoginUrl = decodeAfterLoginParameter(request);
		boolean doReturn = isReturnParameterSet(request);
		String referrer = whereDidWeComeFrom(request);

		if (afterLoginUrl != null) {
			bean.setAfterLoginUrl(afterLoginUrl);
			bean.setLoginPageUrl(request.getContextPath() + Controllers.LOGIN);
		} else if (doReturn) {
			bean.setAfterLoginUrl(referrer);
			bean.setLoginPageUrl(request.getContextPath() + Controllers.LOGIN);
		} else {
			bean.setAfterLoginUrl(referrer);
			bean.setLoginPageUrl(referrer);
		}
	}

	private String decodeAfterLoginParameter(HttpServletRequest request) {
		String parm = request.getParameter(PARAMETER_AFTER_LOGIN);
		if (parm == null) {
			return null;
		} else {
			try {
				return URLDecoder.decode(parm, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("No UTF-8 encoding? Really?", e);
				return parm;
			}
		}
	}

	private boolean isAfterLoginParameterSet(HttpServletRequest request) {
		return (null != request.getParameter(PARAMETER_AFTER_LOGIN));
	}

	private boolean isReturnParameterSet(HttpServletRequest request) {
		return (null != request.getParameter(PARAMETER_RETURN));
	}

	/** If no referrer, say we were on the login page. */
	private String whereDidWeComeFrom(HttpServletRequest request) {
		String referrer = request.getHeader("referer");
		if (referrer != null) {
			return referrer;
		} else {
			return request.getContextPath() + Controllers.LOGIN;
		}
	}

	/**
	 * Where are we in the process? Logged in? Not? Somewhere in between?
	 */
	private State getCurrentLoginState(HttpServletRequest request) {
		State currentState;

		HttpSession session = request.getSession(false);
		if (session == null) {
			currentState = NOWHERE;
			log.debug("no session: current state is NOWHERE");
		} else if (LoginStatusBean.getBean(request).isLoggedIn()) {
			currentState = LOGGED_IN;
			log.debug("found a LoginStatusBean: current state is LOGGED IN");
		} else if (LoginProcessBean.isBean(request)) {
			currentState = LoginProcessBean.getBean(request).getState();
			log.debug("state from LoginProcessBean is " + currentState);
		} else {
			currentState = NOWHERE;
			log.debug("no LoginSessionBean, no LoginProcessBean: "
					+ "current state is NOWHERE");
		}

		if (weCameFromAColdWidget(request, currentState)) {
			currentState = actLikeWeWereLoggingIn(request);
		}

		return currentState;
	}

	/**
	 * If they submitted the login form, they shouldn't be NOWHERE.
	 */
	private boolean weCameFromAColdWidget(HttpServletRequest request,
			State currentState) {
		if (currentState == NOWHERE) {
			if (null != request.getParameter(PARAMETER_LOGIN_FORM)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * They got here by submitting the login form. They should be treated as
	 * already logging in.
	 */
	private State actLikeWeWereLoggingIn(HttpServletRequest request) {
		LoginProcessBean bean = new LoginProcessBean();
		bean.setState(LOGGING_IN);
		bean.setLoginPageUrl(whereDidWeComeFrom(request));
		bean.setAfterLoginUrl(whereDidWeComeFrom(request));
		LoginProcessBean.setBean(request, bean);

		return LOGGING_IN;
	}

	/**
	 * They just got here. Start the process.
	 */
	private void processInputNowhere(HttpServletRequest request) {
		transitionToLoggingIn(request);
	}

	/**
	 * They are logging in. If they get it wrong, let them know. If they get it
	 * right, record it.
	 */
	private void processInputLoggingIn(HttpServletRequest request) {
		String username = request.getParameter(PARAMETER_USERNAME);
		String password = request.getParameter(PARAMETER_PASSWORD);

		LoginProcessBean bean = LoginProcessBean.getBean(request);
		bean.clearMessage();
		log.trace("username=" + username + ", password=" + password + ", bean="
				+ bean);

		if ((username == null) || username.isEmpty()) {
			bean.setMessage(Message.NO_USERNAME);
			return;
		}

		bean.setUsername(username);

		UserAccount user = getAuthenticator(request).getAccountForInternalAuth(
				username);
		log.trace("User is " + (user == null ? "null" : user.getUri()));

		if (user == null) {
			bean.setMessage(Message.UNKNOWN_USERNAME, username);
			return;
		}

		if ((password == null) || password.isEmpty()) {
			bean.setMessage(Message.NO_PASSWORD);
			return;
		}

		if (!getAuthenticator(request).isCurrentPassword(user, password)) {
			bean.setMessage(Message.INCORRECT_PASSWORD);
			return;
		}

		// Username and password are correct. What next?
		if (user.isPasswordChangeRequired()) {
			transitionToForcedPasswordChange(request);
		} else {
			transitionToLoggedIn(request, user);
		}
	}

	/**
	 * <pre>
	 * They are changing passwords. 
	 * - If they cancel, let them out without checking for problems. 
	 * - Otherwise, 
	 *   - If they get it wrong, let them know. 
	 *   - If they get it right, record it.
	 * </pre>
	 */
	private void processInputChangePassword(HttpServletRequest request) {
		String newPassword = request.getParameter(PARAMETER_NEW_PASSWORD);
		String confirm = request.getParameter(PARAMETER_CONFIRM_PASSWORD);
		String cancel = request.getParameter(PARAMETER_CANCEL);

		if (Boolean.valueOf(cancel)) {
			// It's over, man. Let them go.
			transitionToNowhere(request);
			return;
		}

		LoginProcessBean bean = LoginProcessBean.getBean(request);
		bean.clearMessage();
		log.trace("newPassword=" + newPassword + ", confirm=" + confirm
				+ ", bean=" + bean);

		if ((newPassword == null) || newPassword.isEmpty()) {
			bean.setMessage(Message.NO_NEW_PASSWORD);
			return;
		}

		if (!newPassword.equals(confirm)) {
			bean.setMessage(Message.MISMATCH_PASSWORD);
			return;
		}

		if ((newPassword.length() < MIN_PASSWORD_LENGTH)
				|| (newPassword.length() > MAX_PASSWORD_LENGTH)) {
			bean.setMessage(Message.PASSWORD_LENGTH, MIN_PASSWORD_LENGTH,
					MAX_PASSWORD_LENGTH);
			return;
		}

		String username = bean.getUsername();

		UserAccount user = getAuthenticator(request).getAccountForInternalAuth(
				username);
		if (getAuthenticator(request).isCurrentPassword(user, newPassword)) {
			bean.setMessage(Message.USING_OLD_PASSWORD);
			return;
		}

		// New password is acceptable. Store it and go on.
		transitionToLoggedIn(request, user, newPassword);
	}

	/**
	 * They are already logged in.
	 */
	@SuppressWarnings("unused")
	private void processInputLoggedIn(HttpServletRequest request) {
		// Nothing to do. No transition.
	}

	/**
	 * State change: they are starting the login process.
	 */
	private void transitionToLoggingIn(HttpServletRequest request) {
		log.debug("Starting the login process.");
		LoginProcessBean.getBean(request).setState(LOGGING_IN);
	}

	/**
	 * State change: username and password were correct, but now we require a
	 * new password.
	 */
	private void transitionToForcedPasswordChange(HttpServletRequest request) {
		log.debug("Forcing first-time password change");
		LoginProcessBean.getBean(request).setState(FORCED_PASSWORD_CHANGE);
	}

	/**
	 * State change: all requirements are satisfied. Log them in.
	 */
	private void transitionToLoggedIn(HttpServletRequest request,
			UserAccount user) {
		log.debug("Completed login: " + user.getEmailAddress());
		getAuthenticator(request).recordLoginAgainstUserAccount(user,
				AuthenticationSource.INTERNAL);
	}

	/**
	 * State change: all requirements are satisfied. Change their password and
	 * log them in.
	 */
	private void transitionToLoggedIn(HttpServletRequest request,
			UserAccount user, String newPassword) {
		log.debug("Completed login: " + user.getEmailAddress()
				+ ", password changed.");
		getAuthenticator(request).recordNewPassword(user, newPassword);
		getAuthenticator(request).recordLoginAgainstUserAccount(user,
				AuthenticationSource.INTERNAL);
	}

	/**
	 * State change: they decided to cancel the login.
	 */
	private void transitionToNowhere(HttpServletRequest request) {
		LoginProcessBean.getBean(request).setState(NOWHERE);
		log.debug("Cancelling login.");
	}

	/**
	 * Exit: there has been an unexpected exception, so show it.
	 */
	private void showSystemError(Exception e, HttpServletResponse response) {
		log.error("Unexpected error in login process", e);
		try {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e1) {
			log.error(e1, e1);
		}
	}

	/**
	 * Exit: user is still logging in, so go back to the page they were on.
	 */
	private void showLoginScreen(VitroRequest vreq, HttpServletResponse response)
			throws IOException {
		log.debug("logging in.");

		LoginInProcessFlag.set(vreq);

		String loginProcessPage = LoginProcessBean.getBean(vreq)
				.getLoginPageUrl();
		response.sendRedirect(loginProcessPage);
		return;
	}
	
	/**
	 * Exit: user has completed the login. Redirect appropriately and clear the bean.
	 */
	private void showLoginComplete(HttpServletResponse response,
			VitroRequest vreq) throws IOException {
		getLoginRedirector(vreq).redirectLoggedInUser(response);
		LoginProcessBean.removeBean(vreq);
	}

	/**
	 * Exit: user has canceled. Redirect and clear the bean.
	 */
	private void showLoginCanceled(HttpServletResponse response,
			VitroRequest vreq) throws IOException {
		getLoginRedirector(vreq).redirectCancellingUser(response);
		LoginProcessBean.removeBean(vreq);
	}

	private LoginRedirector getLoginRedirector(VitroRequest vreq) {
		String afterLoginUrl = LoginProcessBean.getBean(vreq).getAfterLoginUrl();
		return new LoginRedirector(vreq, afterLoginUrl);
	}



	/** Get a reference to the Authenticator. */
	private Authenticator getAuthenticator(HttpServletRequest request) {
		return Authenticator.getInstance(request);
	}

	// ----------------------------------------------------------------------
	// Public utility methods.
	// ----------------------------------------------------------------------

	/**
	 * The servlet context should contain a map from User URIs to
	 * {@link HttpSession}s. Get a reference to it, creating it if necessary.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, HttpSession> getUserURISessionMapFromContext(
			ServletContext ctx) {
		Map<String, HttpSession> m = (Map<String, HttpSession>) ctx
				.getAttribute(USER_SESSION_MAP_ATTR);
		if (m == null) {
			m = new HashMap<String, HttpSession>();
			ctx.setAttribute(USER_SESSION_MAP_ATTR, m);
		}
		return m;
	}

	/**
	 * Let everyone know that somebody has logged in or logged out.
	 */
	public static void sendLoginNotifyEvent(LoginLogoutEvent event,
			ServletContext context, HttpSession session) {
		if (event == null) {
			log.warn("Unable to notify audit model of login "
					+ "because a null event was passed");
			return;
		}

		OntModel jenaOntModel = (OntModel) session.getAttribute("jenaOntModel");
		if (jenaOntModel == null) {
			jenaOntModel = (OntModel) context.getAttribute("jenaOntModel");
		}
		if (jenaOntModel == null) {
			log.error("Unable to notify audit model of login event "
					+ "because no model could be found");
			return;
		}

		jenaOntModel.getBaseModel().notifyEvent(event);
	}

	private void dumpStateToLog(String label, State state, VitroRequest vreq) {
		log.debug("State on " + label + ": " + state);

		if (log.isTraceEnabled()) {
			log.trace("Status bean on " + label + ": "
					+ LoginStatusBean.getBean(vreq));

			LoginProcessBean processBean = null;
			if (LoginProcessBean.isBean(vreq)) {
				processBean = LoginProcessBean.getBean(vreq);
			}
			log.trace("Process bean on " + label + ": " + processBean);
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		doPost(request, response);
	}

}
