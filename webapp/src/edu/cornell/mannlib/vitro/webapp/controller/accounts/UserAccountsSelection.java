/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;

/**
 * A group of accounts (might be empty), with the criteria that were used to
 * select them.
 */
public class UserAccountsSelection {
	private final UserAccountsSelectionCriteria criteria;
	private final List<UserAccount> userAccounts;
	private final int resultCount;

	public UserAccountsSelection(UserAccountsSelectionCriteria criteria,
			Collection<UserAccount> userAccounts, int resultCount) {
		this.criteria = criteria;
		this.userAccounts = Collections
				.unmodifiableList(new ArrayList<UserAccount>(userAccounts));
		this.resultCount = resultCount;
	}

	public UserAccountsSelectionCriteria getCriteria() {
		return criteria;
	}

	public List<UserAccount> getUserAccounts() {
		return userAccounts;
	}

	public int getResultCount() {
		return resultCount;
	}
}
