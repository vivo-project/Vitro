/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.resource;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A common base class for resource-related actions.
 */
public abstract class AbstractResourceAction extends RequestedAction {
	private final String typeUri;
	private final String subjectUri;

	public AbstractResourceAction(String typeUri, String subjectUri) {
		this.typeUri = typeUri;
		this.subjectUri = subjectUri;
	}

	// This should return a list of type URIs since an Indiviudal can be
	// multiple types.
	public String getTypeUri() {
		return typeUri;
	}

	public String getSubjectUri() {
		return subjectUri;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " <" + subjectUri + ">";
	}
}
