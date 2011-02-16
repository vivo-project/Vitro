/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

public class DropResource extends AbstractResourceAction implements
		RequestedAction {

	public DropResource(String typeUri, String subjectUri) {
		super(typeUri, subjectUri);
	}

	public PolicyDecision accept(VisitingPolicyIface policy,
			IdentifierBundle ids) {
		return policy.visit(ids, this);
	}
}
