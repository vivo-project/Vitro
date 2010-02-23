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
        
        // Use chained contexts so values like title are available to both contexts.
        // For example, title is used in the <head> element and also as a page
        // title, so it's needed in both contexts.
        VelocityContext vc = new VelocityContext(context);
        
        // RY Velocity works like StringTemplate here: if the value is an empty string,
        // an if test on the variable will succeed, unlike the EL empty operator.
        // Since these methods return nulls rather than empty strings, this is ok here,
        // but in other cases, we might need a utility method that won't put the value
        // in the context if it's an empty string.
        vc.put("aboutText", portal.getAboutText());
        vc.put("acknowledgeText", portal.getAcknowledgeText()); 
          	
        return mergeBodyTemplateToContext("about.vm", vc);       	     
    }


}
