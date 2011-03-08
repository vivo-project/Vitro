/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.ActiveIdentifierBundleFactories;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditing2RoleIdentifierFactory;

/**
 * Add the SelfEditing2RoleIdentifier factory to the IdentifierFactory list
 * in the servlet context.
 * 
 * This should be added to the IdentifierFactory list after the
 * SelfEditingIdentiferFactory.
 * 
 * This only sets up a IdentifierFactoy that maps SelfEditing identifiers to
 * roles associated with the Individual that represents the self editor.  This
 * does not set up a policy or the SelfEditingIdentifierFactory.
 * 
 * @author bdc34
 *
 */
public class SelfEditing2RoleIdentifierSetup implements ServletContextListener{

    private static final Log log = LogFactory.getLog(SelfEditing2RoleIdentifierSetup.class.getName());
    
    @Override
	public void contextDestroyed(ServletContextEvent sce) {
        //do nothing            
    }

    @Override
	public void contextInitialized(ServletContextEvent sce) {
        try{
            log.debug("Setting up SelfEditing2RoleIdentifier");                                
            ActiveIdentifierBundleFactories.addFactory(sce, new SelfEditing2RoleIdentifierFactory());
            log.debug( "SelfEditing2RoleIdentifier has been setup. " );            
        }catch(Exception e){
            log.error("could not run SelfEditing2RoleIdentifier: " + e);
            e.printStackTrace();
        }
    }

}
