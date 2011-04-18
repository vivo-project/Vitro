/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor.NoAction;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;

/**
 * A collection of static methods to help determine whether requested actions
 * are authorized by current policy.
 */
public class PolicyHelper {
	private static final Log log = LogFactory.getLog(PolicyHelper.class);

	/**
	 * A subclass of VitroHttpServlet may be annotated to say what actions
	 * should be checked for authorization before permitting the user to view
	 * the page that the servlet would create.
	 * 
	 * Any RequestedAction can be specified, but the most common use will be to
	 * specify implementations of UsePagesRequestedAction.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface RequiresAuthorizationFor {
		static class NoAction extends RequestedAction {
			/* no fields */
		}

		Class<? extends RequestedAction>[] value() default NoAction.class;
	}

	/**
	 * Does this servlet require authorization?
	 */
	public static boolean isRestrictedPage(VitroHttpServlet servlet) {
		Class<? extends VitroHttpServlet> servletClass = servlet.getClass();
		return !getRequiredAuthorizationsForServlet(servletClass).isEmpty();
	}

	/**
	 * Are the actions that this servlet requires authorized for the current
	 * user by the current policies?
	 */
	public static boolean areRequiredAuthorizationsSatisfied(
			HttpServletRequest req, VitroHttpServlet servlet) {
		Class<? extends VitroHttpServlet> servletClass = servlet.getClass();
		return areRequiredAuthorizationsSatisfied(req, servletClass);
	}

	/**
	 * Are the actions that this servlet class requires authorized for the
	 * current user by the current policies?
	 */
	public static boolean areRequiredAuthorizationsSatisfied(
			HttpServletRequest req,
			Class<? extends VitroHttpServlet> servletClass) {
		return areRequiredAuthorizationsSatisfied(req,
				getRequiredAuthorizationsForServlet(servletClass));
	}

	/**
	 * Are these action classes authorized for the current user by the current
	 * policies?
	 */
	public static boolean areRequiredAuthorizationsSatisfied(
			HttpServletRequest req,
			Class<? extends RequestedAction>... actionClasses) {
		List<Class<? extends RequestedAction>> classList = Arrays
				.asList(actionClasses);

		Set<RequestedAction> actions = instantiateActions(classList);
		if (actions == null) {
			log.debug("not authorized: failed to instantiate actions");
			return false;
		}

		return areRequiredAuthorizationsSatisfied(req, actions);
	}

	/**
	 * Are these actions authorized for the current user by the current
	 * policies?
	 */
	public static boolean areRequiredAuthorizationsSatisfied(
			HttpServletRequest req,
			Collection<? extends RequestedAction> actions) {
		PolicyIface policy = ServletPolicyList.getPolicies(req);
		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);

		for (RequestedAction action : actions) {
			if (isAuthorized(policy, ids, action)) {
				log.debug("not authorized");
				return false;
			}
		}

		log.debug("authorized");
		return true;
	}

	/**
	 * Is this action class authorized for the current user by the current
	 * policies?
	 */
	@SuppressWarnings("unchecked")
	public static boolean isAuthorized(HttpServletRequest req,
			Class<? extends RequestedAction> actionClass) {
		return areRequiredAuthorizationsSatisfied(req, actionClass);
	}

	/**
	 * Is this action authorized for these IDs by this policy?
	 */
	private static boolean isAuthorized(PolicyIface policy,
			IdentifierBundle ids, RequestedAction action) {
		PolicyDecision decision = policy.isAuthorized(ids, action);
		log.debug("decision for '" + action.getClass().getName() + "' was: "
				+ decision);
		return (decision == null)
				|| (decision.getAuthorized() != Authorization.AUTHORIZED);
	}

	/**
	 * What RequestedActions does this servlet require authorization for?
	 * 
	 * Keep this private, since it reveals how the Annotation is implemented. If
	 * we change the Annotation to include "or" and "and", then this method
	 * becomes meaningless with its current return type.
	 */
	private static Set<RequestedAction> getRequiredAuthorizationsForServlet(
			Class<? extends VitroHttpServlet> clazz) {
		Set<RequestedAction> result = new HashSet<RequestedAction>();

		RequiresAuthorizationFor annotation = clazz
				.getAnnotation(RequiresAuthorizationFor.class);

		if (annotation != null) {
			for (Class<? extends RequestedAction> actionClass : annotation
					.value()) {
				if (NoAction.class != actionClass) {
					RequestedAction action = instantiateAction(actionClass);
					if (action != null) {
						result.add(action);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Instantiate actions from their classes. If any one of the classes cannot
	 * be instantiated, return null.
	 */
	private static Set<RequestedAction> instantiateActions(
			Collection<Class<? extends RequestedAction>> actionClasses) {
		Set<RequestedAction> actions = new HashSet<RequestedAction>();
		for (Class<? extends RequestedAction> actionClass : actionClasses) {
			RequestedAction action = instantiateAction(actionClass);
			if (action == null) {
				return null;
			} else {
				actions.add(action);
			}
		}
		return actions;
	}

	/**
	 * Get an instance of the RequestedAction, from its class. If the class
	 * cannot be instantiated, return null.
	 */
	private static RequestedAction instantiateAction(
			Class<? extends RequestedAction> actionClass) {
		try {
			Constructor<? extends RequestedAction> constructor = actionClass
					.getConstructor();
			RequestedAction instance = constructor.newInstance();
			return instance;
		} catch (NoSuchMethodException e) {
			log.error("'" + actionClass.getName()
					+ "' does not have a no-argument constructor.");
			return null;
		} catch (IllegalAccessException e) {
			log.error("The no-argument constructor for '"
					+ actionClass.getName() + "' is not public.");
			return null;
		} catch (Exception e) {
			log.error("Failed to instantiate '" + actionClass.getName() + "'",
					e);
			return null;
		}
	}

	/**
	 * No need to instantiate this helper class - all methods are static.
	 */
	private PolicyHelper() {
		// nothing to do.
	}

}
