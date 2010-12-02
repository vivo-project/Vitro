/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;

public class AddDataPropStmt extends AbstractDataPropertyAction {

    protected String data;
    protected String dataType;
    protected String lang;
    
    public AddDataPropStmt(String subjectUri, String predicateUri, String value, String dataType, String lang) {
        super(subjectUri, predicateUri);
        this.data= value;
        this.dataType = dataType;
        this.lang = lang;
    }

    public String getData() {
        return data;
    }

    public String getDataType() {
        return dataType;
    }

    public String getLang() {
        return lang;
    }

    @Override
    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle ids ){
        return policy.visit(ids, this );
    }
}
