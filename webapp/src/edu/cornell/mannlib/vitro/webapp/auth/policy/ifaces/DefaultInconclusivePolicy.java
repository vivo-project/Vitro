/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.BasicPolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A policy where every type of action is authorized as INCONCLUSIVE by default.
 * 
 * @author bdc34
 */
public class DefaultInconclusivePolicy implements PolicyIface {
	protected static PolicyDecision INCONCLUSIVE_DECISION = new BasicPolicyDecision(
			Authorization.INCONCLUSIVE,
			"This is the default decision defined in DefaultInconclusivePolicy");

    @Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
            RequestedAction whatToAuth) {
        if (whoToAuth == null)
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "null was passed as whoToAuth");
        if (whatToAuth == null)
            return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
                    "null was passed as whatToAuth");
        return INCONCLUSIVE_DECISION;
    }

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " - " + hashCode();
	}

}
