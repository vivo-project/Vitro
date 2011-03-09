/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;

/**
 * Allow us to store policies in a Request, in addition to those in the
 * ServletContext
 */
public class RequestPolicyList extends PolicyList {
	private static final String ATTRIBUTE_POLICY_ADDITIONS = RequestPolicyList.class
			.getName();
	private static final Log log = LogFactory.getLog(RequestPolicyList.class);

	/**
	 * Get a copy of the current list of policies. This includes the policies in
	 * the ServletContext, followed by any stored in the request. This method may
	 * return an empty list, but it never returns null.
	 */
	public static PolicyList getPolicies(HttpServletRequest request) {
		ServletContext ctx = request.getSession().getServletContext();

		PolicyList list = ServletPolicyList.getPolicies(ctx);
		list.addAll(getPoliciesFromRequest(request));
		return list;
	}

	public static void addPolicy(ServletRequest request, PolicyIface policy) {
		PolicyList policies = getPoliciesFromRequest(request);
		if (!policies.contains(policy)) {
			policies.add(policy);
			log.debug("Added policy: " + policy.toString());
		} else {
			log.warn("Ignored attempt to add redundent policy.");
		}
	}

	/**
	 * Get the current list of policy additions from the request, or create one
	 * if there is none. This method may return an empty list, but it never
	 * returns null.
	 */
	private static PolicyList getPoliciesFromRequest(ServletRequest request) {
		if (request == null) {
			throw new NullPointerException("request may not be null.");
		}
		
		Object obj = request.getAttribute(ATTRIBUTE_POLICY_ADDITIONS);
		if (obj == null) {
			obj = new PolicyList();
			request.setAttribute(ATTRIBUTE_POLICY_ADDITIONS, obj);
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
