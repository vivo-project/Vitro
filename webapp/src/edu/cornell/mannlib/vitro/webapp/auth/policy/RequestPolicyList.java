/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;

/**
 * This is store and get policies with a Request.
 */
public class RequestPolicyList extends PolicyList{       
    public final static String POLICY_LIST = "PolicyList";
    private static final Log log = LogFactory.getLog( RequestPolicyList.class );

    @SuppressWarnings("unchecked")
    public static PolicyList getPolicies(ServletRequest request){
        PolicyList list  = null;
        try{
            list = (PolicyList)request.getAttribute(POLICY_LIST);
        }catch(ClassCastException cce){
            log.error(POLICY_LIST +" server context attribute was not of type PolicyList");
        }
        if( list == null ){
            list = new RequestPolicyList();
            request.setAttribute(POLICY_LIST, list);
        }
        return list;
    }

    public static void addPolicy(ServletRequest request, PolicyIface policy){
        PolicyList policies = getPolicies(request);
        if( !policies.contains(policy) ){
            policies.add(policy);
            log.info("Added policy: " + policy.toString());
        }else{
            log.info("Ignored attempt to add redundent policy.");
        }
    }
    

}
