/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import freemarker.template.Configuration;

public class FreemarkerConfigurationLoader {    
    private static final Log log = LogFactory.getLog(FreemarkerConfigurationLoader.class);

    private ServletContext context;
    
    public FreemarkerConfigurationLoader(ServletContext context){
        this.context = context;
        context.setAttribute("FreemarkerConfigurationLoader", this);
    }
    
    /**
     * @return Under serious error conditions this will return null.
     */
    public static FreemarkerConfigurationLoader getFreemarkerConfigurationLoader(ServletContext context){
        if( context!=null){
            FreemarkerConfigurationLoader fcl = (FreemarkerConfigurationLoader)
                context.getAttribute("FreemarkerConfigurationLoader");
            if( fcl == null ){
                log.error("Must be constructed before calling " +
                        "getFreemarkerConfigurationLoader(), usually this is done by FreemarkerSetup");
                return null;
            }
            return fcl;
        }else{
            log.error("getFreemarkerConfigurationLoader() must not be called with a null context");        
            return null; 
        }
    }
    
    public FreemarkerConfiguration getConfig(VitroRequest vreq) { 
        String themeDir = getThemeDir(vreq.getAppBean());
        return getConfigForTheme(themeDir, vreq);
    }

    protected String getThemeDir(ApplicationBean appBean) {
    	if (appBean == null) {
    		log.error("Cannot get themeDir from null application bean");
    		return null;
    	}
    	
    	String themeDir = appBean.getThemeDir();
	    if (themeDir == null) {
	        log.error("themeDir is null");
	        return null;
	    }

        return themeDir.replaceAll("/$", "");
    }
    
    protected FreemarkerConfiguration getConfigForTheme(String themeDir, VitroRequest vreq) {
        
        /* The Configuration is theme-specific because:
         * 1. The template loader is theme-specific, since it specifies a theme directory to load templates from.
         * 2. Shared variables like stylesheets are theme-specific.
         */ 
        @SuppressWarnings("unchecked")
        Map<String, FreemarkerConfiguration> themeToConfigMap = 
            (Map<String, FreemarkerConfiguration>) context.getAttribute("themeToConfigMap");
        
        if( themeToConfigMap == null ) {
            log.error("The templating system is not configured correctly. Make sure that you have the FreemarkerSetup context listener in your web.xml.");
            // We'll end up with a blank page as well as errors in the log, which is probably fine. 
            // Doesn't seem like we should throw a checked exception in this case.
            return null;    
        } else if (themeToConfigMap.containsKey(themeDir)) {
            return themeToConfigMap.get(themeDir);
        } else {
            FreemarkerConfiguration config = new FreemarkerConfiguration(themeDir, vreq, context); 
            themeToConfigMap.put(themeDir, config);
            return config;
        }
    }
    
}