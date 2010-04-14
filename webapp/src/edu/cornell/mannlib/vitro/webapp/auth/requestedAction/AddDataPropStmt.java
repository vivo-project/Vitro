/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

public class AddDataPropStmt  implements RequestedAction {

    protected String resourceUri;
    protected String dataPropUri;
    protected String data;
    protected String dataType;
    protected String lang;
    
    public AddDataPropStmt(String resourceUri, String dataPropUri, String value, String dataType, String lang) {
        super();
        this.resourceUri = resourceUri;
        this.dataPropUri = dataPropUri;
        this.data= value;
        this.dataType = dataType;
        this.lang = lang;
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

    public String getData() {
        return data;
    }

    public void setData(String value) {
        this.data= value;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }
    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle ids ){
        return policy.visit(ids, this );
    }
}
