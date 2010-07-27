/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.ContactMailServlet;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.StringUtils;
import freemarker.template.Configuration;

/**
 *  Controller for comments ("contact us") page
 *  * @author bjl23
 */
public class ContactFormController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ContactFormController.class.getName());
    
    protected String getTitle(String siteName) {
        return siteName + " Feedback Form";
    }
    
    protected String getBody(VitroRequest vreq, Map<String, Object> body, Configuration config) {

        String bodyTemplate;
        Portal portal = vreq.getPortal();
        
        if (!ContactMailServlet.isSmtpHostConfigured()) {
            body.put("errorMessage", 
                     "This application has not yet been configured to send mail. " +
                     "An smtp host has not been specified in the configuration properties file.");
            bodyTemplate = "contactForm-error.ftl";
        }
        
        else if (StringUtils.isEmpty(portal.getContactMail())) {
            body.put("errorMessage", 
            		"The feedback form is currently disabled. In order to activate the form, a site administrator must provide a contact email address in the <a href='editForm?home=1&amp;controller=Portal&amp;id=1'>Site Configuration</a>");
            
            bodyTemplate = "contactForm-error.ftl";            
        }
        
        else {

            ApplicationBean appBean = vreq.getAppBean();          
            String portalType = null;
            int portalId = portal.getPortalId();
            String appName = portal.getAppName();
            
            if ( (appBean.getMaxSharedPortalId()-appBean.getMinSharedPortalId()) > 1
                  && ( (portalId  >= appBean.getMinSharedPortalId()
                  && portalId <= appBean.getMaxSharedPortalId() )
                  || portalId == appBean.getSharedPortalFlagNumeric() ) ) {
                portalType = "CALSResearch";
            } else if (appName.equalsIgnoreCase("CALS Impact")) {
                portalType = "CALSImpact";
            } else if (appName.equalsIgnoreCase("VIVO")){
                portalType = "VIVO";
            } else {
                portalType = "clone";
            }
            body.put("portalType", portalType);
            
            body.put("portalId", portalId);
            body.put("formAction", "submitFeedback");

            if (vreq.getHeader("Referer") == null) {
                vreq.getSession().setAttribute("contactFormReferer","none");
            } else {
                vreq.getSession().setAttribute("contactFormReferer",vreq.getHeader("Referer"));
            }
           
            bodyTemplate = "contactForm-form.ftl";
        }
        
        return mergeBodyToTemplate(bodyTemplate, body, config);
    }
}
