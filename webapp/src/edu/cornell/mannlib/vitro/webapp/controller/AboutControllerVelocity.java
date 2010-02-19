/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.MethodInvocationException;

import edu.cornell.mannlib.vitro.webapp.template.velocity.PageController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;

public class AboutControllerVelocity extends PageController {
	
	private static final Log log = LogFactory.getLog(AboutControllerVelocity.class.getName());
	
    public void doGet( HttpServletRequest request, HttpServletResponse response )
    	throws IOException, ServletException {
        try {
            super.doGet(request,response);

            String templateName = "about.vm";

            
            VelocityContext context = new VelocityContext();           
            context.put("title", "About " + portal.getAppName());
            context.put("aboutText", portal.getAboutText());
            context.put("acknowledgeText", portal.getAcknowledgeText());

            Template template = null;
            try {
            	template = Velocity.getTemplate(templateName);

            }
            catch (ResourceNotFoundException e) {
            	System.out.println("Can't find template " + templateName);
            }
            catch (ParseErrorException e) {
            	System.out.println("Problem parsing template " + templateName);
            }           	
            catch (MethodInvocationException e) {
            	System.out.println("Method invocation exception in template " + templateName);
            }

            StringWriter sw = new StringWriter();
            if (template != null) {
            	template.merge(context, sw);
            	out.print(sw);
            }
            
        } catch (Throwable e) {
            log.error("AboutControllerVelocity could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    	doGet(request, response);
    }
    
    protected String getTitle() {
    	return "About " + portal.getAppName();
    }


}
