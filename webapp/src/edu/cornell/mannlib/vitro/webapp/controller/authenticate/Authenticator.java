/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

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
	public static final int LOGGED_IN_TIMEOUT_INTERVAL = 60 * 60;

	/** Maximum inactive interval for a editor (or better) session, in seconds. */
	public static final int PRIVILEGED_TIMEOUT_INTERVAL = 60 * 60 * 8;

	/**
	 * Get the UserAccount for this external ID, or null if there is none.
	 */
	public abstract UserAccount getAccountForExternalAuth(String externalAuthId);

	/**
	 * Get the UserAccount for this email address, or null if there is none.
	 */
	public abstract UserAccount getAccountForInternalAuth(String emailAddress);

	/**
	 * Internal: does this UserAccount have this password? False if the
	 * userAccount is null.
	 */
	public abstract boolean isCurrentPassword(UserAccount userAccount,
			String clearTextPassword);

	/**
	 * Internal: record a new password for the user. Takes no action if the
	 * userAccount is null.
	 */
	public abstract void recordNewPassword(UserAccount userAccount,
			String newClearTextPassword);

	/**
	 * Is a change in name or email required when the user logs in?
	 */
	public abstract boolean accountRequiresEditing(UserAccount userAccount);

	/**
	 * Get the URIs of all individuals associated with this user, whether by a
	 * self-editing property like cornellEmailNetid, or by mayEditAs.
	 */
	public abstract List<String> getAssociatedIndividualUris(
			UserAccount userAccount);

	/**
	 * <pre>
	 * Record that the user has logged in, with all of the housekeeping that 
	 * goes with it:
	 * - update the user record
	 * - set login status and timeout limit in the session
	 * - refresh the Identifiers on the request
	 * - record the user in the session map
	 * - notify other users of the model
	 * </pre>
	 */
	public abstract void recordLoginAgainstUserAccount(UserAccount userAccount,
			AuthenticationSource authSource);

	/**
	 * <pre>
	 * Record that the current user has logged out: - notify other users of the
	 * model. 
	 * - invalidate the session.
	 * </pre>
	 */
	public abstract void recordUserIsLoggedOut();

	// ----------------------------------------------------------------------
	// Public utility methods.
	// ----------------------------------------------------------------------

	/**
	 * Apply MD5 to this string, and encode as a string of hex digits. Just
	 * right for storing passwords in the database, or hashing the password
	 * link.
	 */
	public static String applyMd5Encoding(String raw) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(raw.getBytes());
			char[] hexChars = Hex.encodeHex(digest);
			return new String(hexChars).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			// This can't happen with a normal Java runtime.
			throw new RuntimeException(e);
		}
	}

	/**
	 * Check whether the form of the emailAddress is syntactically correct. Does
	 * not allow multiple addresses. Does not allow local addresses (without a
	 * hostname).
	 * 
	 * Does not confirm that the host actually exists, or has a mailbox by that
	 * name.
	 */
	public static boolean isValidEmailAddress(String emailAddress) {
		try {
			// InternetAddress constructor will throw an exception if the
			// address does not have valid format (if "strict" is true).
			@SuppressWarnings("unused")
			InternetAddress a = new InternetAddress(emailAddress, true);

			// InternetAddress permits a localname without hostname.
			// Guard against that.
			if (emailAddress.indexOf('@') == -1) {
				return false;
			}

			return true;
		} catch (AddressException e) {
			return false;
		}
	}
}
