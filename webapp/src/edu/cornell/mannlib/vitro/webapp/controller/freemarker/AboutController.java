/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AboutController extends FreeMarkerHttpServlet {
	
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(AboutController.class.getName());
    
    protected String getTitle() {
    	return "About " + appName;
    }
    
    protected String getBody() {
        
    	Map<String, Object> body = new HashMap<String, Object>();
    
        body.put("aboutText", portal.getAboutText());
        body.put("acknowledgeText", portal.getAcknowledgeText()); 
        
        // Test of #list directive in template on undefined, null, or empty values
        // Basic idea: empty list okay, null or undefined value not okay
        // List<String> apples = new ArrayList<String>();  // no error
        // List<String> apples = null; // error
        // body.put("apples", apples);
        // no apples in body: error
   
        String bodyTemplate = "about.ftl";             
        return mergeBodyToTemplate(bodyTemplate, body);

    }

}
