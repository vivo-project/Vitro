/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.AdminRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/** Should we allow the user to load an ontology? */
public class LoadOntology extends RequestedAction implements AdminRequestedAction{
    protected String ontologyUrl;

    public String getOntologyUrl() {
        return ontologyUrl;
    }

    public void setOntologyUrl(String ontologyUrl) {
        this.ontologyUrl = ontologyUrl;
    }
}
