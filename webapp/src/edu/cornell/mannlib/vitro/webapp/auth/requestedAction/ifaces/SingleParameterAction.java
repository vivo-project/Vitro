/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

/**
 * A base class for actions that involve a single URI.
 */
public abstract class SingleParameterAction extends RequestedAction {
    protected String subjectUri;

    public String getSubjectUri() {
        return subjectUri;
    }

    public void setSubjectUri(String subjectUri) {
        this.subjectUri = subjectUri;
    }

    @Override
	public String toString(){
        return this.getClass().getName() + " <"+subjectUri+">"; 
    }
}
