/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import java.util.Arrays;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;

/**
 * A base class for RequestedAction that permits boolean operations on them.
 * 
 * A null request is ignored, so in "and" it is equivalent to true, while in
 * "or" it is equivalent to false.
 */
public abstract class AuthorizationRequest {
	// ----------------------------------------------------------------------
	// Constants
	// ----------------------------------------------------------------------

	public static final AuthorizationRequest AUTHORIZED = new AuthorizationRequest() {
		@Override
		public boolean isAuthorized(IdentifierBundle ids, PolicyIface policy) {
			return true;
		}
	};

	public static final AuthorizationRequest UNAUTHORIZED = new AuthorizationRequest() {
		@Override
		public boolean isAuthorized(IdentifierBundle ids, PolicyIface policy) {
			return false;
		}
	};

	// ----------------------------------------------------------------------
	// Static convenience methods
	// ----------------------------------------------------------------------

	public static AuthorizationRequest andAll(AuthorizationRequest... ars) {
		return andAll(Arrays.asList(ars));
	}

	public static AuthorizationRequest andAll(
			Iterable<? extends AuthorizationRequest> ars) {
		AuthorizationRequest result = AUTHORIZED;
		for (AuthorizationRequest ar : ars) {
			result = result.and(ar);
		}
		return result;
	}

	// ----------------------------------------------------------------------
	// The abstract class
	// ----------------------------------------------------------------------

	public AuthorizationRequest and(AuthorizationRequest that) {
		if (that == null) {
			return this;
		} else {
			return new AndAuthorizationRequest(this, that);
		}
	}

	public AuthorizationRequest or(AuthorizationRequest that) {
		if (that == null) {
			return this;
		} else {
			return new OrAuthorizationRequest(this, that);
		}
	}

	public abstract boolean isAuthorized(IdentifierBundle ids,
			PolicyIface policy);

	// ----------------------------------------------------------------------
	// Subclasses for boolean operations
	// ----------------------------------------------------------------------

	private static class AndAuthorizationRequest extends AuthorizationRequest {
		private final AuthorizationRequest ar1;
		private final AuthorizationRequest ar2;

		private AndAuthorizationRequest(AuthorizationRequest ar1,
				AuthorizationRequest ar2) {
			this.ar1 = ar1;
			this.ar2 = ar2;
		}

		@Override
		public boolean isAuthorized(IdentifierBundle ids, PolicyIface policy) {
			return ar1.isAuthorized(ids, policy)
					&& ar2.isAuthorized(ids, policy);
		}

		@Override
		public String toString() {
			return "(" + ar1 + " && " + ar2 + ")";
		}

	}

	private static class OrAuthorizationRequest extends AuthorizationRequest {
		private final AuthorizationRequest ar1;
		private final AuthorizationRequest ar2;

		private OrAuthorizationRequest(AuthorizationRequest ar1,
				AuthorizationRequest ar2) {
			this.ar1 = ar1;
			this.ar2 = ar2;
		}

		@Override
		public boolean isAuthorized(IdentifierBundle ids, PolicyIface policy) {
			return ar1.isAuthorized(ids, policy)
					|| ar2.isAuthorized(ids, policy);
		}

		@Override
		public String toString() {
			return "(" + ar1 + " || " + ar2 + ")";
		}

	}

}
