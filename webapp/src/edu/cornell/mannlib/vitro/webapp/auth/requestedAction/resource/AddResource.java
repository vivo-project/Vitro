/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;

public class AddResource extends AbstractResourceAction {

	public AddResource(String typeUri, String subjectUri) {
		super(typeUri, subjectUri);
	}

	@Override
	public PolicyDecision accept(VisitingPolicyIface policy,
			IdentifierBundle ids) {
		return policy.visit(ids, this);
	}
}
