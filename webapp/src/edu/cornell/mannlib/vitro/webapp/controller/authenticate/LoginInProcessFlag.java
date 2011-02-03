/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This sits in the session to say that a login is in process.
 * 
 * Authenticate sets the flag each time it redirects to the login widget, and
 * the login widget inspects the flag and resets it.
 * 
 * If ever the login widget finds that the flag is already reset, it knows that
 * the user navigated to the widget directly, rather than coming through
 * Authenticate, and so it discards any existing LoginProcessBean as obsolete.
 */
public class LoginInProcessFlag {
	private static final String ATTRIBUTE_NAME = LoginInProcessFlag.class
			.getName();

	/**
	 * Set the flag, saying that a login session is in process.
	 */
	public static void set(HttpServletRequest request) {
		if (request == null) {
			throw new NullPointerException("request may not be null.");
		}
		
		request.getSession().setAttribute(ATTRIBUTE_NAME, Boolean.TRUE);
	}

	/**
	 * Check to see whether the flag is set. Reset it.
	 */
	public static boolean checkAndReset(HttpServletRequest request) {
		if (request == null) {
			throw new NullPointerException("request may not be null.");
		}
		
		HttpSession session = request.getSession(false);
		if (session == null) {
			return false;
		}
		
		Object flag = session.getAttribute(ATTRIBUTE_NAME);
		if (flag == null) {
			return false;
		}

		session.removeAttribute(ATTRIBUTE_NAME);
		return true;
	}
}
