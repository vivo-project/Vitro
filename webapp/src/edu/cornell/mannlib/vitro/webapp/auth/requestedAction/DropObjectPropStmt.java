package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.ThreeParameterAction;

public class DropObjectPropStmt  extends ThreeParameterAction implements RequestedAction {

    public DropObjectPropStmt(String sub, String pred, String obj){
            setUriOfSubject(sub);
            setUriOfPredicate(pred);
            setUriOfObject(obj);
    }
    
    public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }
    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle ids){
        return policy.visit(ids,this);
    }
}
