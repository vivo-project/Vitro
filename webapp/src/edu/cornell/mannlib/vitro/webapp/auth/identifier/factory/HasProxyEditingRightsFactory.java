/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasProxyEditingRights;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Find out what Profiles the User can edit through proxy.
 */
public class HasProxyEditingRightsFactory extends BaseIdentifierBundleFactory {

	public HasProxyEditingRightsFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public IdentifierBundle getIdentifierBundle(HttpServletRequest req) {
		ArrayIdentifierBundle ids = new ArrayIdentifierBundle();

		UserAccount user = LoginStatusBean.getCurrentUser(req);
		if (user != null) {
			for (String proxiedUri : user.getProxiedIndividualUris()) {
				ids.add(new HasProxyEditingRights(proxiedUri));
			}
		}

		return ids;
	}

}
