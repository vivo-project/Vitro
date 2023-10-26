/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import java.util.Collections;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;

public abstract class AuthorizationRequest {

    public static final AuthorizationRequest UNAUTHORIZED = new ForbiddenAuthorizationRequest();
    public static final AuthorizationRequest AUTHORIZED = new AllowedAuthorizationRequest();
    
    public static enum WRAP_TYPE { AND , OR } ; 
    
    IdentifierBundle ids;
    private List<String> editorUris = Collections.emptyList();
    List<String> roleUris = Collections.emptyList();

    
    public void setRoleUris(List<String> roleUris) {
        this.roleUris = roleUris;
    }

    public WRAP_TYPE getWrapType() {
        return null;
    }

    public DecisionResult getPredefinedDecision(){
        return DecisionResult.INCONCLUSIVE;
    }
    
    public List<AuthorizationRequest> getItems() {
        return Collections.emptyList();
    }
    
    public abstract AccessObject getAccessObject();
    
    public abstract AccessOperation getAccessOperation();

    public IdentifierBundle getIds() {
        return ids;
    }
    
    public List<String> getRoleUris() {
        return roleUris;
    }
    
    public void setIds(IdentifierBundle ids) {
        this.ids = ids;
    }

    public void setEditorUris(List<String> list) {
        editorUris = list;
    }

    public List<String> getEditorUris(){
        return editorUris;
    }

    public static AuthorizationRequest or(AuthorizationRequest fist, AuthorizationRequest second) {
        if (fist == null) {
            return second;
        } else if (second == null) {
            return fist;
        } else {
            return new OrAuthorizationRequest(fist, second);
        }
    }
    
    public AuthorizationRequest and(AuthorizationRequest second) {
        return new AndAuthorizationRequest(this, second);
    }
    
    @Override
    public String toString() {
        String result = "";
        if (!getRoleUris().isEmpty()) {
            result += String.format("User with roles '%s' ", getRoleUris().toString());
        }
        if (!getEditorUris().isEmpty()) {
            result += String.format(" profile uris '%s' ", getEditorUris().toString());
        }
        result += String.format(" requested '%s' ", getAccessOperation());
        result += String.format(" on '%s' ", getAccessObject());
        return result;
    }
}