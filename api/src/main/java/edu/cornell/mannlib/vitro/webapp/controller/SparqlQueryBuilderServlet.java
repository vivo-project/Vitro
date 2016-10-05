/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.resultset.ResultsFormat;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;

/**
 *  This servlet works as a RequestDispatcher to direct to the sparl query builder page.
 *  @author yuysun
 */
public class SparqlQueryBuilderServlet extends BaseEditController {

    private static final Log log = LogFactory.getLog(SparqlQueryBuilderServlet.class.getName());

    protected static final Syntax SYNTAX = Syntax.syntaxARQ;
    
    protected static HashMap<String,ResultsFormat>formatSymbols = new HashMap<String,ResultsFormat>();
    static{
        formatSymbols.put( ResultsFormat.FMT_RS_XML.getSymbol(),   ResultsFormat.FMT_RS_XML);
        formatSymbols.put( ResultsFormat.FMT_RDF_XML.getSymbol(),  ResultsFormat.FMT_RDF_XML);
        formatSymbols.put( ResultsFormat.FMT_RDF_N3.getSymbol(),   ResultsFormat.FMT_RDF_N3);
        formatSymbols.put( ResultsFormat.FMT_TEXT.getSymbol() ,    ResultsFormat.FMT_TEXT);
        formatSymbols.put( ResultsFormat.FMT_RS_JSON.getSymbol() , ResultsFormat.FMT_RS_JSON);
        formatSymbols.put( "vitro:csv", null);
    }
    
    protected static HashMap<String,String> rdfFormatSymbols = new HashMap<String,String>();
    static {
    	rdfFormatSymbols.put( "RDF/XML", "application/rdf+xml" );
    	rdfFormatSymbols.put( "RDF/XML-ABBREV", "application/rdf+xml" );
    	rdfFormatSymbols.put( "N3", "text/n3" );
    	rdfFormatSymbols.put( "N-TRIPLE", "text/plain" );
    	rdfFormatSymbols.put( "TTL", "application/x-turtle" );
    }

    protected static HashMap<String, String>mimeTypes = new HashMap<String,String>();
    static{
        mimeTypes.put( ResultsFormat.FMT_RS_XML.getSymbol() ,         "text/xml" );
        mimeTypes.put( ResultsFormat.FMT_RDF_XML.getSymbol(),      "application/rdf+xml"  );
        mimeTypes.put( ResultsFormat.FMT_RDF_N3.getSymbol(),       "text/plain" );
        mimeTypes.put( ResultsFormat.FMT_TEXT.getSymbol() ,        "text/plain");
        mimeTypes.put( ResultsFormat.FMT_RS_JSON.getSymbol(),         "application/javascript" );
        mimeTypes.put( "vitro:csv",                                    "text/csv");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        this.doGet(request,response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {    	    	   	
		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION)) {
    		return;
    	}

    	VitroRequest vreq = new VitroRequest(request);
        
        Model model = vreq.getJenaOntModel(); // getModel()
        if( model == null ){
            doNoModelInContext(request,response);
            return;
        }

        doHelp(request,response);
        return;
    }
    
    private void doNoModelInContext(HttpServletRequest request, HttpServletResponse res){
        try {
            res.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
            ServletOutputStream sos = res.getOutputStream();
            sos.println("<html><body>this service is not supporeted by the current " +
                    "webapp configuration. A jena model is required in the servlet context.</body></html>" );
        } catch (IOException e) {
            log.error("Could not write to ServletOutputStream");
        }
    }

    private void doHelp(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setAttribute("title","SPARQL Query Builder");

        JSPPageHandler.renderBasicPage(req, res, "/admin/sparql.jsp");
    }
}
