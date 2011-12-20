/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper.TemplateProcessingException;
import freemarker.template.Configuration;

/**
 * TEMPORARY for transition from JSP to FreeMarker. Once transition
 * is complete and no more pages are generated in JSP, this can be removed.
 * 
 * @author rjy7
 *
 */
public class FreemarkerComponentGenerator extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FreemarkerComponentGenerator.class);
    
    private static ServletContext context = null;
    
    FreemarkerComponentGenerator(HttpServletRequest request) {
   
        // Mimic what FreemarkerHttpServlet does for a new request
        VitroRequest vreq = new VitroRequest(request);       
        FreemarkerConfiguration config = getConfig(vreq);
        vreq.setAttribute("freemarkerConfig", config);
        config.resetRequestSpecificSharedVariables();        
        Map<String, Object> map = getPageTemplateValues(vreq);
        
        request.setAttribute("ftl_head", getHead("head", map, config, vreq));
        request.setAttribute("ftl_identity", get("identity", map, config, vreq));
        request.setAttribute("ftl_menu", get("menu", map, config, vreq));
        request.setAttribute("ftl_search", get("search", map, config, vreq));
        request.setAttribute("ftl_footer", get("footer", map, config, vreq));
        request.setAttribute("ftl_googleAnalytics", get("googleAnalytics", map, config, vreq));
    }

    private String get(String templateName, Map<String, Object> root, Configuration config, HttpServletRequest request) {
        templateName += ".ftl";
        try {
            return processTemplate(templateName, root, config, request).toString();
        } catch (TemplateProcessingException e) {
            log.error("Error processing template " + templateName + ": " + e.getMessage(), e);
            return null;
        }
    }
    
    private String getHead(String templateName, Map<String, Object> root, Configuration config, HttpServletRequest request) {
        // The Freemarker head template displays the page title in the <title> tag. Get the value out of the request.
        String title = (String) request.getAttribute("title");
        if (!StringUtils.isEmpty(title)) {
            root.put("title", title);
        }
        return get(templateName, root, config, request);        
    }
    
    // RY We need the servlet context in getConfig(). For some reason using the method inherited from
    // GenericServlet bombs.
    @Override
    public ServletContext getServletContext() {
        return context;
    }
    
    protected static void setServletContext(ServletContext sc) {
        context = sc;
    }

}
