/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * The current user has this RoleLevel.
 */
public class HasRoleLevel extends AbstractCommonIdentifier implements Identifier {
	public static Collection<HasRoleLevel> getIdentifiers(IdentifierBundle ids) {
		return getIdentifiersForClass(ids, HasRoleLevel.class);
	}

	public static Collection<String> getRoleLevelUris(IdentifierBundle ids) {
		Set<String> set = new HashSet<String>();
		for (HasRoleLevel id : getIdentifiers(ids)) {
			set.add(id.getRoleLevel().getURI());
		}
		return set;
	}
	
	public static RoleLevel getUsersRoleLevel(IdentifierBundle whoToAuth) {
		Collection<HasRoleLevel> roleIds = getIdentifiers(whoToAuth);
		if (roleIds.isEmpty()) {
			return RoleLevel.PUBLIC;
		} else {
			return roleIds.iterator().next().getRoleLevel();
		}
	}

	private final RoleLevel roleLevel;

	public HasRoleLevel(RoleLevel roleLevel) {
		this.roleLevel = roleLevel;
	}

	public RoleLevel getRoleLevel() {
		return roleLevel;
	}

	@Override
	public String toString() {
		return "HasRoleLevel[" + roleLevel + "]";
	}
}
