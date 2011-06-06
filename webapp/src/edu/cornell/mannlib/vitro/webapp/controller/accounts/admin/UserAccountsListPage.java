/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import static edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelectionCriteria.DEFAULT_ACCOUNTS_PER_PAGE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelection;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelectionCriteria;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelector;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Direction;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Field;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Handle the List page.
 * 
 * TODO: agree with Manolo how to do the column heads as links that change the
 * sort order
 * 
 * TODO: auto-complete
 */
public class UserAccountsListPage extends UserAccountsPage {
	private static final Log log = LogFactory
			.getLog(UserAccountsListPage.class);

	public static final String PARAMETER_ACCOUNTS_PER_PAGE = "accountsPerPage";
	public static final String PARAMETER_PAGE_INDEX = "pageIndex";
	public static final String PARAMETER_ORDERING_DIRECTION = "orderDirection";
	public static final String PARAMETER_ORDERING_FIELD = "orderField";
	public static final String PARAMETER_ROLE_FILTER_URI = "roleFilterUri";
	public static final String PARAMETER_SEARCH_TERM = "searchTerm";
	public static final String PARAMETER_NEW_USER_URI = "newUserUri";
	public static final String PARAMETER_UPDATED_USER_URI = "updatedUserUri";
	public static final String FLAG_UPDATED_USER_PW = "updatedUserPw";
	public static final String FLAG_USERS_DELETED = "usersDeleted";

	private static final String TEMPLATE_NAME = "userAccounts-list.ftl";

	private UserAccountsSelectionCriteria criteria = UserAccountsSelectionCriteria.DEFAULT_CRITERIA;

	public UserAccountsListPage(VitroRequest vreq) {
		super(vreq);
		parseParameters();
	}

	/**
	 * Build the criteria from the request parameters.
	 */
	private void parseParameters() {
		int accountsPerPage = getIntegerParameter(PARAMETER_ACCOUNTS_PER_PAGE,
				DEFAULT_ACCOUNTS_PER_PAGE);
		int pageIndex = getIntegerParameter(PARAMETER_PAGE_INDEX, 1);

		Direction orderingDirection = Direction.fromKeyword(vreq
				.getParameter(PARAMETER_ORDERING_DIRECTION));
		Field orderingField = Field.fromName(vreq
				.getParameter(PARAMETER_ORDERING_FIELD));
		UserAccountsOrdering ordering = new UserAccountsOrdering(orderingField,
				orderingDirection);

		String roleFilterUri = getStringParameter(PARAMETER_ROLE_FILTER_URI, "");
		String searchTerm = getStringParameter(PARAMETER_SEARCH_TERM, "");

		criteria = new UserAccountsSelectionCriteria(accountsPerPage,
				pageIndex, ordering, roleFilterUri, searchTerm);
		log.debug("selection criteria is: " + criteria);
	}

