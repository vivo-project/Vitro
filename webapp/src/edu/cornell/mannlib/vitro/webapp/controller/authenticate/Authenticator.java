/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.beans.User;

/**
 * The tool that a login process will use to interface with the user records in
 * the model (or wherever).
 * 
 * This needs to be based on a HttpSession, because things like the UserDAO are
 * tied to the session. It seemed easier to base it on a HttpServletRequest,
 * which we can use to get the session.
 */
public abstract class Authenticator {
	// ----------------------------------------------------------------------
	// The factory
	//
	// Unit tests can replace the factory to get a stub class instead.
	// Note: this can only work because the factory value is not final.
	// ----------------------------------------------------------------------

	public static interface AuthenticatorFactory {
		Authenticator newInstance(HttpServletRequest request);
	}

	private static AuthenticatorFactory factory = new AuthenticatorFactory() {
		@Override
		public Authenticator newInstance(HttpServletRequest request) {
			return new BasicAuthenticator(request);
		}
	};

	public static Authenticator getInstance(HttpServletRequest request) {
		return factory.newInstance(request);
	}

	// ----------------------------------------------------------------------
	// The interface.
	// ----------------------------------------------------------------------

	/** Maximum inactive interval for a ordinary logged-in session, in seconds. */
	public static final int LOGGED_IN_TIMEOUT_INTERVAL = 300;

	/** Maximum inactive interval for a editor (or better) session, in seconds. */
	public static final int PRIVILEGED_TIMEOUT_INTERVAL = 32000;

	/**
	 * Does a user by this name exist?
	 */
	public abstract boolean isExistingUser(String username);

	/**
	 * Does a user by this name have this password?
	 */
	public abstract boolean isCurrentPassword(String username,
			String clearTextPassword);

	/**
	 * Get the user with this name, or null if no such user exists.
	 */
	public abstract User getUserByUsername(String username);

	/**
	 * Get a list of URIs of the people that this user is allowed to edit.
	 */
	public abstract List<String> asWhomMayThisUserEdit(User user);

	/**
	 * Record a new password for the user.
	 */
	public abstract void recordNewPassword(String username,
			String newClearTextPassword);

	/**
	 * <pre>
	 * Record that the user has logged in, with all of the housekeeping that 
	 * goes with it:
	 * - updating the user record
	 * - setting login status and timeout limit in the session
	 * - record the user in the session map
	 * - notify other users of the model
	 * </pre>
	 */
	public abstract void recordUserIsLoggedIn(String username);

	/**
	 * Record that the current user has logged out:
	 * - notify other users of the model.
	 * - invalidate the session.
	 */
	public abstract void recordUserIsLoggedOut();
}
