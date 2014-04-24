/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.display;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

/** Should we let the user see this ObjectPropertyStatement? */
public class DisplayObjectPropertyStatement extends RequestedAction {
	private final String subjectUri;
	private final ObjectProperty property;
	private final String objectUri;

	public DisplayObjectPropertyStatement(String subjectUri,
			ObjectProperty property, String objectUri) {
		this.subjectUri = subjectUri;
		this.property = property;
		this.objectUri = objectUri;
	}

	public String getSubjectUri() {
		return subjectUri;
	}

	public ObjectProperty getProperty() {
		return property;
	}

	public String getObjectUri() {
		return objectUri;
	}

	@Override
	public String toString() {
		return "DisplayObjectPropertyStatement[" + subjectUri + "==>"
				+ property.getURI() + "==>" + objectUri + "]";
	}

}
