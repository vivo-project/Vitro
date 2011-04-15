/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;

/**
 * Confirm that the user is authorized to perform each of the RequestedActions.
 * 
 * The user specifies the actions as a comma delimited list of names (with
 * optional spaces). These names are matched against the map of recognized
 * names. If no match is found, an error is logged and the authorization fails.
 */
public class RequiresAuthorizationFor extends BodyTagSupport {
	private static final Log log = LogFactory
			.getLog(RequiresAuthorizationFor.class);

	/**
	 * These are the only action names that we recognize.
	 */
	private static final Map<String, RequestedAction> actionMap = new HashMap<String, RequestedAction>();
	static {
		actionMap.put("UseAdvancedDataToolsPages",
				new UseAdvancedDataToolsPages());
	}

	String actionNames = "";

	public void setActions(String actionNames) {
		this.actionNames = actionNames;
	}

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
	 * They are authorized if we recognize the actions they ask for, and they
	 * are authorized for those actions.
	 */
	private boolean isAuthorized() {
		Collection<RequestedAction> actions = parseActionNames();
		if (actions == null) {
			return false;
		}
		return PolicyHelper.areRequiredAuthorizationsSatisfied(getRequest(),
				actions);
	}

	/**
	 * Parse the string and pull the corresponding actions from the map. If we
	 * can't do that, complain and return null.
	 */
	private Collection<RequestedAction> parseActionNames() {
		Set<RequestedAction> actions = new HashSet<RequestedAction>();

		for (String part : actionNames.split("[\\s],[\\s]")) {
			String key = part.trim();
			if (key.isEmpty()) {
				continue;
			}

			if (actionMap.containsKey(key)) {
				log.debug("checking authorization for '" + key + "'");
				actions.add(actionMap.get(key));
			} else {
				log.error("JSP requested authorization for unknown action: '"
						+ key + "'");
				return null;
			}
		}
		return actions;
	}

	private boolean isLoggedIn() {
		return LoginStatusBean.getBean(getRequest()).isLoggedIn();
	}

	private int showInsufficientAuthorizationMessage() {
		VitroHttpServlet.redirectToInsufficientAuthorizationPage(getRequest(),
				getResponse());
		return SKIP_PAGE;
	}

	private int redirectToLoginPage() throws JspException {
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
