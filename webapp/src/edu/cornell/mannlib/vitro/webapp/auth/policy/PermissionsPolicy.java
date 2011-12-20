/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasPermission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.Permission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * The user is authorized to perform the RequestedAction if one of his
 * Permissions will authorize it.
 */
public class PermissionsPolicy implements PolicyIface {

	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {
		for (Permission p : HasPermission.getPermissions(whoToAuth)) {
			if (p.isAuthorized(whatToAuth)) {
				return new BasicPolicyDecision(Authorization.AUTHORIZED,
						"PermissionsPolicy: approved by " + p);
			}
		}
		return new BasicPolicyDecision(Authorization.INCONCLUSIVE,
				"no permission will approve " + whatToAuth);
	}

	@Override
	public String toString() {
		return "PermissionsPolicy - " + hashCode();
	}

}
