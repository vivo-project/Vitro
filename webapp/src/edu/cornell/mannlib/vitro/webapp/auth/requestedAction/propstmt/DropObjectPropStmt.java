/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;

public class DropObjectPropStmt extends AbstractObjectPropertyAction {

	public DropObjectPropStmt(String sub, String pred, String obj) {
		super(sub, pred, obj);
	}

	@Override
	public PolicyDecision accept(VisitingPolicyIface policy,
			IdentifierBundle ids) {
		return policy.visit(ids, this);
	}
}
