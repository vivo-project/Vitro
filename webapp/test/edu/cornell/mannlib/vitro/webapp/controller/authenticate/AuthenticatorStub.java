/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.controller.edit.Authenticate;

/**
 * A simple stub for unit tests that require an Authenticator. Call setup() to
 * put it into place.
 */
public class AuthenticatorStub extends Authenticator {
	// ----------------------------------------------------------------------
	// factory
	// ----------------------------------------------------------------------

	/**
	 * Create a single instance of the stub. Force our factory into the
	 * Authenticator, so each request for an instance returns that one.
	 * 
	 * Call this at the top of each unit test, so you get fresh instance for
	 * each test.
	 */
	public static AuthenticatorStub setup() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		AuthenticatorStub authenticator = new AuthenticatorStub();

		Field factoryField = Authenticator.class.getDeclaredField("factory");
		factoryField.setAccessible(true);
		Authenticator.AuthenticatorFactory factory = new AuthenticatorStub.AuthenticatorFactory(
				authenticator);
		factoryField.set(null, factory);

		return authenticator;
	}

	/**
	 * This factory holds a single instance of the stub, and hands it out each
	 * time we request an "newInstance".
	 */
	private static class AuthenticatorFactory implements
			Authenticator.AuthenticatorFactory {
		private final AuthenticatorStub authenticator;

		public AuthenticatorFactory(AuthenticatorStub authenticator) {
			this.authenticator = authenticator;
		}

		@Override
		public Authenticator newInstance(HttpServletRequest request) {
			authenticator.setRequest(request);
			return authenticator;
		}
	}

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, User> usersByName = new HashMap<String, User>();
	private final Map<String, List<String>> editingPermissions = new HashMap<String, List<String>>();
	private final List<String> recordedLogins = new ArrayList<String>();
	private final List<String> loginSessions = new ArrayList<String>();
	private final Map<String, String> newPasswords = new HashMap<String, String>();

	private HttpServletRequest request;

	private void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void addUser(User user) {
		usersByName.put(user.getUsername(), user);
	}

	public void addEditingPermission(String userUri, String personUri) {
		if (!editingPermissions.containsKey(userUri)) {
			editingPermissions.put(userUri, new ArrayList<String>());
		}
		editingPermissions.get(userUri).add(personUri);
	}

	public List<String> getRecordedLoginUsernames() {
		return recordedLogins;
	}

	public Map<String, String> getNewPasswordMap() {
		return newPasswords;
	}

	public Collection<? extends String> getLoginSessions() {
		return loginSessions;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public boolean isExistingUser(String username) {
		return usersByName.containsKey(username);
	}

	@Override
	public User getUserByUsername(String username) {
		return usersByName.get(username);
	}

	@Override
	public boolean isCurrentPassword(String username, String clearTextPassword) {
		if (!isExistingUser(username)) {
			return false;
		}
		String md5Password = Authenticate.applyMd5Encoding(clearTextPassword);
		User user = getUserByUsername(username);
		return md5Password.equals(user.getMd5password());
	}

	@Override
	public void recordNewPassword(User user, String newClearTextPassword) {
		newPasswords.put(user.getUsername(), newClearTextPassword);
	}

	@Override
	public List<String> asWhomMayThisUserEdit(User user) {
		String userUri = user.getURI();
		if (editingPermissions.containsKey(userUri)) {
			return editingPermissions.get(userUri);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void recordSuccessfulLogin(User user) {
		recordedLogins.add(user.getUsername());
	}

	@Override
	public void setLoggedIn(User user) {
		LoginStatusBean lsb = new LoginStatusBean(user.getURI(),
				user.getUsername(), parseUserSecurityLevel(user.getRoleURI()));
		LoginStatusBean.setBean(request.getSession(), lsb);

		loginSessions.add(user.getUsername());
	}

	private static final String ROLE_NAMESPACE = "role:/";

	/**
	 * Parse the role URI from User. Don't crash if it is not valid.
	 */
	private int parseUserSecurityLevel(String roleURI) {
		try {
			if (roleURI.startsWith(ROLE_NAMESPACE)) {
				String roleLevel = roleURI.substring(ROLE_NAMESPACE.length());
				return Integer.parseInt(roleLevel);
			} else {
				return Integer.parseInt(roleURI);
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

}
