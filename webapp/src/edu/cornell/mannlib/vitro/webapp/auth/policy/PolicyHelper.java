/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor.NoAction;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor.Or;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;

/**
 * A collection of static methods to help determine whether requested actions
 * are authorized by current policy.
 */
public class PolicyHelper {
	private static final Log log = LogFactory.getLog(PolicyHelper.class);

	/**
	 * Are these actions authorized for the current user by the current
	 * policies?
	 */
	public static boolean isAuthorizedForActions(HttpServletRequest req,
			RequestedAction... actions) {
		return isAuthorizedForActions(req, new Actions(actions));
	}
	
	/**
	 * Are these actions authorized for the current user by the current
	 * policies?
	 */
	public static boolean isAuthorizedForActions(HttpServletRequest req,
			Actions actions) {
		PolicyIface policy = ServletPolicyList.getPolicies(req);
		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);
		return Actions.notNull(actions).isAuthorized(policy, ids);
	}

	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	// Obsolete ????????
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	// ----------------------------------------------------------------------
	
	/**
	 * A subclass of VitroHttpServlet may be annotated to say what actions
	 * should be checked for authorization before permitting the user to view
	 * the page that the servlet would create.
	 * 
	 * Any RequestedAction can be specified, but the most common use will be to
	 * specify implementations of UsePagesRequestedAction.
	 * 
	 * Note that a combination of AND and OR relationships can be created
	 * (at-signs converted to #-signs, so Javadoc won't try to actually apply
	 * the annotations):
	 * 
	 * <pre>
	 * #RequiresAuthorizationFor(This.class)
	 * #RequiresAuthorizationFor({This.class, That.class})
	 * #RequiresAuthorizationFor(value=This.class, or=#Or(That.class))
	 * #RequiresAuthorizationFor(or={#Or(One_A.class, One_B.class), #Or(Two.class)})
	 * </pre>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface RequiresAuthorizationFor {
		static class NoAction extends RequestedAction {
			/* no fields */
		}

		@Retention(RetentionPolicy.RUNTIME)
		public static @interface Or {
			Class<? extends RequestedAction>[] value() default NoAction.class;
		}

		Class<? extends RequestedAction>[] value() default NoAction.class;

		Or[] or() default @Or();
	}

	/**
	 * Are the actions that this servlet requires authorized for the current
	 * user by the current policies?
	 */
	public static boolean isAuthorizedForServlet(HttpServletRequest req,
			HttpServlet servlet) {
		return isAuthorizedForServlet(req, servlet.getClass());
	}

	/**
	 * Are the actions that this servlet class requires authorized for the
	 * current user by the current policies?
	 */
	public static boolean isAuthorizedForServlet(HttpServletRequest req,
			Class<? extends HttpServlet> servletClass) {
		try {
			return isAuthorizedForActionClauses(req,
					ActionClauses.forServletClass(servletClass));
		} catch (PolicyHelperException e) {
			return false;
		}
	}

	/**
	 * Is this action class authorized for the current user by the current
	 * policies?
	 */
	public static boolean isAuthorizedForAction(HttpServletRequest req,
			Class<? extends RequestedAction> actionClass) {
		try {
			return isAuthorizedForActionClauses(req, new ActionClauses(
					actionClass));
		} catch (PolicyHelperException e) {
			return false;
		}
	}

	/**
	 * Are these actions authorized for the current user by the current
	 * policies?
	 */
	public static boolean isAuthorizedForAction(HttpServletRequest req,
			RequestedAction... actions) {
		return isAuthorizedForActionClauses(req, new ActionClauses(actions));
	}

	/**
	 * Actions must be authorized for the current user by the current policies.
	 * If no actions, no problem.
	 */
	private static boolean isAuthorizedForActionClauses(HttpServletRequest req,
			ActionClauses actionClauses) {
		PolicyIface policy = ServletPolicyList.getPolicies(req);
		IdentifierBundle ids = RequestIdentifiers.getIdBundleForRequest(req);

		return actionClauses.isEmpty()
				|| isAuthorizedForActionClauses(policy, ids, actionClauses);
	}

	/** Any clause in an ActionClauses may be authorized. */
	private static boolean isAuthorizedForActionClauses(PolicyIface policy,
			IdentifierBundle ids, ActionClauses actionClauses) {
		for (Set<RequestedAction> clause : actionClauses.getClauseList()) {
			if (isAuthorizedForClause(policy, ids, clause)) {
				return true;
			}
		}
		return false;
	}

	/** All actions in a clause must be authorized. */
	private static boolean isAuthorizedForClause(PolicyIface policy,
			IdentifierBundle ids, Set<RequestedAction> clause) {
		for (RequestedAction action : clause) {
			if (!isAuthorizedForAction(policy, ids, action)) {
				log.debug("not authorized");
				return false;
			}
		}
		return true;
	}

	/**
	 * Is this action authorized for these IDs by this policy?
	 */
	private static boolean isAuthorizedForAction(PolicyIface policy,
			IdentifierBundle ids, RequestedAction action) {
		PolicyDecision decision = policy.isAuthorized(ids, action);
		log.debug("decision for '" + action.getClass().getName() + "' was: "
				+ decision);
		return (decision != null)
				&& (decision.getAuthorized() == Authorization.AUTHORIZED);
	}

	/**
	 * This helper class embodies the list of OR and AND relationships for the
	 * required authorizations. A group of AND relationships is a "clause", and
	 * the list of clauses are in an OR relationship.
	 * 
	 * Authorization is successful if ALL of the actions in ANY of the clauses
	 * are authorized, or if there are NO clauses.
	 * 
	 * If any action can't be instantiated, throw an exception so authorization
	 * will fail.
	 */
	private static class ActionClauses {
		static ActionClauses forServletClass(
				Class<? extends HttpServlet> servletClass)
				throws PolicyHelperException {
			return new ActionClauses(
					servletClass.getAnnotation(RequiresAuthorizationFor.class));
		}

		private final List<Set<RequestedAction>> clauseList;

		ActionClauses(RequiresAuthorizationFor annotation)
				throws PolicyHelperException {
			List<Set<RequestedAction>> list = new ArrayList<Set<RequestedAction>>();
			if (annotation != null) {
				addClause(list, annotation.value());
				for (Or orAnnotation : annotation.or()) {
					addClause(list, orAnnotation.value());
				}
			}
			this.clauseList = Collections.unmodifiableList(list);
		}

		ActionClauses(Collection<Class<? extends RequestedAction>> actionClasses)
				throws PolicyHelperException {
			this.clauseList = Collections
					.singletonList(buildClause(actionClasses));
		}

		ActionClauses(Class<? extends RequestedAction> actionClass)
				throws PolicyHelperException {
			this.clauseList = Collections.singletonList(Collections
					.singleton(instantiateAction(actionClass)));
		}

		ActionClauses(RequestedAction[] actions) {
			HashSet<RequestedAction> actionSet = new HashSet<RequestedAction>(
					Arrays.asList(actions));
			this.clauseList = Collections.singletonList(Collections
					.unmodifiableSet(actionSet));
		}

		private void addClause(List<Set<RequestedAction>> list,
				Class<? extends RequestedAction>[] actionClasses)
				throws PolicyHelperException {
			Set<RequestedAction> clause = buildClause(Arrays
					.asList(actionClasses));
			if (!clause.isEmpty()) {
				list.add(clause);
			}
		}

		private Set<RequestedAction> buildClause(
				Collection<Class<? extends RequestedAction>> actionClasses)
				throws PolicyHelperException {
			Set<RequestedAction> clause = new HashSet<RequestedAction>();
			for (Class<? extends RequestedAction> actionClass : actionClasses) {
				if (!NoAction.class.equals(actionClass)) {
					clause.add(instantiateAction(actionClass));
				}
			}
			return Collections.unmodifiableSet(clause);
		}

		/**
		 * Get an instance of the RequestedAction, from its class, or throw an
		 * exception.
		 */
		private RequestedAction instantiateAction(
				Class<? extends RequestedAction> actionClass)
				throws PolicyHelperException {
			try {
				Constructor<? extends RequestedAction> constructor = actionClass
						.getConstructor();
				RequestedAction instance = constructor.newInstance();
				return instance;
			} catch (NoSuchMethodException e) {
				log.error("'" + actionClass.getName()
						+ "' does not have a no-argument constructor.");
				throw new PolicyHelperException();
			} catch (IllegalAccessException e) {
				log.error("The no-argument constructor for '"
						+ actionClass.getName() + "' is not public.");
				throw new PolicyHelperException();
			} catch (Exception e) {
				log.error("Failed to instantiate '" + actionClass.getName()
						+ "'", e);
				throw new PolicyHelperException();
			}
		}

		boolean isEmpty() {
			return this.clauseList.isEmpty();
		}

		List<Set<RequestedAction>> getClauseList() {
			return this.clauseList;
		}
	}

	/** We failed to instantiate a RequestedAction */
	private static class PolicyHelperException extends Exception {
		// no members
	}

	/**
	 * No need to instantiate this helper class - all methods are static.
	 */
	private PolicyHelper() {
		// nothing to do.
	}

}
