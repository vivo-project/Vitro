/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
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
		Set<RequestedAction> actionSet = getActionsFromRequestAttribute();
		return PolicyHelper.isAuthorizedForActions(getRequest(), new Actions(
				actionSet));
	}

	/**
	 * The attribute may be either a single RequestedAction or an array of
	 * RequestedActions. It may also be empty, but in that case why call this
	 * tag?
	 * 
	 * When we are done, clear the attribute, so any included or forwarded page
	 * will not see it.
	 */
	private Set<RequestedAction> getActionsFromRequestAttribute() {
		Set<RequestedAction> actionSet = new HashSet<RequestedAction>();
		Object attribute = getRequest().getAttribute("requestedActions");
		getRequest().removeAttribute("requestedActions");

		if (attribute == null) {
			log.warn("<vitro:confirmAuthorization /> was called, but nothing "
					+ "was found at request.getAttribute(\"requestedActions\")");
		} else if (attribute instanceof RequestedAction) {
			RequestedAction ra = (RequestedAction) attribute;
			log.debug("requested action was " + ra.getClass().getSimpleName());
			actionSet.add(ra);
		} else if (attribute instanceof RequestedAction[]) {
			RequestedAction[] array = (RequestedAction[]) attribute;
			List<RequestedAction> raList = Arrays.asList(array);
			if (log.isDebugEnabled()) {
				log.debug("requested actions were "
						+ formatRequestedActions(raList));
			}
			actionSet.addAll(raList);
		} else {
			throw new IllegalStateException(
					"Expected request.getAttribute(\"requestedActions\") "
							+ "to be either a RequestedAction or a "
							+ "RequestedAction[], but found "
							+ attribute.getClass().getCanonicalName());
		}

		return actionSet;
	}

	private String formatRequestedActions(List<RequestedAction> raList) {
		StringBuffer buff = new StringBuffer();
		for (Iterator<RequestedAction> it = raList.iterator(); it.hasNext();) {
			buff.append("'").append(it.next().getClass().getSimpleName())
					.append("'");
			if (it.hasNext()) {
				buff.append(", ");
			}
		}
		return buff.toString();
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
