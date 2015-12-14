/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.developer;

import static edu.cornell.mannlib.vitro.webapp.utils.developer.Key.PERMIT_ANONYMOUS_CONTROL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.services.freemarker.FreemarkerProcessingService.TemplateProcessingException;
import edu.cornell.mannlib.vitro.webapp.services.freemarker.FreemarkerProcessingServiceSetup;

/**
 * Accept an AJAX request to update the developer settings. Return an HTML
 * representation of the developer panel from the settings and a Freemarker
 * template.
 * 
 * If developer mode is not enabled, the HTML response is empty.
 * 
 * You may only control the panel if you are logged in with sufficient
 * authorization, or if anonymous control is permitted by the settings.
 * 
 * If you are not allowed to control the panel, then the HTML response
 * is only a statement that developer mode is enabled. Otherwise, it
 * is a full panel (collapsed at first).
 */
public class DeveloperSettingsServlet extends VitroAjaxController {
	private static final Log log = LogFactory
			.getLog(DeveloperSettingsServlet.class);

	@Override
	protected void doRequest(VitroRequest vreq, HttpServletResponse resp)
			throws ServletException, IOException {
		DeveloperSettings settings = DeveloperSettings.getInstance();

		/*
		 * Are they allowed to control the panel?
		 */
		if (isAuthorized(vreq)) {
			// Update the settings.
			settings.updateFromRequest(vreq.getParameterMap());
		} else {
			log.debug("Not authorized to update settings.");
		}

		/*
		 * Build the response.
		 */
		try {
			Map<String, Object> bodyMap = buildBodyMap(isAuthorized(vreq),
					settings);
			String rendered = renderTemplate(vreq, bodyMap);
			resp.getWriter().write(rendered);
		} catch (Exception e) {
			doError(resp, e.toString(), 500);
		}
	}

	private Map<String, Object> buildBodyMap(boolean authorized,
			DeveloperSettings settings) {
		Map<String, Object> settingsMap = new HashMap<>();
		settingsMap.putAll(settings.getRawSettingsMap());
		settingsMap.put("mayControl", authorized);
		
		Map<String, Object> bodyMap = new HashMap<>();
		bodyMap.put("settings", settingsMap);
		return bodyMap;
	}

	private String renderTemplate(VitroRequest vreq, Map<String, Object> bodyMap)
			throws TemplateProcessingException {
		return FreemarkerProcessingServiceSetup.getService(getServletContext())
				.renderTemplate("developerPanel.ftl", bodyMap, vreq);
	}

	private boolean isAuthorized(VitroRequest vreq) {
		boolean authBySetting = DeveloperSettings.getInstance().getBoolean(
				PERMIT_ANONYMOUS_CONTROL);
		boolean authByPolicy = PolicyHelper.isAuthorizedForActions(vreq,
				SimplePermission.ENABLE_DEVELOPER_PANEL.ACTION);
		return authBySetting || authByPolicy;
	}
}
