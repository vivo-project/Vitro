/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IsUser;

/**
 * If the user is logged in, create an Identifier.
 */
public class IsUserFactory extends BaseIdentifierBundleFactory {

	public IsUserFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public IdentifierBundle getIdentifierBundle(HttpServletRequest req) {
		LoginStatusBean bean = LoginStatusBean.getBean(req);
		if (bean.isLoggedIn()) {
			return new ArrayIdentifierBundle(new IsUser(bean.getUserURI()));
		} else {
			return new ArrayIdentifierBundle();
		}
	}

}
