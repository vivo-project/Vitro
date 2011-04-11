/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

/** Should we let the user see this DataPropertyStatement? */
public class DisplayDataPropertyStatement extends RequestedAction {
	private final DataPropertyStatement dataPropertyStatement;

	public DisplayDataPropertyStatement(
			DataPropertyStatement dataPropertyStatement) {
		this.dataPropertyStatement = dataPropertyStatement;
	}

	public DataPropertyStatement getDataPropertyStatement() {
		return dataPropertyStatement;
	}
}
