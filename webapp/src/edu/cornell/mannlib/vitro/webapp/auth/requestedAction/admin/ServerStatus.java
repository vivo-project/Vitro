/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;

/**
 * Represents a request to view information about the server status.
 * @author bdc34
 *
 */
public class ServerStatus implements AdminRequestedAction {

    @Override
	public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }
}
