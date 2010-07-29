/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker; 

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import freemarker.template.Configuration;

public class AboutController extends FreeMarkerHttpServlet {
	
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AboutController.class.getName());
    
    protected String getTitle(String siteName) {
    	return "About " + siteName;
    }
    
    protected String getBody(VitroRequest vreq, Map<String, Object> body, Configuration config) {

        Portal portal = vreq.getPortal();
    
        body.put("aboutText", portal.getAboutText());
        body.put("acknowledgeText", portal.getAcknowledgeText()); 
   
        String bodyTemplate = "about.ftl";             
        return mergeBodyToTemplate(bodyTemplate, body, config);

    }

}
