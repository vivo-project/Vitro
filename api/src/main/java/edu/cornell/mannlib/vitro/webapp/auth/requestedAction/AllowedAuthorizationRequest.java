/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;

public class AllowedAuthorizationRequest extends AuthorizationRequest {

    private DecisionResult decision;

    public AllowedAuthorizationRequest() {
        decision = DecisionResult.AUTHORIZED;
    }

    @Override
    public DecisionResult getPredefinedDecision() {
        return decision;
    }

    @Override
    public AccessObject getAccessObject() {
        return null;
    }

    @Override
    public AccessOperation getAccessOperation() {
        return null;
    }

}
