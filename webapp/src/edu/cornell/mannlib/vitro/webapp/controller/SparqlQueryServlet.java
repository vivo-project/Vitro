/* $This file is distributed under the terms of the license in /doc/license.txt$ */

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;


/**
 * Services a sparql query.  This will return a simple error message and a 501 if
 * there is no jena Model.
 *
 * @author bdc34
 *
 */
public class SparqlQueryServlet extends BaseEditController {

    private static final Log log = LogFactory.getLog(SparqlQueryServlet.class.getName());

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
        // rjy7 Allows any editor (including self-editors) access to this servlet.
        // This servlet is now requested via Ajax from some custom forms, so anyone
        // using the custom form needs access rights.

        // TODO Actually, this only allows someone who is logged in to use this servlet.
        // If a self-editor is not logged in, they will not have access. -- jb
        if( !checkLoginStatus(request, response) ) {
        	return;
        }
        
        VitroRequest vreq = new VitroRequest(request);

        Model model = vreq.getJenaOntModel(); // getModel()
        if( model == null ){
            doNoModelInContext(request,response);
            return;
        }

        String queryParam = vreq.getParameter("query");
        log.debug("queryParam was : " + queryParam);

        String resultFormatParam = vreq.getParameter("resultFormat");
        log.debug("resultFormat was: " + resultFormatParam);
        
        String rdfResultFormatParam = vreq.getParameter("rdfResultFormat");
        if (rdfResultFormatParam == null) {
        	rdfResultFormatParam = "RDF/XML-ABBREV";
        }
        log.debug("rdfResultFormat was: " + rdfResultFormatParam);

        if( queryParam == null || "".equals(queryParam) ||
            resultFormatParam == null || "".equals(resultFormatParam) ||
            !formatSymbols.containsKey(resultFormatParam) || 
            rdfResultFormatParam == null || "".equals(rdfResultFormatParam) ||
            !rdfFormatSymbols.keySet().contains(rdfResultFormatParam) ) {
            doHelp(request,response);
            return;
        }
        
        DataSource dataSource = DatasetFactory.create() ;
        ModelMaker maker = (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker");

        boolean someModelSet = false;        
        String models[] = request.getParameterValues("sourceModelName");        
        if( models != null && models.length > 0 ){
        	OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            for( String modelName : models ){
                Model modelNamed = maker.getModel(modelName);
                if( modelNamed != null ){
                    dataSource.addNamedModel(modelName, modelNamed) ;
                	// For now, people expect to query these graphs without using 
                	// FROM NAMED, so we'll also add to the background
                	ontModel.addSubModel(modelNamed);
                    someModelSet = true;
                }
            }         
            if (someModelSet) {
            	dataSource.setDefaultModel(ontModel);
            }
        }
        
        if( ! someModelSet )
            dataSource.setDefaultModel(model) ;
        
        executeQuery(request, response, resultFormatParam, rdfResultFormatParam, queryParam, dataSource); 
        return;
    }
    
    private void executeQuery(HttpServletRequest req,
            HttpServletResponse response, String resultFormatParam, String rdfResultFormatParam, String queryParam, DataSource dataSource ) throws IOException {
        
    	ResultSetFormat rsf = null;
    	/* BJL23 2008-11-06
    	 * modified to support CSV output.
    	 * Unfortunately, ARQ doesn't make it easy to
    	 * do this by implementing a new ResultSetFormat, because 
    	 * ResultSetFormatter is hardwired with expected values.
    	 * This slightly ugly approach will have to do for now. 
    	 */
        if ( !("vitro:csv").equals(resultFormatParam) ) {
        	rsf = formatSymbols.get(resultFormatParam);
        }                       
        String mimeType = mimeTypes.get(resultFormatParam);
        
        QueryExecution qe = null;
        try{
            Query query = QueryFactory.create(queryParam, SYNTAX);
            qe = QueryExecutionFactory.create(query, dataSource);
            if( query.isSelectType() ){
                ResultSet results = null;
                results = qe.execSelect();
                response.setContentType(mimeType);
                if (rsf != null) {
                	OutputStream out = response.getOutputStream();
                	ResultSetFormatter.output(out, results, rsf);
                } else {
                	Writer out = response.getWriter();
                	toCsv(out, results);
                }
            } else {
                Model resultModel = null;
                if( query.isConstructType() ){
                    resultModel = qe.execConstruct();
                }else if ( query.isDescribeType() ){
                    resultModel = qe.execDescribe();
                }
                response.setContentType(rdfFormatSymbols.get(rdfResultFormatParam));
                OutputStream out = response.getOutputStream();
                resultModel.write(out, rdfResultFormatParam);
            }
        }finally{
            if( qe != null)
                qe.close();
        }        
    }

