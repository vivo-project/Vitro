/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.login;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.edit.Authenticate;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.login.LoginProcessBean.State;
import freemarker.template.Configuration;

/**
 * A temporary means of displaying the Login templates within the SiteAdmin
 * form.
 * 
 * This class contains stuff that I swiped from {@link Authenticate}. The base
 * class, {@link LoginTemplateHelperBase}, contains stuff that I swiped from
 * {@link FreemarkerHttpServlet}.
 */
public class LoginTemplateHelper extends LoginTemplateHelperBase {
	private static final Log log = LogFactory.getLog(LoginTemplateHelper.class);

	/** If they are logging in, show them this form. */
	public static final String TEMPLATE_LOGIN = "login-form.ftl";

	/** If they are changing their password on first login, show them this form. */
	public static final String TEMPLATE_FORCE_PASSWORD_CHANGE = "login-forcedPasswordChange.ftl";

	public static final String BODY_LOGIN_NAME = "loginName";
	public static final String BODY_FORM_ACTION = "formAction";
	public static final String BODY_INFO_MESSAGE = "infoMessage";
	public static final String BODY_ERROR_MESSAGE = "errorMessage";
	public static final String BODY_ALERT_ICON_URL = "alertImageUrl";
	public static final String BODY_CANCEL_URL = "cancelUrl";

	/** Use this icon for an info message. */
	public static final String URL_INFO_ICON = "/images/iconAlert.png";

	/** Use this icon for an error message. */
	public static final String URL_ERROR_ICON = "/images/iconAlert.png";

	/** If no portal is specified in the request, use this one. */
	private static final int DEFAULT_PORTAL_ID = 1;

	public LoginTemplateHelper(HttpServletRequest req) {
		super(req);
	}

	public String showLoginPage(HttpServletRequest request) {
		try {
			VitroRequest vreq = new VitroRequest(request);

			State state = getCurrentLoginState(vreq);
			log.debug("State on exit: " + state);

			switch (state) {
			case LOGGED_IN:
				return "";
			case FORCED_PASSWORD_CHANGE:
				return doTemplate(vreq, showPasswordChangeScreen(vreq));
			default:
				return doTemplate(vreq, showLoginScreen(vreq));
			}
		} catch (Exception e) {
			log.error(e);
			return "<h2>Internal server error:<br/>" + e + "</h2>";
		}
	}

	/**
	 * User is just starting the login process. Be sure that we have a
	 * {@link LoginProcessBean} with the correct status. Show them the login
	 * screen.
	 */
	private TemplateResponseValues showLoginScreen(VitroRequest vreq)
			throws IOException {
		LoginProcessBean bean = getLoginProcessBean(vreq);
		bean.setState(State.LOGGING_IN);
		log.trace("Going to login screen: " + bean);

		TemplateResponseValues trv = new TemplateResponseValues(TEMPLATE_LOGIN);
		trv.put(BODY_FORM_ACTION, getAuthenticateUrl(vreq));
		trv.put(BODY_LOGIN_NAME, bean.getUsername());

		String infoMessage = bean.getInfoMessage();
		if (!infoMessage.isEmpty()) {
			trv.put(BODY_INFO_MESSAGE, infoMessage);
			trv.put(BODY_ALERT_ICON_URL, UrlBuilder.getUrl(URL_INFO_ICON));
		}
		String errorMessage = bean.getErrorMessage();
		if (!errorMessage.isEmpty()) {
			trv.put(BODY_ERROR_MESSAGE, errorMessage);
			trv.put(BODY_ALERT_ICON_URL, UrlBuilder.getUrl(URL_ERROR_ICON));
		}
		return trv;
	}

