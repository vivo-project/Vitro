/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Reflects a collection of Permissions that can be made available to a user.
 * Similar to the concept of a Role.
 */
public class PermissionSet {
	/** This may be empty, but it should never be null. */
	private String uri = "";

	/** This may be empty, but it should never be null. */
	private String label = "";

	/** This may be empty, but it should never be null. */
	private Set<String> permissionUris = Collections.emptySet();

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = (uri == null) ? "" : uri;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = (label == null) ? "" : label;
	}

	public Set<String> getPermissionUris() {
		return permissionUris;
	}

	public void setPermissionUris(Collection<String> permissionUris) {
		if (permissionUris == null) {
			throw new NullPointerException("permissionUris may not be null.");
		}
		this.permissionUris = new HashSet<String>(permissionUris);
	}

}
