/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.BasicPolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 *a policy where every type of action is authorized as UNAUTHORIZED
 * by default.  This can be useful for a unauthenticated session or
 * as the last policy on a PolicyList to force INCONCLUSIVE decisions
 * to UNAUTHORIZED.
 */
public class DefaultUnauthorizedPolicy implements PolicyIface{
    protected static PolicyDecision UNAUTHORIZED_DECISION = new BasicPolicyDecision(
            Authorization.UNAUTHORIZED,
            "This is the default decision defined in DefaultUnauthorizedPolicy");

    public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
            RequestedAction whatToAuth) {
        if (whoToAuth == null)
            return new BasicPolicyDecision(Authorization.UNAUTHORIZED,
                    "null was passed as whoToAuth");
        if (whatToAuth == null)
            return new BasicPolicyDecision(Authorization.UNAUTHORIZED,
                    "null was passed as whatToAuth");
        return UNAUTHORIZED_DECISION;
    }

	@Override
	public String toString() {
		return "DefaultInconclusivePolicy";
	}     
}
