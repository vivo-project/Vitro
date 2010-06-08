/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.view.ViewObject;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class FreeMarkerSetup implements ServletContextListener {
    
    private static final Log log = LogFactory.getLog(FreeMarkerSetup.class);

	public void contextInitialized(ServletContextEvent event) {	

		ServletContext sc = event.getServletContext();	
		
		Configuration cfg = new Configuration();
		
		// Specify the data source where the template files come from.
		// RY Now being done for each request, in order to support multi-portal apps
		// and dynamic theme-loading.
		// try {
		// 	cfg.setDirectoryForTemplateLoading(new File(templatePath));
		// } catch (IOException e) {
		// 	log.error("Error specifying template directory.");
		// }
		
		String buildEnv = ConfigurationProperties.getProperty("Environment.build");
		if (buildEnv != null && buildEnv.equals("development")) {
		    cfg.setTemplateUpdateDelay(0); // no template caching in development 
		}
		
	    // Specify how templates will see the data-model. 
		// The default wrapper exposes set methods unless exposure level is set.
		// By default we want to block exposure of set methods. 
		// cfg.setObjectWrapper(new DefaultObjectWrapper());
		BeansWrapper wrapper = new DefaultObjectWrapper();
		wrapper.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        cfg.setObjectWrapper(wrapper);
		
		// Set some formatting defaults. These can be overridden at the template
		// or environment (template-processing) level, or for an individual
        // instance by using built-ins.
		cfg.setLocale(java.util.Locale.US);
		
		String dateFormat = "M/d/yyyy";
		cfg.setDateFormat(dateFormat);
		String timeFormat = "hh:mm a";
	    cfg.setTimeFormat(timeFormat);
		cfg.setDateTimeFormat(dateFormat + " " + timeFormat);
		
		//cfg.setNumberFormat("#,##0.##");
		
		try {
            cfg.setSetting("url_escaping_charset", "ISO-8859-1");
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            log.error("Error setting value for url_escaping_charset.");
        }

		FreeMarkerHttpServlet.config = cfg;  
        FreeMarkerHttpServlet.context = sc;
        ViewObject.context = sc;
		UrlBuilder.contextPath = sc.getContextPath();

	}

	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do here
	}

}
