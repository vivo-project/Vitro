/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.bean;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * A property or faux property whose usage can be restricted according to the
 * user's role level.
 */
public interface RoleRestrictedProperty {
	String getDomainVClassURI();

	String getRangeVClassURI();

	String getURI();

	RoleLevel getHiddenFromDisplayBelowRoleLevel();

	RoleLevel getProhibitedFromUpdateBelowRoleLevel();

	RoleLevel getHiddenFromPublishBelowRoleLevel();
}
