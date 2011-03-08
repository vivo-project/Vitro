/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RequestPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;

/**
 * Setup a PolicyList for the request and put it in the request scope.
 *
 * It expects to get the PolicyList from ServletPolicyList;
 *
 * @author bdc34
 *
 */
public class AuthSetupForRequest implements Filter {
    ServletContext context;

    @Override
	public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    @Override
	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
        //get the policies that are in effect for the context and add to Request Scope
        PolicyList plist = ServletPolicyList.getPolicies(context);        
        servletRequest.setAttribute(RequestPolicyList.POLICY_LIST , plist);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
	public void destroy() { /* Nothing to destroy */ }
}