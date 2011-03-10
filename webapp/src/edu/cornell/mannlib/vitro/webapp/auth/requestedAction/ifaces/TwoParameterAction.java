/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

public abstract class TwoParameterAction implements RequestedAction {
    protected String resourceUri;
    protected String secondUri;

    public String getResourceUri() {
        return resourceUri;
    }
    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }
    public String getSecondUri() {
        return secondUri;
    }
    public void setSecondUri(String secondUri) {
        this.secondUri = secondUri;
    }

}
