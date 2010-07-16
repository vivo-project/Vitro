/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * Represents the process of mapping an identifier that represents a user or
 * principle and a action they are requesting to true, representing authorized or
 * false, representing unauthorized.
 *
 * @author bdc34
 *
 */
public interface PolicyIface  {
    public PolicyDecision isAuthorized(IdentifierBundle whoToAuth, RequestedAction whatToAuth);

}
