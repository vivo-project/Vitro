/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.accountmanagement;

import static edu.cornell.mannlib.vitro.webapp.controller.freemarker.accountmanagement.UserAccountsOrdering.DEFAULT_ORDERING;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UserAccountsSelectionCriteriaTest {
	private UserAccountsSelectionCriteria criteria;

	@Test(expected = IllegalArgumentException.class)
	public void accountsPerPageOutOfRange() {
		criteria = create(0, 10, DEFAULT_ORDERING, "role", "search");
	}

	@Test(expected = IllegalArgumentException.class)
	public void pageIndexOutOfRange() {
		criteria = create(10, -1, DEFAULT_ORDERING, "role", "search");
	}

	@Test
	public void orderByIsNull() {
		criteria = create(10, 1, null, "role", "search");
		assertEquals("ordering", UserAccountsOrdering.DEFAULT_ORDERING,
				criteria.getOrderBy());
	}

	@Test
	public void roleFilterUriIsNull() {
		criteria = create(10, 1, DEFAULT_ORDERING, null, "search");
		assertEquals("roleFilter", "", criteria.getRoleFilterUri());
	}

	@Test
	public void searchTermIsNull() {
		criteria = create(10, 1, DEFAULT_ORDERING, "role", null);
		assertEquals("searchTerm", "", criteria.getSearchTerm());
	}

	private UserAccountsSelectionCriteria create(int accountsPerPage,
			int pageIndex, UserAccountsOrdering orderBy, String roleFilterUri,
			String searchTerm) {
		return new UserAccountsSelectionCriteria(accountsPerPage, pageIndex,
				orderBy, roleFilterUri, searchTerm);
	}

}
