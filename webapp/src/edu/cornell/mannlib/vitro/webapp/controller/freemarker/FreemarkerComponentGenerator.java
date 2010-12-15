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
        VitroRequest vreq = new VitroRequest(request);
        Configuration config = getConfig(vreq);

        // root is the map used to create the page shell - header, footer, menus, etc.
        Map<String, Object> root = getSharedVariables(vreq, new HashMap<String, Object>()); 
        root.putAll(getPageTemplateValues(vreq));  
        
        request.setAttribute("ftl_head", getHead("head", root, config, vreq));
        request.setAttribute("ftl_identity", get("identity", root, config, vreq));
        request.setAttribute("ftl_menu", get("menu", root, config, vreq));
        request.setAttribute("ftl_search", get("search", root, config, vreq));
        request.setAttribute("ftl_footer", get("footer", root, config, vreq));
        request.setAttribute("ftl_googleAnalytics", get("googleAnalytics", root, config, vreq));
    }

    private String get(String templateName, Map<String, Object> root, Configuration config, HttpServletRequest request) {
        templateName += ".ftl";
        return processTemplate(templateName, root, config, request).toString();
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
