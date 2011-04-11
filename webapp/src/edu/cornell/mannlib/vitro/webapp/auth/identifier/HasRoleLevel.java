/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * The current user has this RoleLevel.
 */
public class HasRoleLevel implements Identifier {
	private final RoleLevel roleLevel;

	public HasRoleLevel(RoleLevel roleLevel) {
		this.roleLevel = roleLevel;
	}

	public RoleLevel getRoleLevel() {
		return roleLevel;
	}
}
