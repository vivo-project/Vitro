/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * TODO present the form. Get the submission. 
 * 
 * TODO If email is available, present the reset flag with message templage, and send email
 * 
 * TODO if email is not available, allow password change with checks for validity
 * 
 * TODO If successful, go to AccountsList with message and optional password message.
 * 
 * TODO if unsuccessful, go back to the page, with errors.
 * 
 * TODO How much of this can be shared with AddPage? Email templates?
 */
public class UserAccountsEditPage extends UserAccountsPage {
	private static final String TEMPLATE_NAME = "userAccounts-edit.ftl";

	public UserAccountsEditPage(VitroRequest vreq) {
		super(vreq);
	}

	public ResponseValues showPage() {
		return new TemplateResponseValues(TEMPLATE_NAME);
	}

	/**
	 * @return
	 */
	public UserAccount updateAccount() {
		// TODO Auto-generated method stub
		throw new RuntimeException("UserAccountsEditPage.updateAccount() not implemented.");
	}

	/**
	 * @return
	 */
	public boolean wasPasswordEmailSent() {
		// TODO Auto-generated method stub
		throw new RuntimeException("UserAccountsEditPage.wasPasswordEmailSent() not implemented.");
	}

	/**
	 * @return
	 */
	public UserAccount getUpdatedAccount() {
		// TODO Auto-generated method stub
		throw new RuntimeException("UserAccountsEditPage.getUpdatedAccount() not implemented.");
	}

	/**
	 * @return
	 */
	public boolean isValid() {
		// TODO Auto-generated method stub
		throw new RuntimeException("UserAccountsEditPage.isValid() not implemented.");
	}

	/**
	 * @return
	 */
	public boolean isSubmit() {
		// TODO Auto-generated method stub
		throw new RuntimeException("UserAccountsEditPage.isSubmit() not implemented.");
	}


}
