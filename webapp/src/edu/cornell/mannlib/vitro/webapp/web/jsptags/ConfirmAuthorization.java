/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import static edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest.AUTHORIZED;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;

/**
 * Confirm that the user is authorized to perform each of the RequestedActions.
 * 
 * The user specifies the actions as the "requestedActions" attribute of the
 * HTTP request. The attribute must contain either a RequestedAction or an array
 * of RequestedActions.
 */
public class ConfirmAuthorization extends BodyTagSupport {
	private static final Log log = LogFactory
			.getLog(ConfirmAuthorization.class);

	/**
	 * This is all of it. If they are authorized, continue. Otherwise, redirect.
	 */
	@Override
	public int doEndTag() throws JspException {
		if (isAuthorized()) {
			return EVAL_PAGE;
		} else if (isLoggedIn()) {
			return showInsufficientAuthorizationMessage();
		} else {
			return redirectToLoginPage();
		}
	}

	/**
	 * They are authorized if the request contains no actions, or if they are
	 * authorized for the actions it contains.
	 */
	private boolean isAuthorized() {
		return PolicyHelper.isAuthorizedForActions(getRequest(),
				getActionsFromRequestAttribute());
	}

	/**
	 * The attribute may be either a single RequestedAction or an array of
	 * RequestedActions. 
	 *
	 * When we are done, clear the attribute, so any included or forwarded page
	 * will not see it.
	 */
	private AuthorizationRequest getActionsFromRequestAttribute() {
		Object attribute = getRequest().getAttribute("requestedActions");
		getRequest().removeAttribute("requestedActions");

		if (attribute == null) {
			return AUTHORIZED;
		} else if (attribute instanceof RequestedAction) {
			RequestedAction ra = (RequestedAction) attribute;
			log.debug("requested action was " + ra.getClass().getSimpleName());
			return ra;
		} else if (attribute instanceof RequestedAction[]) {
			AuthorizationRequest auth = AUTHORIZED;
			for (RequestedAction ra : (RequestedAction[]) attribute) {
				auth = auth.and(ra);
			}
			log.debug("requested actions were " + auth);
			return auth;
		} else {
			throw new IllegalStateException(
					"Expected request.getAttribute(\"requestedActions\") "
							+ "to be either a RequestedAction or a "
							+ "RequestedAction[], but found "
							+ attribute.getClass().getCanonicalName());
		}
	}

	private boolean isLoggedIn() {
		return LoginStatusBean.getBean(getRequest()).isLoggedIn();
	}

	private int showInsufficientAuthorizationMessage() {
		VitroHttpServlet.redirectToInsufficientAuthorizationPage(getRequest(),
				getResponse());
		return SKIP_PAGE;
	}

	private int redirectToLoginPage() {
		VitroHttpServlet.redirectToLoginPage(getRequest(), getResponse());
		return SKIP_PAGE;
	}

	private HttpServletRequest getRequest() {
		return ((HttpServletRequest) pageContext.getRequest());
	}

	private HttpServletResponse getResponse() {
		return (HttpServletResponse) pageContext.getResponse();
	}
}
