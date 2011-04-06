/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;

public class UploadFile implements AdminRequestedAction{
    protected String subjectUri;
    protected String predicateUri;

    public UploadFile(String subjectUri, String predicateUri) {
        super();
        this.subjectUri = subjectUri;
        this.predicateUri = predicateUri;
    }

    @Override
	public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }
}
