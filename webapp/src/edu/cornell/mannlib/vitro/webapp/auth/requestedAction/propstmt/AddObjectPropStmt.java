/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;

public class AddObjectPropStmt extends AbstractObjectPropertyAction {

	public AddObjectPropStmt(String uriOfSub, String uriOfPred, String uriOfObj) {
		super(uriOfSub, uriOfPred, uriOfObj);
	}

	@Override
	public PolicyDecision accept(VisitingPolicyIface policy,
			IdentifierBundle ids) {
		return policy.visit(ids, this);
	}
}
