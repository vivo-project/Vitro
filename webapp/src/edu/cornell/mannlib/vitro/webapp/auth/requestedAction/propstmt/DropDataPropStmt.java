/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;

public class DropDataPropStmt extends AbstractDataPropertyAction {

    private final DataPropertyStatement dataPropStmt;
    
    public DropDataPropStmt(DataPropertyStatement dps){
    	super(dps.getIndividualURI(),dps.getDatapropURI() );
        this.dataPropStmt = dps;
    }

    public DropDataPropStmt(String subjectUri, String predicateUri, String data) {
    	super(subjectUri, predicateUri);
        dataPropStmt = new DataPropertyStatementImpl();
        dataPropStmt.setIndividualURI(subjectUri);
        dataPropStmt.setDatapropURI(predicateUri);
        dataPropStmt.setData(data);        
    }
    
    @Override
    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle whoToAuth) {
        return policy.visit(whoToAuth,this);
    }

    public String data(){ return dataPropStmt.getData(); }
    public String lang(){ return dataPropStmt.getLanguage(); }
    public String datatype(){return dataPropStmt.getDatatypeURI(); }
    
    /*
     * TODO: needs to be fixed to work with lang/datatype literals
     */
    
    /*
    
    protected String resourceUri;
    protected String dataPropUri;
    protected String value;

    //TODO: needs to be fixed to work with lang/datatype literals
    public DropDataPropStmt(String resourceUri, String dataPropUri, String value) {
        super();
        this.resourceUri = resourceUri;
        this.dataPropUri = dataPropUri;
        this.value = value;
    }

    public String getDataPropUri() {
        return dataPropUri;
    }

    public void setDataPropUri(String dataPropUri) {
        this.dataPropUri = dataPropUri;
    }

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }
//TODO: needs to be fixed to work with lang/datatype literals
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }
    public PolicyDecision accept(PolicyIface policy, IdentifierBundle ids){
        return policy.visit(ids,this);
    } */
}
