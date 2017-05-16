/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
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

	@Override
	public String toString() {
		return "DisplayDataPropertyStatement["
				+ dataPropertyStatement.getIndividualURI() + "==>"
				+ dataPropertyStatement.getDatapropURI() + "==>"
				+ dataPropertyStatement.getData() + "]";
	}

}
