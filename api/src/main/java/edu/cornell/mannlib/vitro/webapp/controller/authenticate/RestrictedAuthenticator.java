/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean.AuthenticationSource;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * A "restricted" authenticator, that will not allow logins except for root and
 * for users that are authorized to maintain the system.
 */
public class RestrictedAuthenticator extends Authenticator {
	// ----------------------------------------------------------------------
	// The factory
	// ----------------------------------------------------------------------

	public static class Factory implements AuthenticatorFactory {
		@Override
		public Authenticator getInstance(HttpServletRequest req) {
			return new RestrictedAuthenticator(req, new BasicAuthenticator(req));
		}
	}

	// ----------------------------------------------------------------------
	// The authenticator
	// ----------------------------------------------------------------------

	private final HttpServletRequest req;
	private final Authenticator auth;

	public RestrictedAuthenticator(HttpServletRequest req, Authenticator auth) {
		this.req = req;
		this.auth = auth;
	}

	@Override
	public boolean isUserPermittedToLogin(UserAccount userAccount) {
		if (userAccount == null) {
			return false;
		}
		
		ArrayIdentifierBundle ids = new ArrayIdentifierBundle();
		ids.addAll(getIdsForUserAccount(req, userAccount));
		ids.addAll(RequestIdentifiers.getIdBundleForRequest(req));

		return PolicyHelper.isAuthorizedForActions(ids,
				ServletPolicyList.getPolicies(req),
				SimplePermission.LOGIN_DURING_MAINTENANCE.ACTION);
	}

	@Override
	public void recordLoginAgainstUserAccount(UserAccount userAccount,
			AuthenticationSource authSource) throws LoginNotPermitted {
		if (!isUserPermittedToLogin(userAccount)) {
			throw new LoginNotPermitted();
		}
		auth.recordLoginAgainstUserAccount(userAccount, authSource);
	}

	@Override
	public UserAccount getAccountForExternalAuth(String externalAuthId) {
		return auth.getAccountForExternalAuth(externalAuthId);
	}

	@Override
	public UserAccount getAccountForInternalAuth(String emailAddress) {
		return auth.getAccountForInternalAuth(emailAddress);
	}

	@Override
	public boolean isCurrentPassword(UserAccount userAccount,
			String clearTextPassword) {
		return auth.isCurrentPassword(userAccount, clearTextPassword);
	}

	@Override
	public void recordNewPassword(UserAccount userAccount,
			String newClearTextPassword) {
		auth.recordNewPassword(userAccount, newClearTextPassword);
	}

	@Override
	public boolean accountRequiresEditing(UserAccount userAccount) {
		return auth.accountRequiresEditing(userAccount);
	}

	@Override
	public List<String> getAssociatedIndividualUris(UserAccount userAccount) {
		return auth.getAssociatedIndividualUris(userAccount);
	}

	@Override
	public void recordUserIsLoggedOut() {
		auth.recordUserIsLoggedOut();
	}

	@Override
	public String toString() {
		return "RestrictedAuthenticator[" + auth + "]";
	}

}
