/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * A base class with utility methods for policies involving self-editing.
 */
public abstract class BaseSelfEditingPolicy {
	protected final ServletContext ctx;
	protected final RoleLevel roleLevel;

	public BaseSelfEditingPolicy(ServletContext ctx, RoleLevel roleLevel) {
		this.ctx = ctx;
		this.roleLevel = roleLevel;
	}

	protected boolean canModifyResource(String uri) {
		return PropertyRestrictionBean.getBean().canModifyResource(uri,
				roleLevel);
	}

	protected boolean canModifyPredicate(Property predicate) {
		return PropertyRestrictionBean.getBean().canModifyPredicate(predicate,
				roleLevel);
	}

	protected PolicyDecision cantModifyResource(String uri) {
		return inconclusiveDecision("No access to admin resources; cannot modify "
				+ uri);
	}

	protected PolicyDecision cantModifyPredicate(Property predicate) {
		return inconclusiveDecision("No access to admin predicates; cannot modify "
				+ predicate.getURI());
	}

	protected PolicyDecision userNotAuthorizedToStatement() {
		return inconclusiveDecision("User has no access to this statement.");
	}

	/** An INCONCLUSIVE decision with a message like "PolicyClass: message". */
	protected PolicyDecision inconclusiveDecision(String message) {
		return new BasicPolicyDecision(Authorization.INCONCLUSIVE, getClass()
				.getSimpleName() + ": " + message);
	}

	/** An AUTHORIZED decision with a message like "PolicyClass: message". */
	protected PolicyDecision authorizedDecision(String message) {
		return new BasicPolicyDecision(Authorization.AUTHORIZED, getClass()
				.getSimpleName() + ": " + message);
	}

}
