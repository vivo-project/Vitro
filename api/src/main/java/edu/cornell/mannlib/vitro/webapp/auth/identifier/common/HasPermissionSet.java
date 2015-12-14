/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;

/**
 * The current user has this Permission, through one or more PermissionSets.
 */
public class HasPermissionSet extends AbstractCommonIdentifier implements
		Identifier, Comparable<HasPermissionSet> {
	public static Collection<HasPermissionSet> getIdentifiers(IdentifierBundle ids) {
		return getIdentifiersForClass(ids, HasPermissionSet.class);
	}

	public static Collection<PermissionSet> getPermissionSets(IdentifierBundle ids) {
		Set<PermissionSet> set = new HashSet<>();
		for (HasPermissionSet id : getIdentifiers(ids)) {
			set.add(id.getPermissionSet());
		}
		return set;
	}

	public static Collection<String> getPermissionSetUris(IdentifierBundle ids) {
		Set<String> set = new HashSet<>();
		for (HasPermissionSet id : getIdentifiers(ids)) {
			set.add(id.getPermissionSet().getUri());
		}
		return set;
	}
	
	private final PermissionSet permissionSet; // never null

	public HasPermissionSet(PermissionSet permissionSet) {
		if (permissionSet == null) {
			throw new NullPointerException("permissionSet may not be null.");
		}
		this.permissionSet = permissionSet;
	}

	public PermissionSet getPermissionSet() {
		return permissionSet;
	}

	@Override
	public String toString() {
		return "HasPermissionSet[" + permissionSet.getLabel() + "]";
	}

	@Override
	public int hashCode() {
		return permissionSet.getUri().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HasPermissionSet)) {
			return false;
		}
		HasPermissionSet that = (HasPermissionSet) obj;
		return this.permissionSet.getUri().equals(that.permissionSet.getUri());
	}

	@Override
	public int compareTo(HasPermissionSet that) {
		return this.permissionSet.getUri().compareTo(
				that.permissionSet.getUri());
	}
}
