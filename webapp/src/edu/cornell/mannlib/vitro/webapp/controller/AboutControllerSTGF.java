/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.template.stringtemplate.PageSTGF;
import edu.cornell.mannlib.vitro.webapp.utils.StringTemplateUtil;

public class AboutControllerSTGF extends VitroHttpServlet {
	
	private static final Log log = LogFactory.getLog(AboutControllerSTGF.class.getName());
	
    public void doGet( HttpServletRequest request, HttpServletResponse response )
    	throws IOException, ServletException {
        try {
            super.doGet(request,response);
            VitroRequest vreq = new VitroRequest(request);
            Portal portal = vreq.getPortal();
            
            PageSTGF p = new AboutPage(portal);
            p.setRequest(vreq);
            p.setResponse(response);
            p.generate();
            
        } catch (Throwable e) {
            log.error("AboutControllerSTGF could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    	doGet(request, response);
    }
    
    private class AboutPage extends PageSTGF { 	

    	// RY This should be static with a static initializer; see notes in PageSTGF
    	private StringTemplateGroup aboutTemplates;	

    	// RY Note to myself: constructors are not inherited, so this must be declared 
    	// for each subclass of Page.
    	// Not sure if the portal should be handled in the constructor. That might have
    	// implications for template caching.
    	public AboutPage(Portal portal) {
    		super(AboutControllerSTGF.this.getServletContext(), portal);
    		aboutTemplates = initTemplateGroup("about.stg");
    	}

        public StringTemplate body() {
        	StringTemplate bodyST = aboutTemplates.getInstanceOf("main");
        	
        	StringTemplateUtil.setTemplateStringAttribute(bodyST, "aboutText", portal.getAboutText());
        	StringTemplateUtil.setTemplateStringAttribute(bodyST, "acknowledgeText", portal.getAcknowledgeText());
        	
        	return bodyST;
        }

        public String getTitle() { 
        	return "About " + portal.getAppName();
        }
       
    }

}
