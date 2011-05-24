/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * TODO If hash is not valid display bogus message.
 * 
 * TODO Set the password fields, reset the expire time, set account active, kick
 * to home page with message. Send confirmation email.
 * 
 * TODO How do we know "createPassword" from "setPassword"? a parameter? or just by account status?
 */
public class UserAccountsSetPasswordPage extends UserAccountsPage {

	protected UserAccountsSetPasswordPage(VitroRequest vreq) {
		super(vreq);
	}

}
