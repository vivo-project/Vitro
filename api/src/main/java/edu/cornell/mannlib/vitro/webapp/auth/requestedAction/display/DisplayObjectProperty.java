/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

/** Should we allow the user to see this ObjectProperty? */
public class DisplayObjectProperty extends RequestedAction {
	private final ObjectProperty objectProperty;

	public DisplayObjectProperty(ObjectProperty objectProperty) {
		this.objectProperty = objectProperty;
	}

	public ObjectProperty getObjectProperty() {
		return objectProperty;
	}

	@Override
	public String toString() {
		return "DisplayObjectProperty[" + objectProperty + "]";
	}

}
