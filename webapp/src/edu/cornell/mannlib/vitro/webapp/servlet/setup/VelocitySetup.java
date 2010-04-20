/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.velocity.app.Velocity;

public class VelocitySetup implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		ServletContext sc = event.getServletContext();	
		String templatePath = sc.getRealPath("/templates/velocity");
		
        Properties p = new Properties();
        // RY May need to change this to webapp.resource.loader.path - see http://velocity.apache.org/engine/devel/webapps.html
        p.setProperty("file.resource.loader.path", templatePath);
        p.setProperty("runtime.references.strict", "true"); // for debugging; turn off in production
        //p.setProperty("eventhandler.invalidreference.class", "org.apache.velocity.app.event.implement.ReportInvalidReferences");
        //p.setProperty("eventhandler.nullset.class", "edu.cornell.mannlib.vitro.webapp.template.velocity.VitroNullSetEventHandler");
        //p.setProperty("eventhandler.include.class", "edu.cornell.mannlib.vitro.webapp.template.velocity.VitroIncludeEventHandler");
        
        // RY Switch to separate instance pattern insteadl of singleton.
        // See http://velocity.apache.org/engine/devel/developer-guide.html#separate
        try {
        	Velocity.init(p);
        } catch (Exception e){
        	// RY change to a logging statement
        	System.out.println("Error initializing Velocity");
        }
	}

	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}
}
