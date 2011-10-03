/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class FreemarkerSetup implements ServletContextListener {
    
    private static final Log log = LogFactory.getLog(FreemarkerSetup.class);

	@Override
	public void contextInitialized(ServletContextEvent event) {	
		ServletContext sc = event.getServletContext();	
        BaseTemplateModel.setServletContext(sc);
        FreemarkerComponentGenerator.setServletContext(sc);
		UrlBuilder.contextPath = sc.getContextPath();
		
		log.info("Freemarker templating system initialized.");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}

}
