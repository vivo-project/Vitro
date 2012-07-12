/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * Creates an IdentifierBundle based only on the characteristics of the current
 * user, without considering other aspects of the current request.
 */
public interface UserBasedIdentifierBundleFactory extends
		IdentifierBundleFactory {
	/**
	 * Get the IdentifierBundle for this user. If user is null, return an empty
	 * bundle. Never returns null.
	 */
	public IdentifierBundle getIdentifierBundleForUser(UserAccount user);
}
