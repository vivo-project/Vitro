/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.ContactMailServlet;

/**
 *  Controller for comments ("contact us") page
 *  * @author bjl23
 */
public class UserMailController extends VitroHttpServlet{

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException {
        super.doGet(request,response);
        VitroRequest vreq = new VitroRequest(request);
        try {
        //this try block passes any errors to error.jsp
            if (!ContactMailServlet.isSmtpHostConfigured()) {
                request.setAttribute("title", "Mail All Users Form");
                request.setAttribute("bodyJsp", "/contact_err.jsp");// <<< this is where the body gets set
                request.setAttribute("ERR","This application has not yet been configured to send mail -- " +
                		"smtp host has not been specified in the configuration properties file.");
                RequestDispatcher errd = request.getRequestDispatcher(Controllers.BASIC_JSP);
                errd.forward(request, response);
            }
            ApplicationBean appBean=vreq.getAppBean();
            Portal portalBean=vreq.getPortal();

            if ( (appBean.getMaxSharedPortalId()-appBean.getMinSharedPortalId()) > 1
                    && ( (portalBean.getPortalId()    >= appBean.getMinSharedPortalId()
                          && portalBean.getPortalId() <= appBean.getMaxSharedPortalId() )
                          || portalBean.getPortalId() == appBean.getSharedPortalFlagNumeric() )
                ) {
                request.setAttribute("portalType","CALSResearch");
            } else
                if (portalBean.getAppName().equalsIgnoreCase("CALS Impact")){
                request.setAttribute("portalType", "CALSImpact");
            } else if (portalBean.getAppName().equalsIgnoreCase("VIVO")) {
                request.setAttribute("portalType", "VIVO");
            } else {
                request.setAttribute("portalType", "clone");
            }

            request.setAttribute("siteName",portalBean.getAppName());
            request.setAttribute("scripts","/js/commentsForm.js");

            if (request.getHeader("Referer") == null)
                request.getSession().setAttribute("commentsFormReferer","none");
            else
                request.getSession().setAttribute("commentsFormReferer",request.getHeader("Referer"));


            request.setAttribute("portalId",Integer.valueOf(portalBean.getPortalId()));

            request.setAttribute("title", portalBean.getAppName()+" Mail Users Form");
            request.setAttribute("bodyJsp", "/emailUsers.jsp");// <<< this is where the body gets set
            request.setAttribute("portalBean",portalBean);

            RequestDispatcher rd =
                request.getRequestDispatcher(Controllers.BASIC_JSP);
            rd.forward(request, response);

        } catch (Throwable e) {
            // This is how we use an error.jsp
            //it expects javax.servlet.jsp.jspException to be set to the
            //exception so that it can display the info out of that.
            request.setAttribute("javax.servlet.jsp.jspException", e);
            RequestDispatcher rd = request.getRequestDispatcher("/error.jsp");
            rd.include(request, response);
        }
    }
}
