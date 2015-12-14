/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * The tool that a login process will use to interface with the user records in
 * the model (or wherever).
 * 
 * This needs to be based on a HttpSession, because things like the UserDAO are
 * tied to the session. It seemed easier to base it on a HttpServletRequest,
 * which we can use to get the session.
 * 
 * TODO: Wouldn't it be cool if we could remove the LoginNotPermitted exception?
 * Perhaps we could have a sub-object called an Authenticator.ForUser, and you
 * call a getAuthenticatorForUser() method which returns null if your login has
 * been disabled. Then, that object would provide these methods:
 * accountRequiresEditing(), getAssociatedIndividualUris(), isCurrentPassword(),
 * recordLoginAgainstUserAccount(), recordNewPassword(). If you didn't have such
 * an object, you couldn't even call these methods.
 */
public abstract class Authenticator {
	// ----------------------------------------------------------------------
	// The factory
	//
	// Each Authenticator instance is used for a single request, so we store
	// a factory in the context that can create these instances.
	// ----------------------------------------------------------------------

	private static final String FACTORY_ATTRIBUTE_NAME = AuthenticatorFactory.class
			.getName();

	public interface AuthenticatorFactory {
		Authenticator getInstance(HttpServletRequest request);
	}

	/**
	 * Ask the currently configured AuthenticatorFactory to give us an
	 * Authenticator for this request.
	 * 
	 * If there is no factory, configure a Basic one.
	 */
	public static Authenticator getInstance(HttpServletRequest request) {
		ServletContext ctx = request.getSession().getServletContext();
		Object attribute = ctx.getAttribute(FACTORY_ATTRIBUTE_NAME);
		if (!(attribute instanceof AuthenticatorFactory)) {
			setAuthenticatorFactory(new BasicAuthenticator.Factory(), ctx);
			attribute = ctx.getAttribute(FACTORY_ATTRIBUTE_NAME);
		}
		AuthenticatorFactory factory = (AuthenticatorFactory) attribute;

		return factory.getInstance(request);
	}

	public static void setAuthenticatorFactory(AuthenticatorFactory factory,
			ServletContext ctx) {
		ctx.setAttribute(FACTORY_ATTRIBUTE_NAME, factory);
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
	 * Is this user permitted to login? Some Authenticators might disable logins
	 * for certain users.
	 * 
	 * Behavior when userAccount is null depends on the particular
	 * Authenticator. An answer of "true" presumably means that the user will be
	 * permitted to login and create an account on the fly.
	 * 
	 * Note that this method may rely on the HttpServletRequest object that was
	 * provided to the factory when this instance was created.
	 */
	public abstract boolean isUserPermittedToLogin(UserAccount userAccount);

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
	 * 
	 * @throws LoginNotPermitted
	 *             if the Authenticator denies this user the ability to login.
	 *             This should be thrown if and only if isUserPermittedToLogin()
	 *             returns false.
	 */
	public abstract void recordLoginAgainstUserAccount(UserAccount userAccount,
			AuthenticationSource authSource) throws LoginNotPermitted;

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

	/**
	 * Get the IDs that would be created for this userAccount, if this user were
	 * to log in.
	 */
	public static IdentifierBundle getIdsForUserAccount(HttpServletRequest req,
			UserAccount userAccount) {
		return ActiveIdentifierBundleFactories.getUserIdentifierBundle(req,
				userAccount);
	}

	// ----------------------------------------------------------------------
	// Exceptions
	// ----------------------------------------------------------------------

	public static class LoginNotPermitted extends Exception {
		// no other information
	}
}
