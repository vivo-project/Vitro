/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/** Should we allow the user to remove a user account */
public class RemoveUser extends RequestedAction implements AdminRequestedAction{
    protected String userUri;

    public String getUserUri() {
        return userUri;
    }

    public void setUserUri(String userUri) {
        this.userUri = userUri;
    }
}
