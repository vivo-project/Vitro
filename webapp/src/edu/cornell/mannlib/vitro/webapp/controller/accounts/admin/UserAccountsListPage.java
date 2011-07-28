/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import static edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelectionCriteria.DEFAULT_ACCOUNTS_PER_PAGE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageRootAccount;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Direction;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Field;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelection;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelectionCriteria;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelector;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * Handle the List page.
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
		Message.applyToBodyMap(vreq, body);
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
			UserAccountWrapper wrapper = new UserAccountWrapper(account,
					findPermissionSetLabels(account), permittedToEdit(account),
					permittedToDelete(account));
			list.add(wrapper);
		}
		return list;
	}

	private boolean permittedToEdit(UserAccount account) {
		if (!account.isRootUser()) {
			return true;
		}
		if (PolicyHelper.isAuthorizedForActions(vreq, new ManageRootAccount())) {
			return true;
		}
		return false;
	}

	private boolean permittedToDelete(UserAccount account) {
		if (!permittedToEdit(account)) {
			return false;
		}
		UserAccount loggedInUser = LoginStatusBean.getCurrentUser(vreq);
		if (loggedInUser == null) {
			return false;
		}
		if (account.getUri().equals(loggedInUser.getUri())) {
			return false;
		}
		return true;
	}

	private List<String> findPermissionSetLabels(UserAccount account) {
		List<String> labels = new ArrayList<String>();

		if (account.isRootUser()) {
			labels.add("ROOT");
		} else {
			for (String uri : account.getPermissionSetUris()) {
				PermissionSet pSet = userAccountsDao.getPermissionSetByUri(uri);
				if (pSet != null) {
					labels.add(pSet.getLabel());
				}
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
		private final boolean deletable;

		public UserAccountWrapper(UserAccount account,
				List<String> permissionSets, boolean showEditUrl,
				boolean permitDelete) {
			this.account = account;
			this.permissionSets = permissionSets;
			this.deletable = permitDelete;

			if (showEditUrl) {
				this.editUrl = UserAccountsPage
						.editAccountUrl(account.getUri());
			} else {
				this.editUrl = "";
			}
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

		public Date getLastLoginTime() {
			long time = account.getLastLoginTime();
			if (time > 0L) {
				return new Date(time);
			} else {
				return null;
			}
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

		public boolean isDeletable() {
			return deletable;
		}

	}

	/**
	 * Message info that lives in the session. Another request can store this,
	 * and it will be displayed (once) by the list page.
	 */
	public static class Message {
		private static final String ATTRIBUTE = Message.class.getName();
		private static final Collection<String> EMPTY = Collections.emptySet();

		public static void showNewAccount(HttpServletRequest req,
				UserAccount userAccount, boolean emailWasSent) {
			Message message = new Message(Type.NEW_ACCOUNT, userAccount,
					emailWasSent, EMPTY);
			setMessage(req, message);
		}

		public static void showUpdatedAccount(HttpServletRequest req,
				UserAccount userAccount, boolean emailWasSent) {
			Message message = new Message(Type.UPDATED_ACCOUNT, userAccount,
					emailWasSent, EMPTY);
			setMessage(req, message);
		}

		public static void showDeletions(HttpServletRequest req,
				Collection<String> deletedUris) {
			Message message = new Message(Type.DELETIONS, null, false,
					deletedUris);
			setMessage(req, message);
		}

		private static void setMessage(HttpServletRequest req, Message message) {
			req.getSession().setAttribute(ATTRIBUTE, message);
		}

		public static void applyToBodyMap(HttpServletRequest req,
				Map<String, Object> body) {
			HttpSession session = req.getSession();
			Object o = session.getAttribute(ATTRIBUTE);
			session.removeAttribute(ATTRIBUTE);

			if (o instanceof Message) {
				((Message) o).applyToBodyMap(body);
			}
		}

		enum Type {
			NEW_ACCOUNT, UPDATED_ACCOUNT, DELETIONS
		}

		private final Type type;
		private final UserAccount userAccount;
		private final boolean emailWasSent;
		private final Collection<String> deletedUris;

		public Message(Type type, UserAccount userAccount,
				boolean emailWasSent, Collection<String> deletedUris) {
			this.type = type;
			this.userAccount = userAccount;
			this.emailWasSent = emailWasSent;
			this.deletedUris = deletedUris;
		}

		private void applyToBodyMap(Map<String, Object> body) {
			if (type == Type.NEW_ACCOUNT) {
				body.put("newUserAccount", new UserAccountWrapper(userAccount,
						Collections.<String> emptyList(), true, false));
				if (emailWasSent) {
					body.put("emailWasSent", Boolean.TRUE);
				}
			} else if (type == Type.UPDATED_ACCOUNT) {
				body.put("updatedUserAccount", new UserAccountWrapper(
						userAccount, Collections.<String> emptyList(), true,
						false));
				if (emailWasSent) {
					body.put("emailWasSent", Boolean.TRUE);
				}
			} else {
				body.put("deletedAccountCount", deletedUris.size());
			}
		}
	}

}
