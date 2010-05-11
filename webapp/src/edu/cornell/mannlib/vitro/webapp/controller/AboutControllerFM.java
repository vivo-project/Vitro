/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller; 

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.FreeMarkerHttpServlet;

import freemarker.template.SimpleDate;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateModelException;

public class AboutControllerFM extends FreeMarkerHttpServlet {
	
	private static final Log log = LogFactory.getLog(AboutControllerFM.class.getName());
    
    protected String getTitle() {
    	return "About " + portal.getAppName();
    }
    
    protected String getBody() {
        
    	Map<String, Object> body = new HashMap<String, Object>();
    
        body.put("aboutText", portal.getAboutText());
        body.put("acknowledgeText", portal.getAcknowledgeText()); 
        
        String templateName = "body/about.ftl";             
        return mergeBodyToTemplate(templateName, body);

    }

}
