/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.BasicPolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * a policy where every type of action is authorized as INCONCLUSIVE
 * by default.
 *
 * @author bdc34
 */
public class DefaultAuthorizedPolicy implements PolicyIface{
	protected static PolicyDecision AUTHORIZED_DECISION = new BasicPolicyDecision(
            Authorization.AUTHORIZED,
            "This is the default decision defined in DefaultAuthorizedPolicy");

    public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
            RequestedAction whatToAuth) {
        if (whoToAuth == null)
            return new BasicPolicyDecision(Authorization.AUTHORIZED,
                    "null was passed as whoToAuth");
        if (whatToAuth == null)
            return new BasicPolicyDecision(Authorization.AUTHORIZED,
                    "null was passed as whatToAuth");
        return AUTHORIZED_DECISION;
    }

    @Override
	public String toString() {
    	return "DefaultAuthorizedPolicy";
	}
}
