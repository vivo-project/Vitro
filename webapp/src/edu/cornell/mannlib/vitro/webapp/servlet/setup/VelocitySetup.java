package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.velocity.app.Velocity;

public class VelocitySetup implements ServletContextListener {

	// Set default theme based on themes present on the file system
	public void contextInitialized(ServletContextEvent event) {
		ServletContext sc = event.getServletContext();	
		String templatePath = sc.getRealPath("/templates/velocity");
		
        Properties p = new Properties();
        p.setProperty("file.resource.loader.path", templatePath);
        p.setProperty("runtime.references.strict", "true"); // for debugging; turn off in production
        
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
