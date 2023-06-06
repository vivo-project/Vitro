/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.common.IdentifierPermissionSetProvider;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Figure out what PermissionSets the user is entitled to have.
 */
public class HasPermissionSetFactory extends BaseUserBasedIdentifierBundleFactory {
	private static final Log log = LogFactory.getLog(HasPermissionSetFactory.class);

	@Override
	public IdentifierBundle getIdentifierBundleForUser(UserAccount user) {
		IdentifierBundle ids = new ArrayIdentifierBundle();
		if (user != null) {
			for (String psUri : user.getPermissionSetUris()) {
				PermissionSet ps = uaDao.getPermissionSetByUri(psUri);
				if (ps != null) {
					ids.add(new IdentifierPermissionSetProvider(ps));
				}
			}
		}
		return ids;
	}

}
