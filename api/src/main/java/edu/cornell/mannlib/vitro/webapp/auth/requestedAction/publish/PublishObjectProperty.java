/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.publish;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

/** Should we allow the user to publish this ObjectProperty in Linked Open Data? */
public class PublishObjectProperty extends RequestedAction {
	private final ObjectProperty objectProperty;

	public PublishObjectProperty(ObjectProperty objectProperty) {
		this.objectProperty = objectProperty;
	}

	public ObjectProperty getObjectProperty() {
		return objectProperty;
	}

	@Override
	public String toString() {
		return "PublishObjectProperty[" + objectProperty.getLocalName() + "]";
	}
}
