/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.SingleParameterAction;

public class AddResource extends SingleParameterAction implements RequestedAction {

    private String typeUri;
    private String uri;

    public AddResource(String typeUri, String uri) {
        super();
        this.typeUri = typeUri;
        this.uri = uri;
    }

    //This should return a list of type URIs since an Indiviudal can be multiple types.
    public String getTypeUri() {
        return typeUri;
    }

    public void setTypeUri(String typeUri) {
        this.typeUri = typeUri;
    }

    //TODO: rename this method to avoid confusion with getURI()
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /** returns "java//edu.cornell.mannlib.vitro.webapp.auth.requestedAction.CreateResource" */
    public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }

    public PolicyDecision accept(VisitingPolicyIface policy, IdentifierBundle ids){
        return policy.visit(ids,this);
    }
}
