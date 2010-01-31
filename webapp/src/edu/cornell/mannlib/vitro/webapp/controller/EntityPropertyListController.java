/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles requests for entity information.
 * The methods for sorting Property/ObjectPropertyStatement Lists are here also.
 * @author bdc34
 *
 */
public class EntityPropertyListController extends VitroHttpServlet {

    /**
     * This gets the Entity object in the requestScope "entity" and
     * sets up the property list for it.  After that a jsp is
     * called to draw the data.
     *
     * Expected parameters:
     *
     * Expected Attributes:
     * entity - set to entity to display properties for.
     *
     * @author bdc34
     */
    public void doGet( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        try {
            //we don't need to call super because this method does no DAO calls.
            //super.doGet(req, res);
            Object obj = req.getAttribute("entity");
            if( obj == null || !(obj instanceof Individual))
                throw new HelpException("EntityPropertyListController requires request.attribute 'entity' to be of"
                        +" type " + Individual.class.getName() );
            Individual entity =(Individual)obj;
            //sort property list in display order
            entity.sortForDisplay();
            req.setAttribute("entity",entity);

            RequestDispatcher rd = req.getRequestDispatcher(Controllers.ENTITY_PROP_LIST_JSP);
            rd.include(req,res);
        } catch (HelpException help){
            doHelp(res);
        } catch (Throwable e) {
            req.setAttribute("javax.servlet.jsp.jspException",e);
            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
            rd.forward(req, res);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException,IOException {
        doGet(request, response);
    }

    private void doHelp(HttpServletResponse res)
    throws IOException, ServletException {
        ServletOutputStream out = res.getOutputStream();
        res.setContentType("text/html; charset=UTF-8");
        out.println("<html><body><h2>Quick Notes on using EntityPropList:</h2>");
        out.println("<p>request.attributes 'entity' must be set by Entity servlet before calling."
                +" It should already be 'filled out.' </p>");
        out.println("</body></html>");
    }

    private class HelpException extends Throwable{

        public HelpException(String string) {
            super(string);
        }}
}
