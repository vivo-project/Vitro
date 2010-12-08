/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

/*
 * Servlet that only specifies a template, without putting any data
 * into the template model.
 */
public class EmptyController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(EmptyController.class);
    
    private static final Map<String, String> urlsToTemplates = new HashMap<String, String>(){
        {
            put("/login", "login.ftl");
        }
    };

    protected ResponseValues processRequest(VitroRequest vreq) {
        String requestedUrl = vreq.getServletPath();
        String templateName = urlsToTemplates.get(requestedUrl);
        
		log.debug("requestedUrl='" + requestedUrl + "', templateName='"
				+ templateName + "'");
		
        return new TemplateResponseValues(templateName);
    }    
}
