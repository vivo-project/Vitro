/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.specialrelationships;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.BasicPolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.PropertyRestrictionBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.Property;

/**
 * A collection of building-block methods so we can code a policy based on the
 * relationship of the object being edited to the identity of the user doing the
 * editing.
 */
public abstract class AbstractRelationshipPolicy implements PolicyIface {
	private static final Log log = LogFactory
			.getLog(AbstractRelationshipPolicy.class);

	protected final ServletContext ctx;

	public AbstractRelationshipPolicy(ServletContext ctx) {
		this.ctx = ctx;
	}

	protected boolean canModifyResource(String uri) {
		return PropertyRestrictionBean.getBean().canModifyResource(uri,
				RoleLevel.SELF);
	}

	protected boolean canModifyPredicate(Property predicate) {
		return PropertyRestrictionBean.getBean().canModifyPredicate(predicate,
				RoleLevel.SELF);
	}

	protected PolicyDecision cantModifyResource(String uri) {
		return inconclusiveDecision("No access to admin resources; cannot modify "
				+ uri);
	}

	protected PolicyDecision cantModifyPredicate(String uri) {
		return inconclusiveDecision("No access to admin predicates; cannot modify "
				+ uri);
	}

	protected PolicyDecision userNotAuthorizedToStatement() {
		return inconclusiveDecision("User has no access to this statement.");
	}

	/** An INCONCLUSIVE decision with a message like "PolicyClass: message". */
	protected PolicyDecision inconclusiveDecision(String message) {
		return new BasicPolicyDecision(Authorization.INCONCLUSIVE, getClass()
				.getSimpleName() + ": " + message);
	}

}
