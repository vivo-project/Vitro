/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.identifier;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class SetupFakeSelfEditingIdentifierFactory implements ServletContextListener{
	
	private static final Log log = LogFactory.getLog(SetupFakeSelfEditingIdentifierFactory.class.getName());

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        
        WebappDaoFactory wdf = (WebappDaoFactory)sce.getServletContext().getAttribute("webappDaoFactory");
        if( wdf == null ){
            log.debug("SetupFakeSelfEditingIdentifierFactory: need a " +
            		"WebappDaoFactory in ServletContext, none found, factory will " +
            		"not be created");
            return;
        }
        
        IdentifierBundleFactory ibfToAdd = new FakeSelfEditingIdentifierFactory();
        ServletIdentifierBundleFactory.addIdentifierBundleFactory(sc, ibfToAdd);        
    }

    public void contextDestroyed(ServletContextEvent sce) {     
    }
}
