/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasRoleLevel;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean.RoleLevel;

/**
 * Create an identifier that shows the role level of the current user, or
 * PUBLIC if the user is not logged in.
 */
public class HasRoleLevelFactory extends BaseIdentifierBundleFactory {

	public HasRoleLevelFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public IdentifierBundle getIdentifierBundle(HttpServletRequest req) {
		RoleLevel roleLevel = RoleLevel.getRoleFromLoginStatus(req);
		return new ArrayIdentifierBundle(new HasRoleLevel(roleLevel));
	}

}
