/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.ListIterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;

/**
 * This maintains a PolicyList in the ServletContext. As a rule, however, this
 * is only used as the basis for the RequestPolicyList. Client code that wants
 * to access the current list of policies should look there.
 */
public class ServletPolicyList {
	private static final String ATTRIBUTE_POLICY_LIST = ServletPolicyList.class.getName();
	private static final Log log = LogFactory.getLog(ServletPolicyList.class);

	/**
	 * Get a copy of the current list of policies. This method may return an
	 * empty list, but it never returns null.
	 */
	public static PolicyIface getPolicies(HttpServletRequest hreq) {
		return getPolicies(hreq.getSession().getServletContext());
	}

	/**
	 * Get a copy of the current list of policies. This method may return an
	 * empty list, but it never returns null.
	 */
	public static PolicyList getPolicies(ServletContext sc) {
		return new PolicyList(getPolicyList(sc));
	}

	/**
	 * Add the policy to the end of the list.
	 */
	public static void addPolicy(ServletContext sc, PolicyIface policy) {
		if (policy == null) {
			return;
		}
		
		PolicyList policies = getPolicyList(sc);
		if (!policies.contains(policy)) {
			policies.add(policy);
			log.info("Added policy: " + policy.getClass().getSimpleName());
			log.debug("Added policy: " + policy.toString());
		} else {
			log.warn("Ignored attempt to add redundant policy.");
		}
	}

	/**
	 * Add the policy to the front of the list. It may be moved further down the
	 * list by other policies that are later added using this method.
	 */
	public static void addPolicyAtFront(ServletContext sc, PolicyIface policy) {
		if (policy == null) {
			return;
		}
		
		PolicyList policies = getPolicyList(sc);
		if (!policies.contains(policy)) {
			policies.add(0, policy);
			log.info("Added policy at front: " + policy.getClass().getSimpleName());
			log.debug("Added policy at front: " + policy.toString());
		} else {
			log.warn("Ignored attempt to add redundant policy.");
		}
	}

	/**
	 * Replace the first instance of this class of policy in the list. If no
	 * instance is found, add the policy to the end of the list.
	 */
	public static void replacePolicy(ServletContext sc, PolicyIface policy) {
		if (policy == null) {
			return;
		}

		Class<?> clzz = policy.getClass();
		PolicyList policies = getPolicyList(sc);
		ListIterator<PolicyIface> it = policies.listIterator();
		while (it.hasNext()) {
			if (clzz.isAssignableFrom(it.next().getClass())) {
				it.set(policy);
				return;
			}
		}

		addPolicy(sc, policy);
	}

	/**
	 * Get the current PolicyList from the context, or create one if there is
	 * none. This method may return an empty list, but it never returns null.
	 */
	private static PolicyList getPolicyList(ServletContext ctx) {
		if (ctx == null) {
			throw new NullPointerException("ctx may not be null.");
		}

		Object obj = ctx.getAttribute(ATTRIBUTE_POLICY_LIST);
		if (obj == null) {
			obj = new PolicyList();
			ctx.setAttribute(ATTRIBUTE_POLICY_LIST, obj);
		}

		if (!(obj instanceof PolicyList)) {
			throw new IllegalStateException("Expected to find an instance of "
					+ PolicyList.class.getName()
					+ " in the context, but found an instance of "
					+ obj.getClass().getName() + " instead.");
		}

		return (PolicyList) obj;
	}

}
