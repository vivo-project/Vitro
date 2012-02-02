/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsRootUser;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * If the user is logged in as a Root User, create an identifier.
 */
public class IsRootUserFactory extends BaseUserBasedIdentifierBundleFactory {

	public IsRootUserFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public IdentifierBundle getIdentifierBundleForUser(UserAccount user) {
		if ((user != null) && user.isRootUser()) {
			return new ArrayIdentifierBundle(IsRootUser.INSTANCE);
		} else {
			return new ArrayIdentifierBundle();
		}
	}

}
