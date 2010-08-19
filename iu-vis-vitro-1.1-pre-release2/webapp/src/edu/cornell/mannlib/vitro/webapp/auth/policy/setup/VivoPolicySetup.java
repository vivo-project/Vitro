/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ServletPolicyList;
import edu.cornell.mannlib.vitro.webapp.auth.policy.VivoPolicy;

public class VivoPolicySetup implements ServletContextListener{
	private static final Log log = LogFactory.getLog(VivoPolicySetup.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {		
	   log.debug("Setting up VivoPolicy");
       
       //need to make a policy and add it to the ServeltContext                      
       ServletPolicyList.addPolicy(sce.getServletContext(), new VivoPolicy());
       
       //Note: The VivoPolicy doesn't use any identifier bundles so none are added here.		
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		//do nothing		
	}

}
