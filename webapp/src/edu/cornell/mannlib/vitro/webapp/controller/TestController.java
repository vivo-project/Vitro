/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQuery;
import edu.cornell.mannlib.vitro.webapp.search.beans.VitroQueryWrapper;
import edu.cornell.mannlib.vitro.webapp.web.EntityWebUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * for testing styles and JSPs
 *
 * @author bdc34
 *
 */
public class TestController extends VitroHttpServlet {
    private static final Log log = LogFactory.getLog(EntityController.class.getName());

    /**
     *
     * @author bdc34
     */
    public void doGet( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        try {
            super.doGet(req, res);

            VitroRequest vreq = new VitroRequest(req);

            Portal portal = vreq.getPortal();
            vreq.setAttribute("portal",String.valueOf(portal));

            String body = vreq.getParameter("bodyJsp");
            if( body != null )
                vreq.setAttribute("bodyJsp" , body);
            else
                vreq.setAttribute("bodyJsp", Controllers.EMPTY);


            //set title before we do the highlighting so we don't get markup in it.
            vreq.setAttribute("title","Test Page");

            String css = "<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\""
                + portal.getThemeDir() + "css/entity.css\"/>\n"
                + "<script language='JavaScript' type='text/javascript' src='js/toggle.js'></script>";
            vreq.setAttribute("css",css);

            RequestDispatcher rd = vreq.getRequestDispatcher(Controllers.BASIC_JSP);
            rd.forward(req,res);
        } catch (Throwable e) {
            log.error(e);
            req.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
            rd.forward(req, res);
        }
    }



    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }

}