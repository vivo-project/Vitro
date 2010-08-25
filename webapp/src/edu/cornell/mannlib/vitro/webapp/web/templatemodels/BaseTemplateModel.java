/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Params;

public abstract class BaseTemplateModel {

    private static final Log log = LogFactory.getLog(BaseTemplateModel.class.getName());
    
    protected static ServletContext servletContext = null;
    protected static VitroRequest vreq = null;

    // Wrap UrlBuilder method so templates can call ${item.url}
    public String getUrl(String path) {
        return UrlBuilder.getUrl(path);
    }

    // Wrap UrlBuilder method so templates can call ${item.url}
    public String getUrl(String path, Params params) {
        return UrlBuilder.getUrl(path, params);
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static void setServletContext(ServletContext context) {
        servletContext = context;
    }
    
    public static VitroRequest getVitroRequest() {
        return vreq;
    }

    public static void setVitroRequest(VitroRequest vrequest) {
        vreq = vrequest;
    }
    
    public String dump() {
        return "I'm a BaseTemplateModel object";
    }

}