    /*
    private Model getModel(){
        Model model = null;
        try{
        model = (Model)getServletContext().getAttribute("jenaOntModel");
        }catch(Throwable thr){
            log.error("could not cast getServletContext().getAttribute(\"jenaOntModel\") to Model");
        }
        return model;
    }
    */

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

    private void toCsv(Writer out, ResultSet results) {
    	//SimpleWriter csvWriter = new SimpleWriter(out);
    	//csvWriter.setSeperator(','); // sic
    	//char[] quoteChars = {'"'};
    	//csvWriter.setQuoteCharacters(quoteChars);
    	
    	// The Skife library wouldn't quote and escape the normal way, so I'm trying it manually
    	
    	while (results.hasNext()) {
    		QuerySolution solution = (QuerySolution) results.next();
    		List<String> valueList = new LinkedList<String>();
    		Iterator varNameIt = solution.varNames();
    		while (varNameIt.hasNext()) {
    			String varName = (String) varNameIt.next();
    			String value = "";
    			try {
    				Literal lit = solution.getLiteral(varName);
    				if (lit != null) { 
    					value = lit.getLexicalForm();
    					if (XSD.anyURI.getURI().equals(lit.getDatatypeURI())) {
    						value = URLDecoder.decode(value, "UTF-8");
    					}
    				}
    			} catch (Exception e) {
    				try {
    					Resource res = solution.getResource(varName);
    					if (res.isAnon()) {
    						value = res.getId().toString();
    					} else {
    						value = res.getURI();
    					}
	    			} catch (Exception f) {}
    			}
    			valueList.add(value);
    		}
    		/*
    		try {
    			csvWriter.append(valueList.toArray());
    		} catch (IOException ioe) {
    			log.error(ioe);
    		}
    		*/
    		Iterator<String> valueIt = valueList.iterator();
			StringBuffer rowBuff = new StringBuffer();
    		while (valueIt.hasNext()) {
    			String value = valueIt.next();
    			value.replaceAll("\"", "\\\"");

    			rowBuff.append("\"").append(value).append("\"");
    			if (valueIt.hasNext()) {
    				rowBuff.append(",");
    			}
    		}
    		rowBuff.append("\n");
    		try {
    			out.write(rowBuff.toString());
    		} catch (IOException ioe) {
    			log.error(ioe);
    		}
    	}
    	try {
    		out.flush();
    	} catch (IOException ioe) {
    		log.error(ioe);
    	}
    }
    
