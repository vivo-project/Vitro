/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/** Should we let the user see this ObjectPropertyStatement? */
public class DisplayObjectPropertyStatement extends RequestedAction {
	private final ObjectPropertyStatement objectPropertyStatement;

	public DisplayObjectPropertyStatement(
			ObjectPropertyStatement objectPropertyStatement) {
		this.objectPropertyStatement = objectPropertyStatement;
	}

	public ObjectPropertyStatement getObjectPropertyStatement() {
		return objectPropertyStatement;
	}
}
