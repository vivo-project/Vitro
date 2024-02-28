/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

public abstract class AuthorizationRequest {

    public static final AuthorizationRequest UNAUTHORIZED = new ForbiddenAuthorizationRequest();
    public static final AuthorizationRequest AUTHORIZED = new AllowedAuthorizationRequest();
    
    public static enum WRAP_TYPE { AND , OR } ; 
    
    private UserAccount userAccount;

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

    public Set<String> getRoleUris() {
        return userAccount.getPermissionSetUris();
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
    
    public UserAccount getUserAccount() {
        return userAccount;
    }

    public Set<String> getEditorUris() {
        return userAccount.getProxiedIndividualUris();
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
        result += String.format(" requested '%s' ", getAccessOperation());
        result += String.format(" on '%s' ", getAccessObject());
        return result;
    }

    public boolean isRootUser() {
        return userAccount.isRootUser();
    }

}
