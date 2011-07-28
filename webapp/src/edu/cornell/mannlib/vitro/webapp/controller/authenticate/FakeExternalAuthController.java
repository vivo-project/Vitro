/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.authenticate;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/**
 * This allows J. Random User to pretend that he's passed external authentication.
 * 
 * This should not be included in web.xml in a production deployment!!!
 */
public class FakeExternalAuthController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(FakeExternalAuthController.class);

	private static final String URL_FAKE_CONTROLLER = "/admin/fakeExternalAuth";
	private static final String URL_EXTERNAL_AUTH_RETURN = "/loginExternalAuthReturn";

	private static final String TEMPLATE_DEFAULT = "fakeExternalAuth.ftl";

	private static final String PARAMETER_USERNAME = "username";
	private static final String PARAMETER_CANCEL = "cancel";

	@Override
	public void init() throws ServletException {
		log.debug("storing the bean.");
		ExternalAuthHelper.setBean(getServletContext(),
				new FakeExternalAuthHelper(null));
	}
	
	@Override
	protected String getTitle(String siteName, VitroRequest vreq) {
		return "Fake external login " + siteName;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		if (isCancelRequested(vreq)) {
			log.debug("cancelling.");
			setFakeExternalAuthHelper(vreq, null);
			return makeRedirectResponse();
		} else if (isUsernameSupplied(vreq)) {
			log.debug("faking as '" + getUsername(vreq) + "'");
			setFakeExternalAuthHelper(vreq, getUsername(vreq));
			return makeRedirectResponse();
		} else {
			log.debug("show the form.");
			return makeShowFormResponse();
		}
	}

	private boolean isCancelRequested(VitroRequest vreq) {
		String cancelString = vreq.getParameter(PARAMETER_CANCEL);
		return (cancelString != null) && (!cancelString.isEmpty());
	}

	private void setFakeExternalAuthHelper(HttpServletRequest req,
			String username) {
		ExternalAuthHelper.setBean(req.getSession().getServletContext(),
				new FakeExternalAuthHelper(username));
	}

	private boolean isUsernameSupplied(VitroRequest vreq) {
		String username = getUsername(vreq);
		return ((username != null) && (!username.isEmpty()));
	}

	private String getUsername(VitroRequest vreq) {
		return vreq.getParameter(PARAMETER_USERNAME);
	}

	private TemplateResponseValues makeShowFormResponse() {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("controllerUrl", UrlBuilder.getUrl(URL_FAKE_CONTROLLER));
		return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
	}

	private RedirectResponseValues makeRedirectResponse() {
		return new RedirectResponseValues(URL_EXTERNAL_AUTH_RETURN);
	}

	/**
	 * This implementation of ExternalAuthHelper ignores any configuration
	 * properties. This controller is used as the exernal authorization server,
	 * and the username that is set by this controller is used as the external
	 * username.
	 */
	public static class FakeExternalAuthHelper extends ExternalAuthHelper {
		private final String username;

		private FakeExternalAuthHelper(String username) {
			super(null, null);
			this.username = username;
		}

		@Override
		public String buildExternalAuthRedirectUrl(String returnUrl) {
			int lastSlash = returnUrl.lastIndexOf("/");
			String homeUrl = returnUrl.substring(0, lastSlash);
			String url = homeUrl + URL_FAKE_CONTROLLER;
			log.debug("externalAuth URL is '" + url + "'");
			return url;
		}

		@Override
		public String getExternalAuthId(HttpServletRequest request) {
			log.debug("external username is '" + username + "'");
			return username;
		}

		@Override
		public String toString() {
			return "FakeExternalAuthHelper[username='" + username + "']";
		}

	}
}
