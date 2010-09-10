/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 
 * @author jc55
 * 
 */
public class LoginFormBean {
	public static final int ANYBODY = 0;
	public static final int NON_EDITOR = 1;
	public static final int EDITOR = 4;
	public static final int CURATOR = 5;
	public static final int DBA = 50;

	private String userURI;
	private String sessionId;
	private String loginBrowser;
	private String loginRemoteAddr;
	private String loginName;
	private String loginStatus;
	private int loginUserId;
	private String loginRole;
	private String emailAddress;

	public LoginFormBean() {
		sessionId = "";
		loginBrowser = "";
		loginRemoteAddr = "";
		loginName = "";
		loginStatus = "none";
		loginUserId = 0;
		loginRole = "1";
		emailAddress = "";
	}

	public String toString() {
		String name = "-not-logged-in-";
		if (getLoginName() != null && !"".equals(getLoginName()))
			name = getLoginName();

		return this.getClass().getName() + " loginName: " + name
				+ " loginStatus: " + getLoginStatus() + " loginRole: "
				+ getLoginRole();
	}

	/**
	 * Tests a HttpSession to see if logged in and authenticated.
	 * 
	 * @returns loginRole if seems to be authenticated, -1 otherwise
	 */
	public int testSessionLevel(HttpServletRequest request) {
		// TODO: security code added by bdc34, should be checked by jc55
		HttpSession currentSession = request.getSession();
		int returnRole = -1;
		if (getLoginStatus().equals("authenticated")
				&& currentSession.getId().equals(getSessionId())
				&& request.getRemoteAddr().equals(getLoginRemoteAddr())) {
			try {
				returnRole = Integer.parseInt(getLoginRole());
			} catch (Throwable thr) {
			}
		}
		return returnRole;
	}

	public static boolean loggedIn(HttpServletRequest request, int minLevel) {
		if (request == null) {
			return false;
		}
		HttpSession sess = request.getSession(false);
		if (sess == null) {
			return false;
		}
		Object obj = sess.getAttribute("loginHandler");
		if (!(obj instanceof LoginFormBean)) {
			return false;
		}

		LoginFormBean lfb = (LoginFormBean) obj;
		return ("authenticated".equals(lfb.loginStatus) && Integer
				.parseInt(lfb.loginRole) >= minLevel);
	}

	/********************** GET METHODS *********************/

	public String getUserURI() {
		return userURI;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getLoginBrowser() {
		return loginBrowser;
	}

	public String getLoginRemoteAddr() {
		return loginRemoteAddr;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getLoginStatus() {
		return loginStatus;
	}

	public int getLoginUserId() {
		return loginUserId;
	}

	public String getLoginRole() {
		return loginRole;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	/********************** SET METHODS *********************/

	public void setUserURI(String uri) {
		this.userURI = uri;
	}

	public void setSessionId(String id) {
		sessionId = id;
	}

	public void setLoginBrowser(String b) {
		loginBrowser = b;
	}

	public void setLoginRemoteAddr(String ra) {
		loginRemoteAddr = ra;
	}

	public void setLoginName(String ln) {
		loginName = ln;
	}

	public void setLoginStatus(String ls) {
		loginStatus = ls;
	}

	public void setLoginUserId(int int_val) {
		loginUserId = int_val;
	}

	public void setLoginRole(String lr) {
		loginRole = lr;
	}

	public void setEmailAddress(String ea) {
		emailAddress = ea;
	}

}
