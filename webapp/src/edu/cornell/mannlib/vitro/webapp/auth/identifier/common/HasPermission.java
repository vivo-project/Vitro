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
		Identifier {
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

	private final Permission permission;

	public HasPermission(Permission permission) {
		this.permission = permission;
	}

	public Permission getPermission() {
		return permission;
	}

	@Override
	public String toString() {
		return "HasPermission[" + permission + "]";
	}
}
