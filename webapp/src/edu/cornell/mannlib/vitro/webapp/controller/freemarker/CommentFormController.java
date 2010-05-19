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

/**
 *  Controller for comments ("contact us") page
 *  * @author bjl23
 */
public class CommentFormController extends FreeMarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(CommentFormController.class.getName());
    
    protected String getTitle() {
        return appName + " Feedback Form";
    }
    
    protected String getBody() {

        Map<String, Object> body = new HashMap<String, Object>();
        String bodyTemplate;
        
        if (!ContactMailServlet.isSmtpHostConfigured()) {
            body.put("errorMessage", 
                     "This application has not yet been configured to send mail. " +
                     "An smtp host has not been specified in the configuration properties file.");
            bodyTemplate = "commentForm/error.ftl";
        }
        
        else if (StringUtils.isEmpty(portal.getContactMail())) {
            body.put("errorMessage", 
                    "The site administrator has not configured an email address to receive the form submission.");
            bodyTemplate = "commentForm/error.ftl";            
        }
        
        else {

            ApplicationBean appBean = vreq.getAppBean();
            int portalId = portal.getPortalId();
          
            String portalType = null;
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

            // Not used in template. Is it used in processing the form?
            if (vreq.getHeader("Referer") == null) {
                vreq.getSession().setAttribute("commentsFormReferer","none");
            } else {
                vreq.getSession().setAttribute("commentsFormReferer",vreq.getHeader("Referer"));
            }
           
            bodyTemplate = "commentForm/form.ftl";
        }
        
        return mergeBodyToTemplate(bodyTemplate, body);
    }
}
