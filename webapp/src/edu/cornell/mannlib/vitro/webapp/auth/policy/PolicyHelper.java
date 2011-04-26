/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RequestIdentifiers;
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
	 * No need to instantiate this helper class - all methods are static.
	 */
	private PolicyHelper() {
		// nothing to do.
	}

}
