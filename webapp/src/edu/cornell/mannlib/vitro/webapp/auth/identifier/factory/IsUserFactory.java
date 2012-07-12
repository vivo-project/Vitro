/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import javax.servlet.ServletContext;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsUser;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * If the user is logged in, create an Identifier.
 */
public class IsUserFactory extends BaseUserBasedIdentifierBundleFactory {

	public IsUserFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public IdentifierBundle getIdentifierBundleForUser(UserAccount user) {
		if (user == null) {
			return new ArrayIdentifierBundle();
		} else {
			return new ArrayIdentifierBundle(new IsUser(user.getUri()));
		}
	}

}
