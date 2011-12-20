/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RequestPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class EditingPolicyHelper {
    private static final Log log = LogFactory.getLog(EditingPolicyHelper.class);  
    
    private final PolicyIface policy;
    private final IdentifierBundle ids;
    
    public EditingPolicyHelper(VitroRequest vreq) {
        this.policy = RequestPolicyList.getPolicies(vreq);
        this.ids = RequestIdentifiers.getIdBundleForRequest(vreq);
    }
    
    protected boolean isAuthorizedAction(RequestedAction action) {
        PolicyDecision decision = getPolicyDecision(action);
        return (decision != null && decision.getAuthorized() == Authorization.AUTHORIZED);
    }

    private PolicyDecision getPolicyDecision(RequestedAction action) {
        return policy.isAuthorized(ids, action);
    }
}
