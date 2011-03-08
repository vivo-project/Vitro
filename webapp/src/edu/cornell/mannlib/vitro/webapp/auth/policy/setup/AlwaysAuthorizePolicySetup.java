/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.DefaultAuthorizedPolicy;

public class AlwaysAuthorizePolicySetup implements ServletContextListener {
	
	private static final Log log = LogFactory.getLog(AlwaysAuthorizePolicySetup.class.getName());
	
    public void contextInitialized(ServletContextEvent sce) {
        try{
            log.trace("WARNING: Setting up AlwaysAuthorizePolicySetup.");

            
            ServletPolicyList.addPolicy(sce.getServletContext(), new DefaultAuthorizedPolicy() );
            
            
        }catch(Exception e){
            log.error("could not create AuthorizationFactory: " + e);
            e.printStackTrace();
        }
    }
    
    
    public void contextDestroyed(ServletContextEvent sce) { /*nothing*/  }    
}
