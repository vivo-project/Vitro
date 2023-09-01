/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObjectImpl;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;

public class SimpleAuthorizationRequest extends AuthorizationRequest {

    private AccessObject object;

    public AccessObject getObject() {
        return object;
    }

    public AccessOperation getOperation() {
        return operation;
    }

    private AccessOperation operation;

    public SimpleAuthorizationRequest(AccessObject object, AccessOperation operation) {
        this.object = object;
        this.operation = operation;
    }

    public SimpleAuthorizationRequest(String namedAccessObject) {
        this.object = new AccessObjectImpl(namedAccessObject);
        this.operation = AccessOperation.EXECUTE;
    }

    @Override
    public DecisionResult getPredefinedDecision() {
        return DecisionResult.INCONCLUSIVE;
    }

    @Override
    public AccessObject getAccessObject() {
        return object;
    }

    @Override
    public AccessOperation getAccessOperation() {
        return operation;
    }

}
