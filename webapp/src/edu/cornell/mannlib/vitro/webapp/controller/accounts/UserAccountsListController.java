/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import static edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsSelectionCriteria.DEFAULT_ACCOUNTS_PER_PAGE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageUserAccounts;
import edu.cornell.mannlib.vitro.webapp.beans.PermissionSet;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount.Status;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Direction;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsOrdering.Field;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;

/**
 * Display the paginated list of User Accounts.
 */
public class UserAccountsListController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(UserAccountsListController.class);

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

	private OntModel userAccountsModel;
	private UserAccountsDao userAccountsDao;

	@Override
	public void init() throws ServletException {
		super.init();

		OntModelSelector oms = (OntModelSelector) getServletContext()
				.getAttribute("baseOntModelSelector");
		userAccountsModel = oms.getUserAccountsModel();

		WebappDaoFactory wdf = (WebappDaoFactory) getServletContext()
				.getAttribute("webappDaoFactory");
		userAccountsDao = wdf.getUserAccountsDao();
	}

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return new Actions(new ManageUserAccounts());
	}

	/**
	 * Assume the default criteria for display. Modify the criteria based on
	 * parameters in the request. Get the selected accounts and display them.
	 */
	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		if (log.isDebugEnabled()) {
			dumpRequestParameters(vreq);
		}

		Map<String, Object> body = new HashMap<String, Object>();

		UserAccountsSelectionCriteria criteria = buildCriteria(vreq);

		body.put("accountsPerPage", criteria.getAccountsPerPage());
		body.put("pageIndex", criteria.getPageIndex());
		body.put("orderDirection", criteria.getOrderBy().getDirection().keyword);
		body.put("orderField", criteria.getOrderBy().getField().name);
		body.put("roleFilterUri", criteria.getRoleFilterUri());
		body.put("searchTerm", criteria.getSearchTerm());

		UserAccountsSelection selection = UserAccountsSelector.select(
				userAccountsModel, criteria);

		body.put("accounts", wrapUserAccounts(selection));
		body.put("total", selection.getResultCount());
		body.put("page", buildPageMap(selection));

		body.put("formUrl", buildFormUrl(vreq));
		body.put("roles", buildRolesList());

		body.put("messages", buildMessagesMap(vreq));

		return new TemplateResponseValues(TEMPLATE_NAME, body);
	}

	private UserAccountsSelectionCriteria buildCriteria(VitroRequest vreq) {
		int accountsPerPage = getIntegerParameter(vreq,
				PARAMETER_ACCOUNTS_PER_PAGE, DEFAULT_ACCOUNTS_PER_PAGE);
		int pageIndex = getIntegerParameter(vreq, PARAMETER_PAGE_INDEX, 1);

		Direction orderingDirection = Direction.fromKeyword(vreq
				.getParameter(PARAMETER_ORDERING_DIRECTION));
		Field orderingField = Field.fromName(vreq
				.getParameter(PARAMETER_ORDERING_FIELD));
		UserAccountsOrdering ordering = new UserAccountsOrdering(orderingField,
				orderingDirection);

		String roleFilterUri = getStringParameter(vreq,
				PARAMETER_ROLE_FILTER_URI, "");
		String searchTerm = getStringParameter(vreq, PARAMETER_SEARCH_TERM, "");

		return new UserAccountsSelectionCriteria(accountsPerPage, pageIndex,
				ordering, roleFilterUri, searchTerm);
	}

	private String getStringParameter(VitroRequest vreq, String key,
			String defaultValue) {
		String value = vreq.getParameter(key);
		return (value == null) ? defaultValue : value;
	}

	private int getIntegerParameter(VitroRequest vreq, String key,
			int defaultValue) {
		String value = vreq.getParameter(key);
		if (value == null) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			log.warn("Invalid integer for parameter '" + key + "': " + value);
			return defaultValue;
		}
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

	private String buildFormUrl(VitroRequest vreq) {
		UrlBuilder urlBuilder = new UrlBuilder(vreq.getAppBean());
		return urlBuilder.getPortalUrl("/listUserAccounts");
	}

	private List<PermissionSet> buildRolesList() {
		List<PermissionSet> list = new ArrayList<PermissionSet>();
		list.addAll(userAccountsDao.getAllPermissionSets());
		Collections.sort(list, new Comparator<PermissionSet>() {
			@Override
			public int compare(PermissionSet ps1, PermissionSet ps2) {
				return ps1.getUri().compareTo(ps2.getUri());
			}
		});
		return list;
	}

	private Map<String, Object> buildMessagesMap(VitroRequest vreq) {
		Map<String, Object> map = new HashMap<String, Object>();

		UserAccount newUser = getUserFromUriParameter(vreq,
				PARAMETER_NEW_USER_URI);
		if (newUser != null) {
			map.put("newUser", newUser);
		}

		UserAccount updatedUser = getUserFromUriParameter(vreq,
				PARAMETER_UPDATED_USER_URI);
		if (updatedUser != null) {
			map.put("updatedUser", updatedUser);
		}

		if (isFlagOnRequest(vreq, FLAG_UPDATED_USER_PW)) {
			map.put("updatedUserPw", true);
		}

		if (isFlagOnRequest(vreq, FLAG_USERS_DELETED)) {
			map.put("usersDeleted", true);
		}

		return map;
	}

	private UserAccount getUserFromUriParameter(VitroRequest vreq, String key) {
		String uri = vreq.getParameter(key);
		if ((uri == null) || uri.isEmpty()) {
			return null;
		}

		return userAccountsDao.getUserAccountByUri(uri);
	}

	private boolean isFlagOnRequest(VitroRequest vreq, String key) {
		String value = vreq.getParameter(key);
		return (value != null);
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

		public UserAccountWrapper(UserAccount account,
				List<String> permissionSets) {
			this.account = account;
			this.permissionSets = permissionSets;
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

	}

}
