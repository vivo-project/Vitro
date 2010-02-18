/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.StringWriter;

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


import edu.cornell.mannlib.vitro.webapp.beans.Portal;

public class AboutControllerFreeMarker extends VitroHttpServlet {
	
	private static final Log log = LogFactory.getLog(AboutControllerFreeMarker.class.getName());
	
    public void doGet( HttpServletRequest request, HttpServletResponse response )
    	throws IOException, ServletException {
        try {
            super.doGet(request,response);
            VitroRequest vreq = new VitroRequest(request);
            Portal portal = vreq.getPortal();
            ServletContext sc = getServletContext();
            String templatePath = sc.getRealPath("/templates/velocity");
            
            Properties p = new Properties();
            p.setProperty("file.resource.loader.path", templatePath);
            Velocity.init( p );
            
            VelocityContext vcontext = new VelocityContext();
            
            vcontext.put("title", "About " + portal.getAppName());
            vcontext.put("aboutText", portal.getAboutText());
            vcontext.put("acknowledgeText", portal.getAcknowledgeText());

            try {
            	Template template = Velocity.getTemplate("about.vm");
                StringWriter sw = new StringWriter();
                template.merge(vcontext, sw);
            }
            catch (ResourceNotFoundException e) {
            	// couldn't find the template
            }
            catch (ParseErrorException e) {
            	// problem parsing the template
            }           	
            catch (MethodInvocationException e) {
            	// something invoked in the template threw an exception
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


}