    private void doHelp(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
                	        	
            //res.setStatus(HttpServletResponse.SC_BAD_REQUEST);        	            
            
            VitroRequest vreq = new VitroRequest(req);
            Portal portal = vreq.getPortal();
            
            /* @author ass92 */
                  
            
            OntologyDao daoObj = vreq.getFullWebappDaoFactory().getOntologyDao();
            List ontologiesObj = daoObj.getAllOntologies();
            ArrayList prefixList = new ArrayList();
            
            if(ontologiesObj !=null && ontologiesObj.size()>0){
            	
            	Iterator ontItr = ontologiesObj.iterator();
            	while(ontItr.hasNext()){
            		Ontology ont = (Ontology) ontItr.next();
            		prefixList.add(ont.getPrefix() == null ? "(not yet specified)" : ont.getPrefix());
            		prefixList.add(ont.getURI() == null ? "" : ont.getURI());
            	}
            	
            }
            else{
            	prefixList.add("<strong>" + "No Ontologies added" + "</strong>");
            	prefixList.add("<strong>" + "Load Ontologies" + "</strong>");
            }
            
            req.setAttribute("prefixList", prefixList);
            
            /* Code change completed */
            
            req.setAttribute("portalBean",portal);
            // nac26: 2009-09-25 - this was causing problems in safari on localhost installations because the href did not include the context.  The edit.css is not being used here anyway (or anywhere else for that matter)
            // req.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");
            req.setAttribute("title","SPARQL Query");
            req.setAttribute("bodyJsp", "/admin/sparqlquery/sparqlForm.jsp");
            
            RequestDispatcher rd = req.getRequestDispatcher("/"+Controllers.BASIC_JSP);
            rd.forward(req,res);
//        } catch (Throwable e) {
//            log.error(e);
//            req.setAttribute("javax.servlet.jsp.jspException",e);
//            RequestDispatcher rd = req.getRequestDispatcher("/error.jsp");
//            rd.forward(req, res);
//        }
//            ServletOutputStream sos = res.getOutputStream();
//
//            sos.println("<html><body><h1>Sparql Query Form</h1>");
//            if( query == null || "".equals(query)){
//                sos.println("<p>The parameter 'query' must be not be null or zero length</p>");
//            }
//            if( resultFormat == null || "".equals(resultFormat)){
//                sos.println("<p>The parameter 'resultFormat' must not be null or zero length</p>");
//            }
//            if( !formatSymbols.containsKey(resultFormat) ){
//                sos.println("<p>The parameter 'resultFormat' must be one of the following: <ul>");
//                for ( String str : formatSymbols.keySet() ){
//                    sos.println("<li>" + str + "</li>");
//                }
//                sos.println("</ul></p>");
//            }
//            sos.println("<form action='sparqlquery'>");
//            sos.println("query:<div><textarea name='query' rows ='25' cols='80'>"+query+"</textarea></div>");
//            sos.println("<div>");
//            for( String str : formatSymbols.keySet() ){
//                sos.println(" "+str+":<input type='radio' name='resultFormat' value='"+str+"'>\n");
//            }
//            sos.println("</div>");
//            sos.println("<input type=\"submit\" value=\"Submit\">");
//            sos.println("</form>");
//            sos.println("<p>note: CONSTRUCT and DESCRIBE queries always return RDF XML</p>");
//            sos.println("</body></html>");
    }

    /*
    "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
    "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n"+
    "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7>\n"+
    "PREFIX vivo:  <http://vivo.library.cornell.edu/ns/0.1#>\n"+
    "PREFIX mann:  <http://vivo.cornell.edu/ns/mannadditions/0.1#>"+
    "#\n"+
    "# This query gets all range entities labels and types of Steven Tanksley\n"+
    "# A query like this could be used to get enough info to create a display\n"+
    "# page for an entity.\n"+
    "#\n"+
    "SELECT ?pred ?obj ?objLabel ?objType\n"+
    "WHERE \n"+
    "{\n"+
    " ?steve rdfs:label \"Tanksley, Steven D.\" .\n"+
    " ?steve ?pred ?obj \n"+
    " OPTIONAL { ?obj rdfs:label ?objLabel ; rdf:type ?objType }\n"+
    "}\n"+
    "ORDER BY ASC( ?pred )\n";
    */

    private String example =
        "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
        "PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n"+
        "PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>\n"+
        "PREFIX owl:   <http://www.w3.org/2002/07/owl#>\n" +
        "PREFIX swrl:  <http://www.w3.org/2003/11/swrl#>\n" +
        "PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>\n" +
        "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>\n"+
        "PREFIX vivo:  <http://vivo.library.cornell.edu/ns/0.1#>\n" +
        "PREFIX bibo: <http://purl.org/ontology/bibo/>\n" +
        "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
        "PREFIX core: <http://vivoweb.org/ontology/core#>\n" +
        "PREFIX aktp:  <http://www.aktors.org/ontology/portal#>\n"+
        "#\n" +
        "# This query gets all range entities labels and types of a person\n"+
        "# A query like this could be used to get enough info to create a display\n"+
        "# page for an entity.\n"+
        "#\n"+
        "SELECT ?person ?personLabel ?focus ?netid\n"+
        "WHERE \n"+
        "{\n"+
        " ?person vivo:CornellemailnetId ?netid .\n"+
        " ?person rdf:type vivo:CornellEmployee .\n"+
        " ?person vivo:researchFocus ?focus. \n"+
        " OPTIONAL { ?person rdfs:label ?personLabel }\n"+
        "}\n"+
        "limit 20\n";
}
