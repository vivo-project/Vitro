/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.jsptags;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;

/**
 * Confirm that the user is authorized to perform each of the RequestedActions.
 * 
 * The user specifies the actions as a comma delimited list of class names (with
 * optional spaces). The classes named must be extensions of RequestedAction
 * (usually implementations of UsePagesRequestedAction), and each class must
 * have a no-argument public constructor.
 */
public class RequiresAuthorizationFor extends BodyTagSupport {
	private static final Log log = LogFactory
			.getLog(RequiresAuthorizationFor.class);

	String classNamesString = "";

	public void setClassNames(String classNamesString) {
		this.classNamesString = classNamesString;
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
		Collection<RequestedAction> actions = instantiateActions();
		if (actions == null) {
			return false;
		}
		return PolicyHelper.areRequiredAuthorizationsSatisfied(getRequest(),
				actions);
	}

	/**
	 * Break the string into class names. Confirm that each class is
	 * RequestedAction or a subclass of it. Create an instance of each class.
	 * 
	 * If we can't do all of that, complain and return null.
	 */
	private Set<RequestedAction> instantiateActions() {
		Set<String> classNames = parseClassNames();
		if (classNames.isEmpty()) {
			return Collections.emptySet();
		}

		Set<Class<? extends RequestedAction>> actionClasses = loadClassesAndCheckTypes(classNames);
		if (actionClasses == null) {
			return null;
		}

		return getInstancesFromClasses(actionClasses);
	}

	private Set<String> parseClassNames() {
		Set<String> names = new HashSet<String>();
		for (String part : classNamesString.split("[\\s],[\\s]")) {
			String name = part.trim();
			if (!name.isEmpty()) {
				names.add(name);
			}
		}
		return names;
	}

	private Set<Class<? extends RequestedAction>> loadClassesAndCheckTypes(
			Set<String> classNames) {
		Set<Class<? extends RequestedAction>> classes = new HashSet<Class<? extends RequestedAction>>();
		for (String className : classNames) {
			try {
				Class<?> clazz = Class.forName(className);
				classes.add(clazz.asSubclass(RequestedAction.class));
			} catch (ClassNotFoundException e) {
				log.error("Can't load action class: '" + className + "'");
				return null;
			} catch (ClassCastException e) {
				log.error("Action class is not a subclass of RequestedAction: '"
						+ className + "'");
				return null;
			}
		}
		return classes;
	}

	private Set<RequestedAction> getInstancesFromClasses(
			Set<Class<? extends RequestedAction>> actionClasses) {
		Set<RequestedAction> actions = new HashSet<RequestedAction>();
		for (Class<? extends RequestedAction> actionClass : actionClasses) {
			try {
				RequestedAction action = actionClass.newInstance();
				actions.add(action);
			} catch (Exception e) {
				log.error("Failed to create an instance of '"
						+ actionClass.getName()
						+ "'. Does it have a public zero-argument constructor?");
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
