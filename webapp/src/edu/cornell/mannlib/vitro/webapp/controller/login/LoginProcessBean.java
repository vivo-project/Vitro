/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.login;

import java.text.MessageFormat;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Where are we in the process of logging on? What message should we show to the
 * user?
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
		NOWHERE, LOGGING_IN, FORCED_PASSWORD_CHANGE, CANCELLED, LOGGED_IN
	}

	private enum MLevel {
		NONE, INFO, ERROR
	}

	public enum Message {
		NO_MESSAGE("", MLevel.NONE),

		PASSWORD_CHANGE_SAVED("Your password has been saved.<br/>"
				+ "Please log in.", MLevel.INFO),

		NO_USERNAME("Please enter your email address.", MLevel.ERROR),

		NO_PASSWORD("Please enter your password.", MLevel.ERROR),

		UNKNOWN_USERNAME("The email or password you entered is incorrect.",
				MLevel.ERROR),

		INCORRECT_PASSWORD("The email or password you entered is incorrect.",
				MLevel.ERROR),

		NO_NEW_PASSWORD("Please enter your new password.", MLevel.ERROR),

		MISMATCH_PASSWORD("The passwords entered do not match.", MLevel.ERROR),

		PASSWORD_LENGTH(
				"Please enter a password between {0} and {1} characters in length.",
				MLevel.ERROR),

		USING_OLD_PASSWORD("Please choose a different password from the "
				+ "temporary one provided initially.", MLevel.ERROR);

		private final String format;
		private final MLevel messageLevel;

		Message(String format, MLevel messageLevel) {
			this.format = format;
			this.messageLevel = messageLevel;
		}

		String getFormat() {
			return this.format;
		}

		MLevel getMessageLevel() {
			return this.messageLevel;
		}

		String formatMessage(Object[] args) {
			return new MessageFormat(this.format).format(args);
		}
	}

	// ----------------------------------------------------------------------
	// the bean
	// ----------------------------------------------------------------------

	/** Where are we in the process? */
	private State currentState = State.NOWHERE;

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
		this.message = Message.NO_MESSAGE;
		this.messageArguments = NO_ARGUMENTS;
	}

	public void setMessage(Message message, Object... args) {
		this.message = message;
		this.messageArguments = args;
	}

	public String getInfoMessage() {
		if (message.getMessageLevel() == MLevel.INFO) {
			return message.formatMessage(messageArguments);
		} else {
			return "";
		}
	}

	public String getErrorMessage() {
		if (message.getMessageLevel() == MLevel.ERROR) {
			return message.formatMessage(messageArguments);
		} else {
			return "";
		}
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "LoginProcessBean[state=" + currentState + ", message="
				+ message + ", messageArguments="
				+ Arrays.deepToString(messageArguments) + ", username="
				+ username + "]";
	}

}
