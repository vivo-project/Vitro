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
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

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

	private final Map<String, UserAccount> usersByEmail = new HashMap<String, UserAccount>();
	private final Map<String, UserAccount> usersByExternalAuthId = new HashMap<String, UserAccount>();

	private final Map<String, List<String>> editingPermissions = new HashMap<String, List<String>>();
	private final Map<String, String> associatedUris = new HashMap<String, String>();
	private final List<String> recordedLogins = new ArrayList<String>();
	private final Map<String, String> newPasswords = new HashMap<String, String>();

	private HttpServletRequest request;

	private void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void addUser(UserAccount user) {
		usersByEmail.put(user.getEmailAddress(), user);

		String externalAuthId = user.getExternalAuthId();
		if (!externalAuthId.isEmpty()) {
			usersByExternalAuthId.put(user.getExternalAuthId(), user);
		}
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
	public UserAccount getAccountForInternalAuth(String emailAddress) {
		return usersByEmail.get(emailAddress);
	}

	@Override
	public UserAccount getAccountForExternalAuth(String externalAuthId) {
		return usersByExternalAuthId.get(externalAuthId);
	}

	@Override
	public boolean isCurrentPassword(UserAccount userAccount,
			String clearTextPassword) {
		if (userAccount == null) {
			return false;
		} else {
			return userAccount.getMd5Password().equals(
					Authenticator.applyMd5Encoding(clearTextPassword));
		}
	}

	@Override
	public List<String> getAssociatedIndividualUris(UserAccount userAccount) {
		List<String> uris = new ArrayList<String>();

		String emailAddress = userAccount.getEmailAddress();
		if (associatedUris.containsKey(emailAddress)) {
			uris.add(associatedUris.get(emailAddress));
		}

		if (editingPermissions.containsKey(emailAddress)) {
			uris.addAll(editingPermissions.get(emailAddress));
		}

		return uris;
	}

	@Override
	public void recordNewPassword(UserAccount userAccount,
			String newClearTextPassword) {
		newPasswords.put(userAccount.getEmailAddress(), newClearTextPassword);
	}

	@Override
	public void recordLoginAgainstUserAccount(UserAccount userAccount,
			AuthenticationSource authSource) {
		recordedLogins.add(userAccount.getEmailAddress());

		LoginStatusBean lsb = new LoginStatusBean(userAccount.getUri(),
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
	public boolean accountRequiresEditing(UserAccount userAccount) {
		throw new RuntimeException(
				"AuthenticatorStub.accountRequiresEditing() not implemented.");
	}

}
