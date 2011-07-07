/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;

public abstract class BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseTemplateModel.class);
    
    protected static ServletContext servletContext;
    protected final VitroRequest vreq;
    
    protected BaseTemplateModel(VitroRequest vreq) {
        this.vreq = vreq;
    }
    
    // Some BaseTemplateModel classes don't need vreq, so provide an
    // argumentless constructor.
    protected BaseTemplateModel() { 
        this.vreq = null;
    };
    
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

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static void setServletContext(ServletContext context) {
        servletContext = context;
    }
    
}
