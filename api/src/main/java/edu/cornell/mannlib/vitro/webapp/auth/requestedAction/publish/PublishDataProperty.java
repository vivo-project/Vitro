/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;

/** Should we allow the user to publish this DataProperty in Linked Open Data? */
public class PublishDataProperty extends RequestedAction {
	private final DataProperty dataProperty;

	public PublishDataProperty(DataProperty dataProperty) {
		this.dataProperty = dataProperty;
	}

	public DataProperty getDataProperty() {
		return dataProperty;
	}

	@Override
	public String toString() {
		return "PublishDataProperty[" + dataProperty + "]";
	}
}
