/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    public FreeMarkerComponentGenerator(HttpServletRequest request, HttpServletResponse response) {
        doSetup(request, response);
    }
    
    public String getIdentity() {
        return get("identity");
    }

    public String getMenu() {
        return get("menu");
    }
    
    public String getSearch() {
        return get("search");
    }

    public String getFooter() {
        return get("footer"); 
    }
    
    private String get(String templateName) {
        String template = "components/" + templateName + ".ftl";
        return mergeToTemplate(template, root).toString();
    }
    
}
