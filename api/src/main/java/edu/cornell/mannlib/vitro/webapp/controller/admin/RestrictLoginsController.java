/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import java.util.HashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.AbstractPageHandler;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.Authenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.BasicAuthenticator;
import edu.cornell.mannlib.vitro.webapp.controller.authenticate.RestrictedAuthenticator;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;

/**
 * Offer the user the ability to apply a RestrictedAuthenticator or revert to a
 * BasicAuthenticator.
 */
public class RestrictLoginsController extends FreemarkerHttpServlet {
	public static final String PARAMETER_RESTRICT = "restrict";
	public static final String PARAMETER_OPEN = "open";
	public static final String MESSAGE_NO_MESSAGE = "message";
	public static final String MESSAGE_RESTRICTING = "messageRestricting";
	public static final String MESSAGE_OPENING = "messageOpening";
	public static final String MESSAGE_ALREADY_RESTRICTED = "messageAlreadyRestricted";
	public static final String MESSAGE_ALREADY_OPEN = "messageAlreadyOpen";

	@Override
	protected AuthorizationRequest requiredActions(VitroRequest vreq) {
		return SimplePermission.LOGIN_DURING_MAINTENANCE.ACTION;
	}

	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {
		Core pageHandler = new Core(vreq);
		pageHandler.processInput();
		return pageHandler.prepareOutput();
	}

	private static class Core extends AbstractPageHandler {
		private enum State {
			OPEN, RESTRICTED
		}

		private String messageCode;

		Core(VitroRequest vreq) {
			super(vreq);
		}

		void processInput() {
			State desired = figureDesiredState();
			State current = figureCurrentlyState();

			if (desired == null) {
				messageCode = MESSAGE_NO_MESSAGE;
			} else if (desired == State.OPEN) {
				if (current == State.OPEN) {
					messageCode = MESSAGE_ALREADY_OPEN;
				} else {
					openLogins();
					messageCode = MESSAGE_OPENING;
				}
			} else if (desired == State.RESTRICTED) {
				if (current == State.RESTRICTED) {
					messageCode = MESSAGE_ALREADY_RESTRICTED;
				} else {
					restrictLogins();
					messageCode = MESSAGE_RESTRICTING;
				}
			}
		}

		ResponseValues prepareOutput() {
			boolean restricted = figureCurrentlyState() == State.RESTRICTED;

			Map<String, Object> body = new HashMap<String, Object>();
			body.put("title", I18n.text(vreq, "restrict_logins"));
			body.put("restricted", restricted);
			if (!MESSAGE_NO_MESSAGE.equals(messageCode)) {
				body.put(messageCode, Boolean.TRUE);
			}
			body.put("restrictUrl", UrlBuilder.getUrl("/admin/restrictLogins",
					PARAMETER_RESTRICT, "true"));
			body.put("openUrl", UrlBuilder.getUrl("/admin/restrictLogins",
					PARAMETER_OPEN, "true"));
			
			return new TemplateResponseValues("admin-restrictLogins.ftl", body);
		}

		private State figureDesiredState() {
			if (isFlagOnRequest(PARAMETER_RESTRICT)) {
				return State.RESTRICTED;
			} else if (isFlagOnRequest(PARAMETER_OPEN)) {
				return State.OPEN;
			} else {
				return null;
			}
		}

		private State figureCurrentlyState() {
			Authenticator auth = Authenticator.getInstance(vreq);
			if (auth instanceof RestrictedAuthenticator) {
				return State.RESTRICTED;
			} else {
				return State.OPEN;
			}
		}

		private void openLogins() {
			Authenticator.setAuthenticatorFactory(
					new BasicAuthenticator.Factory(), ctx);
		}

		private void restrictLogins() {
			Authenticator.setAuthenticatorFactory(
					new RestrictedAuthenticator.Factory(), ctx);
		}
	}
}
