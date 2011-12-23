/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.Permission;

/**
 * The current user has this Permission, through one or more PermissionSets.
 */
public class HasPermission extends AbstractCommonIdentifier implements
		Identifier, Comparable<HasPermission> {
	public static Collection<HasPermission> getIdentifiers(IdentifierBundle ids) {
		return getIdentifiersForClass(ids, HasPermission.class);
	}

	public static Collection<Permission> getPermissions(IdentifierBundle ids) {
		Set<Permission> set = new HashSet<Permission>();
		for (HasPermission id : getIdentifiers(ids)) {
			set.add(id.getPermission());
		}
		return set;
	}

	private final Permission permission; // never null

	public HasPermission(Permission permission) {
		if (permission == null) {
			throw new NullPointerException("permission may not be null.");
		}
		this.permission = permission;
	}

	public Permission getPermission() {
		return permission;
	}

	@Override
	public String toString() {
		return "HasPermission[" + permission + "]";
	}

	@Override
	public int hashCode() {
		return permission.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof HasPermission)) {
			return false;
		}
		HasPermission that = (HasPermission) obj;
		return this.permission.equals(that.permission);
	}

	@Override
	public int compareTo(HasPermission that) {
		return this.permission.compareTo(that.permission);
	}
}
