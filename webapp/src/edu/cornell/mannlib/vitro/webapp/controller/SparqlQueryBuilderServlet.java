/* $This file is distributed under the terms of the license in /doc/license.txt$ */
/*
Copyright (c) 2010, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;

/* @author ass92 */

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;


/**
 *  This servlet works as a RequestDispatcher to direct to the sparl query builder page.
 *  @author yuysun
 */

public class SparqlQueryBuilderServlet extends BaseEditController {

    private static final Log log = LogFactory.getLog(SparqlQueryBuilderServlet.class.getName());

    protected static final Syntax SYNTAX = Syntax.syntaxARQ;
    
    protected static HashMap<String,ResultSetFormat>formatSymbols = new HashMap<String,ResultSetFormat>();
    static{
        formatSymbols.put( ResultSetFormat.syntaxXML.getSymbol(),     ResultSetFormat.syntaxXML);
        formatSymbols.put( ResultSetFormat.syntaxRDF_XML.getSymbol(), ResultSetFormat.syntaxRDF_XML);
        formatSymbols.put( ResultSetFormat.syntaxRDF_N3.getSymbol(),  ResultSetFormat.syntaxRDF_N3);
        formatSymbols.put( ResultSetFormat.syntaxText.getSymbol() ,   ResultSetFormat.syntaxText);
        formatSymbols.put( ResultSetFormat.syntaxJSON.getSymbol() ,   ResultSetFormat.syntaxJSON);
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
        mimeTypes.put( ResultSetFormat.syntaxXML.getSymbol() ,         "text/xml" );
        mimeTypes.put( ResultSetFormat.syntaxRDF_XML.getSymbol(),      "application/rdf+xml"  );
        mimeTypes.put( ResultSetFormat.syntaxRDF_N3.getSymbol(),       "text/plain" );
        mimeTypes.put( ResultSetFormat.syntaxText.getSymbol() ,        "text/plain");
        mimeTypes.put( ResultSetFormat.syntaxJSON.getSymbol(),         "application/javascript" );
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
        super.doGet(request, response);
        if( !checkLoginStatus(request, response) )
        	return;
        
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
                	        	
            //res.setStatus(HttpServletResponse.SC_BAD_REQUEST);        	            
            
            VitroRequest vreq = new VitroRequest(req);
            Portal portal = vreq.getPortal();
            
            /* Code change completed */
            
            req.setAttribute("portalBean",portal);
            // nac26: 2009-09-25 - this was causing problems in safari on localhost installations because the href did not include the context.  The edit.css is not being used here anyway (or anywhere else for that matter)
            // req.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");
            req.setAttribute("title","SPARQL Query Builder");
            req.setAttribute("bodyJsp", "/admin/sparql.jsp");
            
            RequestDispatcher rd = req.getRequestDispatcher("/"+Controllers.BASIC_JSP);
            rd.forward(req,res);
    }
}
