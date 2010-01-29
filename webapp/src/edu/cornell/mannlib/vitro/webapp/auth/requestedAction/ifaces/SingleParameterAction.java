package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public abstract class SingleParameterAction implements RequestedAction {
    protected String subjectUri;

    public String getSubjectUri() {
        return subjectUri;
    }

    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }

}
