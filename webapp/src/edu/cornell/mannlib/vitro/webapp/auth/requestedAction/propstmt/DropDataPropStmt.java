/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;

public class DropDataPropStmt implements RequestedAction {

    final DataPropertyStatement dataPropStmt;
    
    public DropDataPropStmt(DataPropertyStatement dps){
        this.dataPropStmt = dps;
    }

    public DropDataPropStmt(String subjectUri, String predicateUri, String data) {
        dataPropStmt = new DataPropertyStatementImpl();
        dataPropStmt.setIndividualURI(subjectUri);
        dataPropStmt.setDatapropURI(predicateUri);
        dataPropStmt.setData(data);        
    }
    
    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle whoToAuth) {
        return policy.visit(whoToAuth,this);
    }

    //TODO: rename this method to something like getUriOfSubject    
    public String uriOfSubject(){ return dataPropStmt.getIndividualURI(); }
    
    //TODO: rename this method to something like getUriOfPredicate
    public String uriOfPredicate(){ return dataPropStmt.getDatapropURI(); }
    
    public String data(){ return dataPropStmt.getData(); }
    public String lang(){ return dataPropStmt.getLanguage(); }
    public String datatype(){return dataPropStmt.getDatatypeURI(); }
    
    
    public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }

    public String toString(){ 
        return "DropDataPropStmt <"+dataPropStmt.getIndividualURI()+"> <"+dataPropStmt.getDatapropURI()+">" ;
    }
    
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
