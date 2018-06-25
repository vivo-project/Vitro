/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier.common;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.Identifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;

/**
 * The current user is a root user.
 */
public class IsRootUser extends AbstractCommonIdentifier implements Identifier {
	public static final IsRootUser INSTANCE = new IsRootUser();

	public static boolean isRootUser(IdentifierBundle ids) {
		return !getIdentifiersForClass(ids, IsRootUser.class).isEmpty();
	}

	/** Enforce the singleton pattern. */
	private IsRootUser() {
		// Nothing to initialize.
	}

	@Override
	public String toString() {
		return "IsRootUser";
	}

}
