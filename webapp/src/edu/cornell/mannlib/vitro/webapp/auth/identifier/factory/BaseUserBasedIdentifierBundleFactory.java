/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.UserBasedIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Some fields and methods that are helpful to IdentifierBundleFactory classes.
 */
public abstract class BaseUserBasedIdentifierBundleFactory extends
		BaseIdentifierBundleFactory implements UserBasedIdentifierBundleFactory {

	public BaseUserBasedIdentifierBundleFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public final IdentifierBundle getIdentifierBundle(HttpServletRequest request) {
		return getIdentifierBundleForUser(LoginStatusBean
				.getCurrentUser(request));
	}

	@Override
	public abstract IdentifierBundle getIdentifierBundleForUser(UserAccount user);

}
