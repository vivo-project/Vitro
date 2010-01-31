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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.RequestPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;

/**
 * Setup an IdentifierBundle and PolicyList for the request and put it in the request scope.
 *
 * It expects to get the IdentifierBundleFactory from ServletIdentifierBundleFactory and
 * PolicyList from ServletPolicyList;
 *
 * @author bdc34
 *
 */
public class AuthSetupForRequest implements Filter {
    ServletContext context;

    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //get a factory that will convert Requests into Identifiers
        IdentifierBundleFactory idbf = ServletIdentifierBundleFactory.getIdentifierBundleFactory(context);

        //try to get the session
        HttpSession session = null;
        if( servletRequest instanceof HttpServletRequest)
            session = ((HttpServletRequest)servletRequest).getSession(false);

        //get Identifiers and stick in Request scope
        try{
            if( idbf != null ){
                IdentifierBundle ib = idbf.getIdentifierBundle(servletRequest, session, context);
                servletRequest.setAttribute(IDENTIFIER_BUNDLE,  ib);
            }
        }catch(RuntimeException rx){
            log.warn("could not get Identifier Bundle",rx);
        }

        //get the policies that are in effect for the context and add to Request Scope
        PolicyList plist = ServletPolicyList.getPolicies(context);        
        servletRequest.setAttribute(RequestPolicyList.POLICY_LIST , plist);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() { }
    
    private static final Log log = LogFactory.getLog(AuthSetupForRequest.class);
    private static final String IDENTIFIER_BUNDLE = "IdentifierBundle";
    
    /* ************ static utility methods ********************* */
    public static IdentifierBundle getIdentifierBundle(HttpServletRequest req){
        if( req != null )                
            return (IdentifierBundle)req.getAttribute(IDENTIFIER_BUNDLE);            
       else
            return null;                                      
    }
    
    public static PolicyList getPolicyList( HttpServletRequest req){
        if( req != null ){
            HttpSession sess = req.getSession(false);
            if( sess != null ){     
                return (PolicyList)sess.getAttribute(RequestPolicyList.POLICY_LIST);
            }else{
                return null;            
            }
        }else{
            return null;            
        }
    }    
}