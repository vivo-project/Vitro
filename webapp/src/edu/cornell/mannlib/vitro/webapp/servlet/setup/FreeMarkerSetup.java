/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

public class FreeMarkerSetup {
	
	// Set default theme based on themes present on the file system
	public void contextInitialized(ServletContextEvent event) {	

		Configuration cfg = new Configuration();
		// Specify the data source where the template files come from.
		try {
			cfg.setDirectoryForTemplateLoading(new File("/templates/freemarker"));
		} catch (IOException e) {
			// RY Change to logging statement
			System.out.println("Error specifying template directory ");
		}
		        
		// Specify how templates will see the data-model. This is an advanced topic...
		// but just use this:
		cfg.setObjectWrapper(new DefaultObjectWrapper());		
        
	}

	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}

}
