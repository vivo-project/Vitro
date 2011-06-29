/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * TODO
 */
public class UserAccountsAssociatedProfileHelper {
	private static final Log log = LogFactory
			.getLog(UserAccountsAssociatedProfileHelper.class);

	/**
	 * This profile (if it exists) should be associated with this UserAccount.
	 * No other profile should be associated with this UserAccount. Make it so.
	 */
	public static void reconcile(UserAccount userAccount,
			String associatedProfileUri) {
		log.error("UserAccountsAssociatedProfileHelper.reconcile() not implemented.");
	}

}
