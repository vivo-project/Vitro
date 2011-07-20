/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageRootAccount;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.user.UserAccountsUserController;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Process a request to delete User Accounts.
 */
public class UserAccountsDeleter extends UserAccountsPage {
	private static final Log log = LogFactory.getLog(UserAccountsDeleter.class);

	private static final String PARAMETER_DELETE_ACCOUNT = "deleteAccount";

	/** Might be empty, but never null. */
	private final String[] uris;

	/** The result of checking whether this request is even appropriate. */
	private String bogusMessage = "";

	public UserAccountsDeleter(VitroRequest vreq) {
		super(vreq);

		String[] values = vreq.getParameterValues(PARAMETER_DELETE_ACCOUNT);
		if (values == null) {
			this.uris = new String[0];
		} else {
			this.uris = values;
		}

		WebappDaoFactory wadf = vreq.getWebappDaoFactory();

		validateInputUris();
	}

	private void validateInputUris() {
		UserAccount loggedInAccount = LoginStatusBean.getCurrentUser(vreq);
		if (loggedInAccount == null) {
			log.warn("Trying to delete accounts while not logged in!");
			bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
			return;
		}

		for (String uri : this.uris) {
			UserAccount u = userAccountsDao.getUserAccountByUri(uri);

			if (u == null) {
				log.warn("Delete account for '" + uri
						+ "' is bogus: no such user");
				bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
				return;
			}

			if (u.getUri().equals(loggedInAccount.getUri())) {
				log.warn("'" + u.getUri()
						+ "' is trying to delete his own account.");
				bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
				return;
			}

			if (u.isRootUser()
					&& (!PolicyHelper.isAuthorizedForActions(vreq,
							new ManageRootAccount()))) {
				log.warn("Attempting to delete the root account, "
						+ "but not authorized. Logged in as "
						+ LoginStatusBean.getCurrentUser(vreq));
				bogusMessage = UserAccountsUserController.BOGUS_STANDARD_MESSAGE;
				return;
			}
		}
	}

	public Collection<String> delete() {
		List<String> deletedUris = new ArrayList<String>();

		for (String uri : uris) {
			UserAccount u = userAccountsDao.getUserAccountByUri(uri);
			if (u != null) {
				userAccountsDao.deleteUserAccount(uri);
				deletedUris.add(uri);
			}
		}

		return deletedUris;
	}

	public boolean isBogus() {
		return !bogusMessage.isEmpty();
	}

	public String getBogusMessage() {
		return bogusMessage;
	}

}
