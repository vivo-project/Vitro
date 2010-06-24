/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import freemarker.template.Configuration;

public class FreeMarkerSetup implements ServletContextListener {
    
    private static final Log log = LogFactory.getLog(FreeMarkerSetup.class);

	public void contextInitialized(ServletContextEvent event) {	

		ServletContext sc = event.getServletContext();	
		sc.setAttribute("themeToConfigMap", new HashMap<String, Configuration>());
        BaseTemplateModel.context = sc;
        FreeMarkerComponentGenerator.setServletContext(sc);
		UrlBuilder.contextPath = sc.getContextPath();
	}

	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}

}
