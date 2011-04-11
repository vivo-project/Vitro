/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;

/** Should we allow the user to see this DataProperty? */
public class DisplayDataProperty extends RequestedAction {
	private final DataProperty dataProperty;

	public DisplayDataProperty(DataProperty dataProperty) {
		this.dataProperty = dataProperty;
	}

	public DataProperty getDataProperty() {
		return dataProperty;
	}
}
