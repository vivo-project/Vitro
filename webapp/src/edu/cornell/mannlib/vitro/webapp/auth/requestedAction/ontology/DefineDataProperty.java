/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.OntoRequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestActionConstants;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.SingleParameterAction;

public class DefineDataProperty extends SingleParameterAction implements OntoRequestedAction{
    @Override
	public String getURI() {
        return RequestActionConstants.actionNamespace + this.getClass().getName();
    }
}
