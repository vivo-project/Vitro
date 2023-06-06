package edu.cornell.mannlib.vitro.webapp.auth.requestedAction;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DecisionResult;

public class ForbiddenAuthorizationRequest extends AuthorizationRequest {

    private DecisionResult predefinedDecision;

    public ForbiddenAuthorizationRequest() {
        predefinedDecision = DecisionResult.UNAUTHORIZED;
    }

    @Override
    public DecisionResult getPredefinedDecision() {
        return predefinedDecision;
    }

    @Override
    public AccessObject getAccessObject() {
        return null;
    }

    @Override
    public AccessOperation getAccessOperation() {
        return null;
    }

    @Override
    public IdentifierBundle getIds() {
        // TODO Auto-generated method stub
        return null;
    }
}
