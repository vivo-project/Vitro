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

    public AuthorizationRequest or(AuthorizationRequest authRequest) {
        if (authRequest == null) {
            return this;
        } else {
            return new OrAuthorizationRequest(this, authRequest);
        }
    };
    
    public AuthorizationRequest and(AuthorizationRequest authRequest) {
        if (authRequest == null) {
            return this;
        } else {
            return new AndAuthorizationRequest(this, authRequest);
        }
    };
    
    public static AuthorizationRequest or(AuthorizationRequest fist, AuthorizationRequest second) {
        if (fist == null) {
            return second;
        } else if (second == null) {
            return fist;
        } else {
            return new OrAuthorizationRequest(fist, second);
        }
    }

    public WRAP_TYPE getType() {
        // TODO Auto-generated method stub
        return null;
    };
    
}