/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreeMarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class VitroHttpServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    protected static DateFormat publicDateFormat = new SimpleDateFormat("M/dd/yyyy");

    private static final Log log = LogFactory.getLog(VitroHttpServlet.class.getName());

    public final static String XHTML_MIMETYPE ="application/xhtml+xml";
    public final static String HTML_MIMETYPE ="text/html";
    
    public final static String RDFXML_MIMETYPE ="application/rdf+xml";
    public final static String N3_MIMETYPE ="text/n3"; //unofficial and unregistered
    public final static String TTL_MIMETYPE = "text/turtle"; //unofficial and unregistered
    
    /**
     * Setup the auth flag, portal flag and portal bean objects.
     * Put them in the request attributes.
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
                          throws ServletException, IOException
    {
        setup(request);
    }

    protected final void setup(HttpServletRequest request) {
        
        //check to see if VitroRequestPrep filter was run
        if( request.getAttribute("appBean") == null ||
            request.getAttribute("webappDaoFactory") == null ){
            log.warn("request scope was not prepared by VitroRequestPrep");
        }        
    }
   

    /**
     * doPost does the same thing as the doGet method
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
                           throws ServletException, IOException
    {
        doGet( request,response );
    }

}
