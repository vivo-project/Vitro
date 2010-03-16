package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Setups context so that Identifiers for Individuals associated with Users are
 * added to requests.
 * 
 * This only adds identifiers.  A self-editing policy is also needed.
 * 
 * @author bdc34
 *
 */
public class UserToIndIdentifierFactorySetup implements ServletContextListener{
	private static final Log log = LogFactory.getLog(UserToIndIdentifierFactorySetup.class.getName());
	
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext sc = sce.getServletContext();
		 ServletIdentifierBundleFactory
	     	.addIdentifierBundleFactory(sc, new UserToIndIdentifierFactory());
		 log.info("Set up Identifier Factory UserToIndIdentifierFactory.");		
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
	}	
}
