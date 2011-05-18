/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * TODO
 */
public class UserAccountsEditPage extends UserAccountsPage {
	private static final String TEMPLATE_NAME = "userAccounts-edit.ftl";

	public UserAccountsEditPage(VitroRequest vreq) {
		super(vreq);
	}

	public ResponseValues showPage() {
		return new TemplateResponseValues(TEMPLATE_NAME);
	}


}
