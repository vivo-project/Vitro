/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class FreeMarkerComponentGenerator extends FreeMarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(FreeMarkerHttpServlet.class.getName());
    
    FreeMarkerComponentGenerator(HttpServletRequest request, HttpServletResponse response) {
        VitroRequest vreq = new VitroRequest(request);
        Configuration config = getConfig(vreq);

        // root is the map used to create the page shell - header, footer, menus, etc.
        Map<String, Object> root = getSharedVariables(vreq); 
        setUpRoot(vreq, root);  
        
        request.setAttribute("ftl_identity", get("identity", root, config));
        request.setAttribute("ftl_menu", get("menu", root, config));
        request.setAttribute("ftl_search", get("search", root, config));
        request.setAttribute("ftl_footer", get("footer", root, config));
    }
    
//    public String getIdentity() {
//        return get("identity");
//    }
//
//    public String getMenu() {
//        return get("menu");
//    }
//    
//    public String getSearch() {
//        return get("search");
//    }
//
//    public String getFooter() {
//        return get("footer"); 
//    }
//    
    private String get(String templateName, Map<String, Object> root, Configuration config) {
        String template = "page/partials/" + templateName + ".ftl";
        return mergeToTemplate(template, root, config).toString();
    }

}
