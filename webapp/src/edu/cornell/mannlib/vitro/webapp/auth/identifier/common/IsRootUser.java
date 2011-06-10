/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * The current user is a root user.
 */
public class IsRootUser extends AbstractCommonIdentifier implements Identifier {
	public static boolean isRootUser(IdentifierBundle ids) {
		return !getIdentifiersForClass(ids, IsRootUser.class).isEmpty();
	}
	
	@Override
	public String toString() {
		return "IsRootUser";
	}

}
