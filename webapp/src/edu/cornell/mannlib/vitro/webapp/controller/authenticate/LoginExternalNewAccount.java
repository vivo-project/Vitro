/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;

/**
 * Handle the first login from an external authentication.
 */
public class LoginExternalNewAccount extends FreemarkerHttpServlet {

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		String externalAuthId = ExternalAuthHelper.getHelper(vreq)
				.getExternalAuthId(vreq);

		// TODO Auto-generated method stub
		throw new RuntimeException(
				"LoginExternalNewAccount.processRequest() not implemented.");
	}

}
