/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

/**
 * On what basis are we selecting user accounts?
 * 
 * Search terms are matched against email, and against firstName combined with
 * lastName. Searches are case-insensitive.
 */
public class UserAccountsSelectionCriteria {
	public static final int DEFAULT_ACCOUNTS_PER_PAGE = 25;

	public static final UserAccountsSelectionCriteria DEFAULT_CRITERIA = new UserAccountsSelectionCriteria(
			DEFAULT_ACCOUNTS_PER_PAGE, 1,
			UserAccountsOrdering.DEFAULT_ORDERING, "", "");

	/** How many accounts should we bring back, at most? */
	private final int accountsPerPage;

	/** What page are we on? (1-origin) */
	private final int pageIndex;

	/** How are they sorted? */
	private final UserAccountsOrdering orderBy;

	/** What role are we filtering by, if any? */
	private final String roleFilterUri;

	/** What term are we searching on, if any? */
	private final String searchTerm;

	public UserAccountsSelectionCriteria(int accountsPerPage, int pageIndex,
			UserAccountsOrdering orderBy, String roleFilterUri,
			String searchTerm) {
		if (accountsPerPage <= 0) {
			throw new IllegalArgumentException("accountsPerPage "
					+ "must be a positive integer, not " + accountsPerPage);
		}
		this.accountsPerPage = accountsPerPage;

		if (pageIndex <= 0) {
			throw new IllegalArgumentException("pageIndex must be a "
					+ "non-negative integer, not " + pageIndex);
		}
		this.pageIndex = pageIndex;

		this.orderBy = nonNull(orderBy, UserAccountsOrdering.DEFAULT_ORDERING);

		this.roleFilterUri = nonNull(roleFilterUri, "");
		this.searchTerm = nonNull(searchTerm, "");
	}

	public int getAccountsPerPage() {
		return accountsPerPage;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public UserAccountsOrdering getOrderBy() {
		return orderBy;
	}

	public String getRoleFilterUri() {
		return roleFilterUri;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	private <T> T nonNull(T t, T nullValue) {
		return (t == null) ? nullValue : t;
	}

	@Override
	public String toString() {
		return "UserAccountsSelectionCriteria[accountsPerPage="
				+ accountsPerPage + ", pageIndex=" + pageIndex + ", orderBy="
				+ orderBy + ", roleFilterUri='" + roleFilterUri
				+ "', searchTerm='" + searchTerm + "']";
	}
}
