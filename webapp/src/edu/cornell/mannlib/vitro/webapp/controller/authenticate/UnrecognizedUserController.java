/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * A user logged in using external authentication, but we don't have an account
 * for him, or an associated individual.
 */
public class UnrecognizedUserController extends FreemarkerHttpServlet {
	private static final String TEMPLATE_DEFAULT = "unrecognizedUser.ftl";
	private static final String PARAMETER_USERNAME = "username";

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		Map<String, Object> body = new HashMap<String, Object>();

		String username = vreq.getParameter(PARAMETER_USERNAME);
		if (username != null) {
			body.put("username", username);
		}

		return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
	}

	@Override
	protected String getTitle(String siteName) {
		return "Unrecognized user " + siteName;
	}

}
