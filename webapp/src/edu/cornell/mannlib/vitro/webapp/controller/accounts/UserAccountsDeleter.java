/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import java.util.Collection;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * TODO delete and kick to Accounts list with message, telling how many were
 * deleted. If there was a problem, the user will need to infer it from the
 * count??
 */
public class UserAccountsDeleter extends UserAccountsPage {

	protected UserAccountsDeleter(VitroRequest vreq) {
		super(vreq);
	}

	/**
	 * @return
	 * 
	 */
	public Collection<String> delete() {
		// TODO Auto-generated method stub
		throw new RuntimeException(
				"UserAccountsDeleter.delete() not implemented.");
	}

}
