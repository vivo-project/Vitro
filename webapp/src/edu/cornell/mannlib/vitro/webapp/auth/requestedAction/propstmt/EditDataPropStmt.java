/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

public class EditDataPropStmt extends AbstractDataPropertyAction {

    private final DataPropertyStatement dataPropStmt;
    
    public EditDataPropStmt(DataPropertyStatement dps){
    	super(dps.getIndividualURI(), dps.getDatapropURI());
        this.dataPropStmt = dps;
    }
    
    @Override
    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle whoToAuth) {
        return policy.visit(whoToAuth,this);
    }

    public String data(){ return dataPropStmt.getData(); }
    public String lang(){ return dataPropStmt.getLanguage(); }
    public String datatype(){return dataPropStmt.getDatatypeURI(); }
    
}
