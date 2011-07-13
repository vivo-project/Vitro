/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.web.AntiScript;

public abstract class BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseTemplateModel.class);
    
    protected static ServletContext servletContext;
    
    // Convenience method so subclasses can call getUrl(path)
    protected String getUrl(String path) {
        return UrlBuilder.getUrl(path);
    }

    // Convenience method so subclasses can call getUrl(path, params)
    protected String getUrl(String path, ParamMap params) {
        return UrlBuilder.getUrl(path, params);
    }
    
    // Convenience method so subclasses can call getUrl(path, params)
    protected String getUrl(String path, String... params) {
        return UrlBuilder.getUrl(path, params);
    }

    /**
     * Used to do any processing for display of URIs or URLs.  
     * Currently this only checks for XSS exploits.
     */
    protected String cleanURIForDisplay( String dirty ){
        return AntiScript.cleanURI(dirty, getServletContext());
    }
    
    /**
     * Used to do any processing for display of general text.  
     * Currently this only checks for XSS exploits.
     */
    protected String cleanTextForDisplay( String dirty){
        return AntiScript.cleanText(dirty, getServletContext());
    }
    
    /**
     * Used to do any processing for display of values in
     * a map.  Map may be modified. 
     */
    protected <T> void cleanMapValuesForDisplay( Map<T,String> map){
        AntiScript.cleanMapValues(map, getServletContext());
    }
    
    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static void setServletContext(ServletContext context) {
        servletContext = context;
    }
 
    
}
