/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

public abstract class SingleParameterAction implements RequestedAction {
    protected String subjectUri;

    public String getSubjectUri() {
        return subjectUri;
    }

    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }

    public String toString(){
        return this.getClass().getName() + " <"+subjectUri+">"; 
    }
}
