/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.beans.User;

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
	private final Map<String, String> associatedUris = new HashMap<String, String>();
	private final List<String> recordedLogins = new ArrayList<String>();
	private final Map<String, String> newPasswords = new HashMap<String, String>();

	private HttpServletRequest request;

	private void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void addUser(User user) {
		usersByName.put(user.getUsername(), user);
	}

	public void addEditingPermission(String username, String personUri) {
		if (!editingPermissions.containsKey(username)) {
			editingPermissions.put(username, new ArrayList<String>());
		}
		editingPermissions.get(username).add(personUri);
	}

	public void setAssociatedUri(String username, String individualUri) {
		associatedUris.put(username, individualUri);
	}

	public List<String> getRecordedLoginUsernames() {
		return recordedLogins;
	}

	public Map<String, String> getNewPasswordMap() {
		return newPasswords;
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
	public List<String> getAssociatedIndividualUris(String username) {
		List<String> uris = new ArrayList<String>();

		if (associatedUris.containsKey(username)) {
			uris.add(associatedUris.get(username));
		}

		if (editingPermissions.containsKey(username)) {
			uris.addAll(editingPermissions.get(username));
		}

		return uris;
	}

	@Override
	public boolean isCurrentPassword(String username, String clearTextPassword) {
		if (!isExistingUser(username)) {
			return false;
		}
		String md5Password = applyMd5Encoding(clearTextPassword);
		User user = getUserByUsername(username);
		return md5Password.equals(user.getMd5password());
	}

	@Override
	public void recordNewPassword(String username, String newClearTextPassword) {
		newPasswords.put(username, newClearTextPassword);
	}

	@Override
	public void recordLoginAgainstUserAccount(String username,
			AuthenticationSource authSource) {
		recordedLogins.add(username);

		User user = getUserByUsername(username);
		LoginStatusBean lsb = new LoginStatusBean(user.getURI(), username,
				authSource);
		LoginStatusBean.setBean(request.getSession(), lsb);
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void recordUserIsLoggedOut() {
		throw new RuntimeException(
				"AuthenticatorStub.recordUserIsLoggedOut() not implemented.");
	}

	@Override
	public void recordLoginWithoutUserAccount(String username,
			String individualUri, AuthenticationSource authSource) {
		throw new RuntimeException(
				"AuthenticatorStub.recordLoginWithoutUserAccount() not implemented.");
	}

	@Override
	public boolean isPasswordChangeRequired(String username) {
		throw new RuntimeException(
				"AuthenticatorStub.isPasswordChangeRequired() not implemented.");
	}

}
