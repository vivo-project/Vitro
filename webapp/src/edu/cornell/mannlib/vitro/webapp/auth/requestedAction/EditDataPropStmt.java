package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */


import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

public class EditDataPropStmt implements RequestedAction {

    final DataPropertyStatement dataPropStmt;
    
    public EditDataPropStmt(DataPropertyStatement dps){
        this.dataPropStmt = dps;
    }
    
    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle whoToAuth) {
        return policy.visit(whoToAuth,this);
    }

    
    public String uriOfSubject(){ return dataPropStmt.getIndividualURI(); }
    public String uriOfPredicate(){ return dataPropStmt.getDatapropURI(); }
    public String data(){ return dataPropStmt.getData(); }
    public String lang(){ return dataPropStmt.getLanguage(); }
    public String datatype(){return dataPropStmt.getDatatypeURI(); }
    
    public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }

}