	/**
	 * Build the selection criteria from the request, select the accounts, and
	 * create the ResponseValues to display the page.
	 */
	public ResponseValues showPage() {
		UserAccountsSelection selection = UserAccountsSelector.select(
				userAccountsModel, criteria);
		Map<String, Object> body = buildTemplateBodyMap(selection);
		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	/**
	 * We just came from adding a new account. Show the list with a message.
	 */
	public ResponseValues showPageWithNewAccount(UserAccount userAccount,
			boolean emailWasSent) {
		UserAccountsSelection selection = UserAccountsSelector.select(
				userAccountsModel, criteria);
		Map<String, Object> body = buildTemplateBodyMap(selection);

		body.put("newUserAccount", new UserAccountWrapper(userAccount,
				Collections.<String> emptyList()));
		if (emailWasSent) {
			body.put("emailWasSent", Boolean.TRUE);
		}

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	/**
	 * We just came from editing an account. Show the list with a message.
	 */
	public ResponseValues showPageWithUpdatedAccount(UserAccount userAccount,
			boolean emailWasSent) {
		UserAccountsSelection selection = UserAccountsSelector.select(
				userAccountsModel, criteria);
		Map<String, Object> body = buildTemplateBodyMap(selection);

		body.put("updatedUserAccount", new UserAccountWrapper(userAccount,
				Collections.<String> emptyList()));
		if (emailWasSent) {
			body.put("emailWasSent", Boolean.TRUE);
		}

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	/**
	 * We just came from deleting accounts. Show the list with a message.
	 */
	public ResponseValues showPageWithDeletions(Collection<String> deletedUris) {
		UserAccountsSelection selection = UserAccountsSelector.select(
				userAccountsModel, criteria);
		Map<String, Object> body = buildTemplateBodyMap(selection);

		body.put("deletedAccountCount", deletedUris.size());

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	private Map<String, Object> buildTemplateBodyMap(
			UserAccountsSelection selection) {
		Map<String, Object> body = new HashMap<String, Object>();

		body.put("accountsPerPage", criteria.getAccountsPerPage());
		body.put("pageIndex", criteria.getPageIndex());
		body.put("orderDirection", criteria.getOrderBy().getDirection().keyword);
		body.put("orderField", criteria.getOrderBy().getField().name);
		body.put("roleFilterUri", criteria.getRoleFilterUri());
		body.put("searchTerm", criteria.getSearchTerm());

		body.put("accounts", wrapUserAccounts(selection));
		body.put("total", selection.getResultCount());
		body.put("page", buildPageMap(selection));

		body.put("formUrls", buildUrlsMap());
		body.put("roles", buildRolesList());

		body.put("messages", buildMessagesMap());

		return body;
	}

	private Map<String, Integer> buildPageMap(UserAccountsSelection selection) {
		int currentPage = selection.getCriteria().getPageIndex();

		float pageCount = ((float) selection.getResultCount())
				/ selection.getCriteria().getAccountsPerPage();
		int lastPage = (int) Math.ceil(pageCount);

		Map<String, Integer> map = new HashMap<String, Integer>();

		map.put("current", currentPage);
		map.put("first", 1);
		map.put("last", lastPage);

		if (currentPage < lastPage) {
			map.put("next", currentPage + 1);
		}
		if (currentPage > 1) {
			map.put("previous", currentPage - 1);
		}

		return map;
	}

	private Map<String, Object> buildMessagesMap() {
		Map<String, Object> map = new HashMap<String, Object>();

		UserAccount newUser = getUserFromUriParameter(PARAMETER_NEW_USER_URI);
		if (newUser != null) {
			map.put("newUser", newUser);
		}

		UserAccount updatedUser = getUserFromUriParameter(PARAMETER_UPDATED_USER_URI);
		if (updatedUser != null) {
			map.put("updatedUser", updatedUser);
		}

		if (isFlagOnRequest(FLAG_UPDATED_USER_PW)) {
			map.put("updatedUserPw", true);
		}

		if (isFlagOnRequest(FLAG_USERS_DELETED)) {
			map.put("usersDeleted", true);
		}

		return map;
	}

	private UserAccount getUserFromUriParameter(String key) {
		String uri = vreq.getParameter(key);
		if ((uri == null) || uri.isEmpty()) {
			return null;
		}

		return userAccountsDao.getUserAccountByUri(uri);
	}

	/**
	 * The UserAccount has a list of PermissionSetUris, but the Freemarker
	 * template needs a list of PermissionSet labels instead.
	 */
	private List<UserAccountWrapper> wrapUserAccounts(
			UserAccountsSelection selection) {
		List<UserAccountWrapper> list = new ArrayList<UserAccountWrapper>();
		for (UserAccount account : selection.getUserAccounts()) {
			list.add(new UserAccountWrapper(account,
					findPermissionSetLabels(account)));
		}
		return list;
	}

	private List<String> findPermissionSetLabels(UserAccount account) {
		List<String> labels = new ArrayList<String>();
		for (String uri : account.getPermissionSetUris()) {
			PermissionSet pSet = userAccountsDao.getPermissionSetByUri(uri);
			if (pSet != null) {
				labels.add(pSet.getLabel());
			}
		}
		return labels;
	}

	/**
	 * Shows PermissionSet labels instead of PermissionSet URIs.
	 */
	public static class UserAccountWrapper {
		private final UserAccount account;
		private final List<String> permissionSets;
		private final String editUrl;

		public UserAccountWrapper(UserAccount account,
				List<String> permissionSets) {
			this.account = account;
			this.permissionSets = permissionSets;
			this.editUrl = UserAccountsPage.editAccountUrl(account.getUri());
		}

		public String getUri() {
			return account.getUri();
		}

		public String getEmailAddress() {
			return account.getEmailAddress();
		}

		public String getFirstName() {
			return account.getFirstName();
		}

		public String getLastName() {
			return account.getLastName();
		}

		public int getLoginCount() {
			return account.getLoginCount();
		}

		public String getStatus() {
			Status status = account.getStatus();
			if (status == null) {
				return "";
			} else {
				return status.toString();
			}
		}

		public List<String> getPermissionSets() {
			return permissionSets;
		}

		public String getEditUrl() {
			return editUrl;
		}

	}

}
