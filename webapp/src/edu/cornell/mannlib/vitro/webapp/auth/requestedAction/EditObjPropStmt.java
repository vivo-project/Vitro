/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.ThreeParameterAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

public class EditObjPropStmt extends ThreeParameterAction implements RequestedAction {
    
       
    public EditObjPropStmt(ObjectPropertyStatement ops){    
        setUriOfSubject(ops.getSubjectURI());
        setUriOfPredicate(ops.getPropertyURI());
        setUriOfObject(ops.getObjectURI());
    }
    
    public EditObjPropStmt(String subjectUri, String keywordPredUri,
            String objectUri) {
        setUriOfSubject(subjectUri);
        setUriOfPredicate(keywordPredUri);
        setUriOfObject(objectUri);
    }

    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle whoToAuth) {
        return policy.visit(whoToAuth,this);
    }

    public String getURI() {
     return RequestActionConstants.actionNamespace + this.getClass().getName();
    }

}
