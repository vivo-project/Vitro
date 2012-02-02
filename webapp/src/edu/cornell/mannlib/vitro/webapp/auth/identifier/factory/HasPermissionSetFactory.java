/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.HasPermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Figure out what PermissionSets the user is entitled to have.
 */
public class HasPermissionSetFactory extends
		BaseUserBasedIdentifierBundleFactory {
	private static final Log log = LogFactory
			.getLog(HasPermissionFactory.class);

	public HasPermissionSetFactory(ServletContext ctx) {
		super(ctx);
	}

	@Override
	public IdentifierBundle getIdentifierBundleForUser(UserAccount user) {
		IdentifierBundle ids = new ArrayIdentifierBundle();
		if (user != null) {
			for (String psUri : user.getPermissionSetUris()) {
				PermissionSet ps = uaDao.getPermissionSetByUri(psUri);
				if (ps != null) {
					ids.add(new HasPermissionSet(ps));
				}
			}
		}
		return ids;
	}

}
