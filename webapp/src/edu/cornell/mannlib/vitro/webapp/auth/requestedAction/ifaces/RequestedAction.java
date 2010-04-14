/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;


/* Represents a request to perform an action.    */
public interface RequestedAction {
    /**
     * In its most basic form, a RequestAction needs to have an
     * identifier.  Sometimes this will be enough.  For example
     * ServerStatusRequest.
     * @return
     */
    public String getURI();

    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle whoToAuth);


}
