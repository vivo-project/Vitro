/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreeMarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.view.ViewObject;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class FreeMarkerSetup implements ServletContextListener {
    
    private static final Log log = LogFactory.getLog(FreeMarkerSetup.class);

	public void contextInitialized(ServletContextEvent event) {	

		ServletContext sc = event.getServletContext();	
		
		Configuration cfg = new Configuration();
		
		// Specify the data source where the template files come from.
//		try {
//			cfg.setDirectoryForTemplateLoading(new File(templatePath));
//		} catch (IOException e) {
//			log.error("Error specifying template directory.");
//		}
		
		// RY This setting won't take effect until we use Configuration.getTemplate() to
		// create templates.
		String buildEnv = ConfigurationProperties.getProperty("Environment.build");
		if (buildEnv != null && buildEnv.equals("development")) {
		    cfg.setTemplateUpdateDelay(0); // no template caching in development 
		}
		
		// Specify how templates will see the data-model. This is an advanced topic...
		// but just use this:
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		
		try {
            cfg.setSetting("url_escaping_charset", "ISO-8859-1");
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            log.error("Error setting value for url_escaping_charset.");
        }

		FreeMarkerHttpServlet.config = cfg;  
		
		String contextPath = sc.getContextPath();
		FreeMarkerHttpServlet.contextPath = contextPath;
		FreeMarkerHttpServlet.context = sc;
		// ViewObject.contextPath = contextPath; 
		
	}

	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}

}
