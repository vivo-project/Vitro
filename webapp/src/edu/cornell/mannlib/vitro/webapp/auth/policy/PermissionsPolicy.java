/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasPermission;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.Permission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;

/**
 * The user is authorized to perform the RequestedAction if one of his
 * Permissions will authorize it.
 */
public class PermissionsPolicy implements PolicyIface {
	private static final Log log = LogFactory.getLog(PermissionsPolicy.class);

	@Override
	public PolicyDecision isAuthorized(IdentifierBundle whoToAuth,
			RequestedAction whatToAuth) {
		if (whoToAuth == null) {
			return defaultDecision("whomToAuth was null");
		}
		if (whatToAuth == null) {
			return defaultDecision("whatToAuth was null");
		}

		for (Permission p : HasPermission.getPermissions(whoToAuth)) {
			if (p.isAuthorized(whatToAuth)) {
				log.debug("Permission " + p + " approves request " + whatToAuth);
				return new BasicPolicyDecision(Authorization.AUTHORIZED,
						"PermissionsPolicy: approved by " + p);
			} else {
			    log.trace("Permission " + p + " denies request " + whatToAuth);
			}
		}
		log.debug("No permission will approve " + whatToAuth);
		return defaultDecision("no permission will approve " + whatToAuth);
	}

	/** If the user isn't explicitly authorized, return this. */
	private PolicyDecision defaultDecision(String message) {
		return new BasicPolicyDecision(Authorization.INCONCLUSIVE, message);
	}

	@Override
	public String toString() {
		return "PermissionsPolicy - " + hashCode();
	}

}
