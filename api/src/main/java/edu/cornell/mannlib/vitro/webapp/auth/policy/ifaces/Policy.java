/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;

/**
 * Represents the process of mapping an identifier that represents a user or
 * principle and a action they are requesting to true, representing authorized or
 * false, representing unauthorized.
 *
 * @author bdc34
 *
 */
public interface Policy  {
    public PolicyDecision decide(AuthorizationRequest ar);

    public default String getUri() {
        return "";
    }
    
    public default long getPriority() {
        return 0;
    }
}
