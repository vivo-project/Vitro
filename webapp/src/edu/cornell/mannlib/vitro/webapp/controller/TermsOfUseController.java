/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;

/**
 * Controller for Terms of Use page
 * @author bjl23
 */
public class TermsOfUseController  extends VitroHttpServlet{
    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException {
        try {
            super.doGet(request,response);
            VitroRequest vreq = new VitroRequest(request);

            ApplicationBean appBean=vreq.getAppBean();
            Portal portalBean=vreq.getPortal();

            request.setAttribute("rootBreadCrumbAnchorOrAppName", portalBean.getRootBreadCrumbAnchor()==null || portalBean.getRootBreadCrumbAnchor().equals("")?portalBean.getAppName():portalBean.getRootBreadCrumbAnchor());
            request.setAttribute("copyrightAnchor", portalBean.getCopyrightAnchor());
            //request.setAttribute("rootLogotypeTitle", appBean.getRootLogotypeTitle()); THIS IS NOT POPULATED
            request.setAttribute("appName", portalBean.getAppName());

            request.setAttribute("title", portalBean.getAppName()+" Terms of Use");
            request.setAttribute("bodyJsp", "/usageTerms.jsp");// <<< this is where the body gets set
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
