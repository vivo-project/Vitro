/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.login;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.i18n.I18n;

/**
 * Where are we in the process of logging on? What message should we show to the
 * user?
 * 
 * TODO: the contents loginPageUrl is not explicitly defined, but it is assumed
 * to be either null, absolute, or relative to the host. It would be better if
 * it were explicitly defined, and either null, absolute or relative to the
 * context path. Then, the context path could be applied when the URL is used.
 * Later for that.
 * 
 * TODO: similar for afterLoginUrl, I presume.
 */
public class LoginProcessBean {
	private static final Log log = LogFactory.getLog(LoginProcessBean.class);

	private static Object[] NO_ARGUMENTS = new Object[0];

	private static final String SESSION_ATTRIBUTE = LoginProcessBean.class
			.getName();

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	/**
	 * Is there currently a login process bean in the session?
	 */
	public static boolean isBean(HttpServletRequest request) {
		return (null != getBeanFromSession(request));
	}

	/**
	 * Get the login process bean from the session. If there is no bean, create
	 * one.
	 */
	public static LoginProcessBean getBean(HttpServletRequest request) {
		if (isBean(request)) {
			return getBeanFromSession(request);
		} else {
			setBean(request, new LoginProcessBean());
			return getBeanFromSession(request);
		}
	}

	/**
	 * Store this login process bean in the session.
	 */
	public static void setBean(HttpServletRequest request, LoginProcessBean bean) {
		HttpSession session = request.getSession();
		session.setAttribute(SESSION_ATTRIBUTE, bean);
	}

	/**
	 * Remove the login process bean from the session. If there is no bean, do
	 * nothing.
	 */
	public static void removeBean(HttpServletRequest request) {
		if (isBean(request)) {
			request.getSession().removeAttribute(SESSION_ATTRIBUTE);
		}
	}

	/**
	 * Get the bean from the session, or null if there is no bean.
	 */
	private static LoginProcessBean getBeanFromSession(
			HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}

		Object bean = session.getAttribute(SESSION_ATTRIBUTE);
		if (bean == null) {
			return null;
		}

		if (!(bean instanceof LoginProcessBean)) {
			log.warn("Tried to get login process bean, but found an instance of "
					+ bean.getClass().getName() + ": " + bean);
			return null;
		}

		return (LoginProcessBean) bean;
	}

	// ----------------------------------------------------------------------
	// helper classes
	// ----------------------------------------------------------------------

	public enum State {
		NOWHERE, LOGGING_IN, FORCED_PASSWORD_CHANGE, LOGGED_IN
	}

	public enum MLevel {
		NONE, INFO, ERROR
	}

	public static class Message {
		public static final Message NO_MESSAGE = new Message();

		private final String text;
		private final MLevel messageLevel;

		public Message() {
			this.messageLevel = MLevel.NONE;
			this.text = "";
		}
		
		public Message(HttpServletRequest req, MLevel messageLevel, String textKey, Object... parameters) {
			this.messageLevel = messageLevel;
			this.text = I18n.bundle(req).text(textKey, parameters);
		}
		
		public MLevel getMessageLevel() {
			return this.messageLevel;
		}

		public String getText() {
			return text;
		}

		@Override
		public String toString() {
			return "Message[" + messageLevel + ", '" + text + "']";
		}
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	/** Where are we in the process? */
	private State currentState = State.NOWHERE;

	/** Where is the interaction taking place? */
	private volatile String loginPageUrl;

	/** Where do we go when finished? */
	private volatile String afterLoginUrl;

	/** message and messageArguments must be kept consistent. */
	private final Object messageSynchronizer = new Object();

	/** What message should we display on the screen? */
	private Message message = Message.NO_MESSAGE;

	/** What arguments are needed to format the message? */
	private Object[] messageArguments = NO_ARGUMENTS;

	/**
	 * What username was submitted to the form? This isn't just for display --
	 * if they are changing passwords, we need to remember who it is.
	 */
	private String username = "";

	public void setState(State newState) {
		this.currentState = newState;
	}

	public State getState() {
		return currentState;
	}

	public void clearMessage() {
		synchronized (messageSynchronizer) {
			this.message = Message.NO_MESSAGE;
			this.messageArguments = NO_ARGUMENTS;
		}
	}

	public void setMessage(Message message) {
		synchronized (messageSynchronizer) {
			this.message = message;
		}
	}
	
	public void setMessage(HttpServletRequest req, MLevel level, String textKey, Object... parameters) {
		synchronized (messageSynchronizer) {
			this.message = new Message(req, level, textKey, parameters);
		}
	}

	public String getInfoMessageAndClear() {
		synchronized (messageSynchronizer) {
			String text = "";
			if (message.getMessageLevel() == MLevel.INFO) {
				text = message.getText();
				clearMessage();
			}
			return text;
		}
	}

	public String getErrorMessageAndClear() {
		synchronized (messageSynchronizer) {
			String text = "";
			if (message.getMessageLevel() == MLevel.ERROR) {
				text = message.getText();
				clearMessage();
			}
			return text;
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getLoginPageUrl() {
		return loginPageUrl;
	}

	public void setLoginPageUrl(String loginPageUrl) {
		this.loginPageUrl = loginPageUrl;
	}

	public String getAfterLoginUrl() {
		return afterLoginUrl;
	}

	public void setAfterLoginUrl(String afterLoginUrl) {
		this.afterLoginUrl = afterLoginUrl;
	}

	@Override
	public String toString() {
		return "LoginProcessBean(" + hashCode() + ")[state=" + currentState
				+ ", message=" + message + ", messageArguments="
				+ Arrays.deepToString(messageArguments) + ", username="
				+ username + ", loginPageUrl=" + loginPageUrl
				+ ", afterLoginUrl=" + afterLoginUrl + "]";
	}

}
