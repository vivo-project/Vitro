/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;
import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;

/**
 *  Controller for comments ("contact us") page
 *  * @author bjl23
 */
public class UserMailController extends VitroHttpServlet{

    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
	public void doGet( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException {
        super.doGet(request,response);
        VitroRequest vreq = new VitroRequest(request);
        try {
        //this try block passes any errors to error.jsp
            if (!FreemarkerEmailFactory.isConfigured(request)) {
                request.setAttribute("title", "Mail All Users Form");
				request.setAttribute("ERR",
						"This application has not yet been configured to send mail. "
								+ "Email properties must be specified in the configuration properties file.");
                JSPPageHandler.renderBasicPage(request, response, "/contact_err.jsp");// <<< this is where the body gets set
            }
            ApplicationBean appBean=vreq.getAppBean();

            request.setAttribute("siteName", appBean.getApplicationName());
            request.setAttribute("scripts","/js/commentsForm.js");

            if (request.getHeader("Referer") == null)
                request.getSession().setAttribute("commentsFormReferer","none");
            else
                request.getSession().setAttribute("commentsFormReferer",request.getHeader("Referer"));

            request.setAttribute("title", appBean.getApplicationName()+" Mail Users Form");
            JSPPageHandler.renderBasicPage(request, response, "/templates/parts/emailUsers.jsp");// <<< this is where the body gets set
        } catch (Throwable e) {
            // This is how we use an error.jsp
            //it expects javax.servlet.jsp.jspException to be set to the
            //exception so that it can display the info out of that.
            request.setAttribute("javax.servlet.jsp.jspException", e);
            JSPPageHandler.renderPlainInclude(request, response, "/error.jsp");
        }
    }
}
