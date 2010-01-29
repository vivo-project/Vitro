package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * Represents a request to view information about the server status.
 * @author bdc34
 *
 */
public class ServerStatus implements RequestedAction, AdminRequestedAction {

    /** returns "java://edu.cornell.mannlib.vitro.webapp.auth.requestActions.ServerStatusRequest" */
    public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }

    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle ids){
        return policy.visit(ids,this);
    }
}
