/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageUserAccounts;
import edu.cornell.mannlib.vitro.webapp.beans.DisplayMessage;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

/**
 * Parcel out the different actions required of the Administrators portion of
 * the UserAccounts GUI.
 */
public class UserAccountsAdminController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(UserAccountsAdminController.class);

	private static final String ACTION_ADD = "/add";
	private static final String ACTION_DELETE = "/delete";
	private static final String ACTION_EDIT = "/edit";

	@Override
	protected Actions requiredActions(VitroRequest vreq) {
		return new Actions(new ManageUserAccounts());
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		if (log.isDebugEnabled()) {
			dumpRequestParameters(vreq);
		}

		String action = vreq.getPathInfo();
		log.debug("action = '" + action + "'");

		if (ACTION_ADD.equals(action)) {
			return handleAddRequest(vreq);
		} else if (ACTION_EDIT.equals(action)) {
			return handleEditRequest(vreq);
		} else if (ACTION_DELETE.equals(action)) {
			return handleDeleteRequest(vreq);
		} else {
			return handleListRequest(vreq);
		}
	}

	private ResponseValues handleAddRequest(VitroRequest vreq) {
		UserAccountsAddPage page = new UserAccountsAddPage(vreq);
		if (page.isSubmit() && page.isValid()) {
			page.createNewAccount();

			UserAccountsListPage.Message.showNewAccount(vreq,
					page.getAddedAccount(), page.wasPasswordEmailSent());
			return redirectToList();
		} else {
			return page.showPage();
		}
	}

	private ResponseValues handleEditRequest(VitroRequest vreq) {
		UserAccountsEditPage page = new UserAccountsEditPage(vreq);
		if (page.isBogus()) {
			return showHomePage(vreq, page.getBogusMessage());
		} else if (page.isSubmit() && page.isValid()) {
			page.updateAccount();

			UserAccountsListPage.Message.showUpdatedAccount(vreq,
					page.getUpdatedAccount(), page.wasPasswordEmailSent());
			return redirectToList();
		} else {
			return page.showPage();
		}
	}

	private ResponseValues handleDeleteRequest(VitroRequest vreq) {
		UserAccountsDeleter deleter = new UserAccountsDeleter(vreq);
		if (deleter.isBogus()) {
			return showHomePage(vreq, deleter.getBogusMessage());
		} else {
			Collection<String> deletedUris = deleter.delete();

			UserAccountsListPage.Message.showDeletions(vreq, deletedUris);
			return redirectToList();
		}
	}

	private ResponseValues handleListRequest(VitroRequest vreq) {
		UserAccountsListPage page = new UserAccountsListPage(vreq);
		return page.showPage();
	}

	/**
	 * After an successful change, redirect to the list instead of forwarding.
	 * That way, a browser "refresh" won't try to repeat the operation.
	 */
	private ResponseValues redirectToList() {
		return new RedirectResponseValues("/accountsAdmin/list");
	}

	private ResponseValues showHomePage(VitroRequest vreq, String message) {
		DisplayMessage.setMessage(vreq, message);
		return new RedirectResponseValues("/");
	}

}
