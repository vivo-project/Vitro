/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.velocity.VelocityContext;

import edu.cornell.mannlib.vitro.webapp.template.velocity.VelocityHttpServlet;

public class AboutControllerVelocity extends VelocityHttpServlet {
	
	private static final Log log = LogFactory.getLog(AboutControllerVelocity.class.getName());
    
    protected String getTitle() {
    	return "About " + portal.getAppName();
    }
    
    protected String getBody() {
    	
        String templateName = "about.vm";
        
        // Use chained contexts so values like title are available to both contexts.
        // For example, title is used in the <head> element and also as a page
        // title, so it's needed in both contexts.
        VelocityContext vc = new VelocityContext(context);
        
        // RY Figure out whether we need to check for nulls here, as in StringTemplate
        // We don't want the template to generate the tag with an empty value, we want
        // no tag. Implement a method as in StringTemplateUtil in VelocityUtil.
        vc.put("aboutText", portal.getAboutText());
        vc.put("acknowledgeText", portal.getAcknowledgeText()); 
        
        StringWriter sw = mergeTemplateToContext(templateName, vc);       
        return sw.toString();
    }


}
