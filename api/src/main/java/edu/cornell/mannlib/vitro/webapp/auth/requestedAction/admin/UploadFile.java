/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;

/** Should we allow the user to upload a file? */
public class UploadFile extends RequestedAction implements AdminRequestedAction{
    protected String subjectUri;
    protected String predicateUri;

    public UploadFile(String subjectUri, String predicateUri) {
        super();
        this.subjectUri = subjectUri;
        this.predicateUri = predicateUri;
    }
}
