/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/* Represents a request to perform an action.    */
public abstract class RequestedAction extends AuthorizationRequest {
	public static String ACTION_NAMESPACE = "java:";

	public static String SOME_URI = "?SOME_URI";
	public static Property SOME_PREDICATE = new Property(SOME_URI);
	public static String SOME_LITERAL = "?SOME_LITERAL";

	/**
	 * In its most basic form, a RequestAction needs to have an identifier.
	 * Sometimes this will be enough.
	 */
	public String getURI() {
		return ACTION_NAMESPACE + this.getClass().getName();
	}

	/**
	 * For authorization, just ask the Policy. INCONCLUSIVE is not good enough.
	 */
	@Override
	public final boolean isAuthorized(IdentifierBundle ids, PolicyIface policy) {
		PolicyDecision decision = policy.isAuthorized(ids, this);
		return decision.getAuthorized() == Authorization.AUTHORIZED;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
