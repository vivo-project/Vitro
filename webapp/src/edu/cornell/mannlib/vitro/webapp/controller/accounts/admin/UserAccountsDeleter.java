/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.accounts.UserAccountsPage;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * Process a request to delete User Accounts.
 */
public class UserAccountsDeleter extends UserAccountsPage {
	
	private static final String PARAMETER_DELETE_ACCOUNT = "deleteAccount";

	/** Might be empty, but never null. */
	private final String[] uris;

	public UserAccountsDeleter(VitroRequest vreq) {
		super(vreq);
		
		String[] values = vreq.getParameterValues(PARAMETER_DELETE_ACCOUNT);
		if (values == null) {
			this.uris = new String[0];
		} else {
			this.uris = values;
		}
	}

	public Collection<String> delete() {
		List<String> deletedUris = new ArrayList<String>(); 
		
		WebappDaoFactory wadf = vreq.getWebappDaoFactory();
		UserAccountsDao dao = wadf.getUserAccountsDao();
		
		for (String uri: uris) {
			UserAccount u = dao.getUserAccountByUri(uri);
			if (u != null) {
				dao.deleteUserAccount(uri);
				deletedUris.add(uri);
			}
		}
		
		return deletedUris;
	}

}
