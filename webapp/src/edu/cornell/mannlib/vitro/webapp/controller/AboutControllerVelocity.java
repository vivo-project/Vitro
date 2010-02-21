/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.template.velocity.VelocityHttpServlet;

public class AboutControllerVelocity extends VelocityHttpServlet {
	
	private static final Log log = LogFactory.getLog(AboutControllerVelocity.class.getName());

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    	doGet(request, response);
    }
    
    protected String getTitle() {
    	return "About " + portal.getAppName();
    }
    
    protected String getBody() {
    	// put stuff in context
    	// return a string that's the body template - use merge to put the values into the template
    	return "";
    }


}
