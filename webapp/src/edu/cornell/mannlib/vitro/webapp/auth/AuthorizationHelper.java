/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth;

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

public class AuthorizationHelper {

    private static final Log log = LogFactory.getLog(AuthorizationHelper.class);
    
    private VitroRequest vreq;
    
    public AuthorizationHelper(VitroRequest vreq) {
        this.vreq = vreq;
    }

    public boolean isAuthorizedForRequestedAction(RequestedAction action) {
        PolicyIface policy = getPolicies();
        PolicyDecision dec = policy.isAuthorized(getIdentifiers(), action);
        if (dec != null && dec.getAuthorized() == Authorization.AUTHORIZED) {
            log.debug("Authorized because self-editing.");
            return true;
        } else {
            log.debug("Not Authorized even though self-editing: "
                    + ((dec == null) ? "null" : dec.getMessage() + ", "
                            + dec.getDebuggingInfo()));
            return false;
        }
    }

    private PolicyIface getPolicies() {
        return RequestPolicyList.getPolicies(vreq);
    }

    private IdentifierBundle getIdentifiers() {
        return RequestIdentifiers.getIdBundleForRequest(vreq);
    }

}
