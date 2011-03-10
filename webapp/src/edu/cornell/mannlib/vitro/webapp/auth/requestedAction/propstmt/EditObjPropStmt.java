/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

public class EditObjPropStmt extends AbstractObjectPropertyAction {

	public EditObjPropStmt(ObjectPropertyStatement ops) {
		super(ops.getSubjectURI(), ops.getPropertyURI(), ops.getObjectURI());
	}

	public EditObjPropStmt(String subjectUri, String keywordPredUri,
			String objectUri) {
		super(subjectUri, keywordPredUri, objectUri);
	}

	@Override
	public PolicyDecision accept(VisitingPolicyIface policy,
			IdentifierBundle whoToAuth) {
		return policy.visit(whoToAuth, this);
	}

}