	/**
	 * The user has given the correct password, but now they are required to
	 * change it (unless they cancel out).
	 */
	private TemplateResponseValues showPasswordChangeScreen(VitroRequest vreq) {
		LoginProcessBean bean = getLoginProcessBean(vreq);
		bean.setState(State.FORCED_PASSWORD_CHANGE);
		log.trace("Going to password change screen: " + bean);

		TemplateResponseValues trv = new TemplateResponseValues(
				TEMPLATE_FORCE_PASSWORD_CHANGE);
		trv.put(BODY_FORM_ACTION, getAuthenticateUrl(vreq));
		trv.put(BODY_CANCEL_URL, getCancelUrl(vreq));

		String errorMessage = bean.getErrorMessage();
		if (!errorMessage.isEmpty()) {
			trv.put(BODY_ERROR_MESSAGE, errorMessage);
			trv.put(BODY_ALERT_ICON_URL, UrlBuilder.getUrl(URL_ERROR_ICON));
		}
		return trv;
	}

	/**
	 * We processed a response, and want to show a template.
	 */
	private String doTemplate(VitroRequest vreq, TemplateResponseValues values) {
		// Set it up like FreeMarkerHttpServlet.doGet() would do.
		Configuration config = getConfig(vreq);
		Map<String, Object> sharedVariables = getSharedVariables(vreq);
		Map<String, Object> root = new HashMap<String, Object>(sharedVariables);
		Map<String, Object> body = new HashMap<String, Object>(sharedVariables);
		setUpRoot(vreq, root);

		// Add the values that we got, and merge to the template.
		body.putAll(values.getBodyMap());
		return mergeBodyToTemplate(values.getTemplateName(), body, config);
	}

	/**
	 * Where are we in the process? Logged in? Not? Somewhere in between?
	 */
	private State getCurrentLoginState(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return State.NOWHERE;
		}

		LoginFormBean lfb = (LoginFormBean) session
				.getAttribute("loginHandler");
		if ((lfb != null) && (lfb.getLoginStatus().equals("authenticated"))) {
			return State.LOGGED_IN;
		}

		return getLoginProcessBean(request).getState();
	}

	/**
	 * How is the login process coming along?
	 */
	private LoginProcessBean getLoginProcessBean(HttpServletRequest request) {
		HttpSession session = request.getSession();

		LoginProcessBean bean = (LoginProcessBean) session
				.getAttribute(LoginProcessBean.SESSION_ATTRIBUTE);

		if (bean == null) {
			bean = new LoginProcessBean();
			session.setAttribute(LoginProcessBean.SESSION_ATTRIBUTE, bean);
		}

		return bean;
	}

	/** What's the URL for this servlet? */
	private String getAuthenticateUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String urlParams = "?home=" + getPortalIdString(request)
				+ "&login=block";
		return contextPath + "/authenticate" + urlParams;
	}

	/** What's the URL for this servlet, with the cancel parameter added? */
	private String getCancelUrl(HttpServletRequest request) {
		String contextPath = request.getContextPath();
		String urlParams = "?home=" + getPortalIdString(request)
		+ "&login=block&cancel=true";
		return contextPath + "/authenticate" + urlParams;
	}
	
	/**
	 * What portal are we currently in?
	 */
	private String getPortalIdString(HttpServletRequest request) {
		String portalIdParameter = request.getParameter("home");
		if (portalIdParameter == null) {
			return String.valueOf(DEFAULT_PORTAL_ID);
		} else {
			return portalIdParameter;
		}
	}

	/**
	 * Holds the name of the template and the map of values.
	 */
	private static class TemplateResponseValues {
		private final String templateName;
		private final Map<String, Object> bodyMap = new HashMap<String, Object>();

		public TemplateResponseValues(String templateName) {
			this.templateName = templateName;
		}

		public TemplateResponseValues put(String key, Object value) {
			this.bodyMap.put(key, value);
			return this;
		}

		public Map<? extends String, ? extends Object> getBodyMap() {
			return Collections.unmodifiableMap(this.bodyMap);
		}

		public String getTemplateName() {
			return this.templateName;
		}
	}
}
