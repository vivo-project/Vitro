/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.ListIterator;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyIface;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.VisitingPolicyIface;

/**
 * This is a PolicyList that can do isAuthorized and stashes a singleton
 * in the ServletContext.
 * 
 * The intent of this class is to allow a single point for policies
 * in a ServletContext.  example:
 * <code>
 * Authorization canIDoIt = ServletPolicyList.getPolicies( getServletContext() ).isAuthorized( IdBundle, action );
 * </code>
 * 
 * @author bdc34
 *
 */
public class ServletPolicyList extends PolicyList {
    protected static String POLICY_LIST = "policy_list";
    private static final Log log = LogFactory.getLog(ServletPolicyList.class.getName());

    /**
     * This is for general public use to get a list of policies for the ServletContext.
     * @param sc
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ServletPolicyList getPolicies(ServletContext sc){
        ServletPolicyList list  = null;
        try{
            list = (ServletPolicyList)sc.getAttribute(POLICY_LIST);
        }catch(ClassCastException cce){
            log.error(POLICY_LIST +" server context attribute was not of type List<PolicyIface>");
        }
        if( list == null ){
            list = new ServletPolicyList();
            sc.setAttribute(POLICY_LIST, list);
        }
        return list;
    }

    public static void addPolicy(ServletContext sc, PolicyIface policy){
        ServletPolicyList policies = getPolicies(sc);
        if( !policies.contains(policy) ){
            policies.add(policy);
            log.info("Added policy: " + policy.toString());
        }else{
            log.info("Ignored attempt to add redundent policy.");
        }
    }
    
    /** 
     * This adds the policy to the front of the list but it may be moved further down
     * the list by other policies that are later added using this method.
     */
    public static void addPolicyAtFront(ServletContext sc, PolicyIface policy){
        ServletPolicyList policies = getPolicies(sc);
        if( !policies.contains(policy) ){
            policies.add(0,policy);
            log.info("Added policy at front of ServletPolicyList: " + policy.toString());
        }else{
            log.info("Ignored attempt to add redundent policy.");
        }
    }
    
    /** 
     * Replace first instance of policy found in policy list.  If no instance
     * is found in list add at end of the list.
     * 
     * @param sc
     * @param policy
     */
    public static void replacePolicy(ServletContext sc, PolicyIface policy){
        if( sc == null ) 
            throw new IllegalArgumentException( "replacePolicy() needs a non-null ServletContext");
        if( policy == null )
            return;
        Class clzz = policy.getClass();
        
        ServletPolicyList spl = ServletPolicyList.getPolicies(sc);
        ListIterator<PolicyIface> it = spl.listIterator();
        boolean replaced = false;
        while(it.hasNext()){
            VisitingPolicyIface p = (VisitingPolicyIface)it.next();            
            if( clzz.isAssignableFrom(p.getClass()) ){
                it.set( policy );
                replaced = true;
            }
        }    
        if( ! replaced ){
            ServletPolicyList.addPolicy(sc, policy);
        }    
    }
}
