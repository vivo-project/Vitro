/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A collection of static methods to help determine whether requested actions
 * are authorized by current policy.
 */
public class PolicyHelper {
	private static final Log log = LogFactory.getLog(PolicyHelper.class);

	/**
	 * Are these actions authorized for the current user by the current
	 * policies?
	 */
	public static boolean isAuthorizedForActions(HttpServletRequest req,
			RequestedAction... actions) {
		return isAuthorizedForActions(req, new Actions(actions));
	}
	
	/**
	 * Are these actions authorized for the current user by the current
	 * policies?
	 */
	public static boolean isAuthorizedForActions(HttpServletRequest req,
			Actions actions) {
		PolicyIface policy = ServletPolicyList.getPolicies(req);
		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);
		return Actions.notNull(actions).isAuthorized(policy, ids);
	}

	/**
	 * No need to instantiate this helper class - all methods are static.
	 */
	private PolicyHelper() {
		// nothing to do.
	}

}
