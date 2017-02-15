/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Display the startup status page.
 */
public class StartupStatusController extends FreemarkerHttpServlet {

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.SEE_STARTUP_STATUS.ACTION;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		Map<String, Object> body = new HashMap<String, Object>();

		body.put("title", I18n.text(vreq, "startup_status"));
		body.put("status", StartupStatus.getBean(getServletContext()));
		body.put("contextPath", getContextPath());
		body.put("applicationName", getApplicationName(vreq));

		return new TemplateResponseValues("startupStatus-display.ftl", body);
	}

	private String getContextPath() {
		String cp = getServletContext().getContextPath();
		if ((cp == null) || cp.isEmpty()) {
			return "The application";
		} else {
			return cp;
		}
	}

	private Object getApplicationName(VitroRequest vreq) {
		String name = "";
		try {
			name = vreq.getAppBean().getApplicationName();
		} catch (Exception e) {
			// deal with problems below
		}

		if ((name != null) && (!name.isEmpty())) {
			return name;
		} else {
			return getContextPath();
		}
	}


}
