/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.impl.JenaRDFParser;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.resultset.ResultSetFormat;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.controller.individual.IndividualController;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ResultFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.SparqlQueryUtils;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;


/**
 * Services a SPARQL query.  This will return a simple error message and a 501 if
 * there is no Model.
 *
 *
 * @author bdc34
 *
 */
public class SparqlQueryServlet extends BaseEditController {
    private static final Log log = LogFactory.getLog(SparqlQueryServlet.class.getName());
    
    /**
     * format configurations for SELECT queries.
     */
    protected static HashMap<String,RSFormatConfig> rsFormats = new HashMap<String,RSFormatConfig>();
   
    /**
     * format configurations for CONSTRUCT/DESCRIBE queries.
     */
    protected static HashMap<String,ModelFormatConfig> modelFormats = 
        new HashMap<String,ModelFormatConfig>();

    /**
     * Use this map to decide which MIME type is suited for the "accept" header.
     */
    public static final Map<String, Float> ACCEPTED_CONTENT_TYPES;

   
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
				SimplePermission.USE_SPARQL_QUERY_PAGE.ACTIONS)) {
    		return;
    	}

        VitroRequest vreq = new VitroRequest(request);

        Model model = vreq.getJenaOntModel(); 
        if( model == null ){
            doNoModelInContext(response);
            return;
        }

        String queryParam = vreq.getParameter("query");
        log.debug("queryParam was : " + queryParam);

        if( queryParam == null || "".equals(queryParam) ){
            doHelp(request,response);
            return;
        }
        
        String contentType = checkForContentType(vreq.getHeader("Accept"));        
        
        Query query = SparqlQueryUtils.create(queryParam);
        if( query.isSelectType() ){            
            String format =  contentType!=null ? contentType:vreq.getParameter("resultFormat");            
            RSFormatConfig formatConf = rsFormats.get(format);                    
            doSelect(response, queryParam, formatConf, vreq.getRDFService());            
        }else if( query.isAskType()){
            doAsk( queryParam, vreq.getRDFService(), response ); 
        }else if( query.isConstructType() ){
            String format = contentType != null ? contentType : vreq.getParameter("rdfResultFormat");                        
            if (format== null) {
                format= "RDF/XML-ABBREV";
            }            
            ModelFormatConfig formatConf = modelFormats.get(format);
            doConstruct(response, query, formatConf, vreq.getRDFService());
        }else{
            doHelp(request,response);            
        }
        return;
    }
    

    private void doAsk(String queryParam, RDFService rdfService,
            HttpServletResponse response) throws ServletException, IOException {
        
        // Irrespective of the ResultFormatParam, 
        // this always prints a boolean to the default OutputStream.
        String result;
        try {
            result = (rdfService.sparqlAskQuery(queryParam) == true) 
                ? "true" 
                : "false";
        } catch (RDFServiceException e) {
            throw new ServletException( "Could not execute ask query ", e );
        }
        PrintWriter p = response.getWriter();
        p.write(result);
        return;
    }
    
    /**
     * Execute the query and send the result to out. Attempt to
     * send the RDFService the same format as the rdfResultFormatParam
     * so that the results from the RDFService can be directly piped to the client.
     */
    private void doSelect(HttpServletResponse response,
                                String queryParam,
                                RSFormatConfig formatConf, 
                                RDFService rdfService 
                                ) throws ServletException {
        try {
            if( ! formatConf.converstionFromWireFormat ){
                response.setContentType( formatConf.responseMimeType );
                InputStream results;                
                results = rdfService.sparqlSelectQuery(queryParam, formatConf.wireFormat );
                pipe( results, response.getOutputStream() );
            }else{                        
                //always use JSON when conversion is needed.
                InputStream results = rdfService.sparqlSelectQuery(queryParam, ResultFormat.JSON );
                
                response.setContentType( formatConf.responseMimeType );
                            
                ResultSet rs = ResultSetFactory.fromJSON( results );            
                OutputStream out = response.getOutputStream();
                ResultSetFormatter.output(out, rs, formatConf.jenaResponseFormat);                
            }
        } catch (RDFServiceException e) {
            throw new ServletException("Cannot get result from the RDFService",e);
        } catch (IOException e) {
            throw new ServletException("Cannot perform SPARQL SELECT",e);
        }
    }
    

    /**
     * Execute the query and send the result to out. Attempt to
     * send the RDFService the same format as the rdfResultFormatParam
     * so that the results from the RDFService can be directly piped to the client.
     * @param rdfService 
     * @throws IOException 
     * @throws RDFServiceException 
     * @throws  
     */
    private void doConstruct( HttpServletResponse response, 
                               Query query, 
                               ModelFormatConfig formatConfig,
                               RDFService rdfService 
                               ) throws ServletException{
        try{
            InputStream rawResult = null;        
            if( query.isConstructType() ){                    
                rawResult= rdfService.sparqlConstructQuery( query.toString(), formatConfig.wireFormat );
            }else if ( query.isDescribeType() ){
                rawResult = rdfService.sparqlDescribeQuery( query.toString(), formatConfig.wireFormat );
            }
    
            response.setContentType(  formatConfig.responseMimeType );
            
            if( formatConfig.converstionFromWireFormat ){
                Model resultModel = RDFServiceUtils.parseModel( rawResult, formatConfig.wireFormat );                        
                if( "JSON-LD".equals( formatConfig.jenaResponseFormat )){
                    //since jena 2.6.4 doesn't support JSON-LD we do it                                                                   
                    try {
                        JenaRDFParser parser = new JenaRDFParser();
                        Object json = JSONLD.fromRDF(resultModel, parser);
                        JSONUtils.write(response.getWriter(), json);
                    } catch (JSONLDProcessingError e) {
                       throw new RDFServiceException("Could not convert from Jena model to JSON-LD", e);
                    }
                }else{
                    OutputStream out = response.getOutputStream();
                    resultModel.write(out, formatConfig.jenaResponseFormat );
                }
            }else{
                OutputStream out = response.getOutputStream();
                pipe( rawResult, out );
            }
        }catch( IOException ex){
            throw new ServletException("could not run SPARQL CONSTRUCT",ex);
        } catch (RDFServiceException ex) {
            throw new ServletException("could not run SPARQL CONSTRUCT",ex);
        }
    }

    private void pipe( InputStream in, OutputStream out) throws IOException{
        int size;
        byte[] buffer = new byte[4096];
        while( (size = in.read(buffer)) > -1 ) {
            out.write(buffer,0,size);
        }        
    }

    private void doNoModelInContext(HttpServletResponse res){
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
    	// The Skife library wouldn't quote and escape the normal way, 
    	// so I'm trying it manually.
        List<String> varNames = results.getResultVars();
        int width = varNames.size();
    	while (results.hasNext()) {
    		QuerySolution solution = (QuerySolution) results.next();
    		String[] valueArray = new String[width];
    		Iterator<String> varNameIt = varNames.iterator();
    		int index = 0;
    		while (varNameIt.hasNext()) {
    			String varName = varNameIt.next();
    			String value = null;
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
    					if (res != null) {
        					if (res.isAnon()) {
        						value = res.getId().toString();
        					} else {
        						value = res.getURI();
        					}
    					}
	    			} catch (Exception f) {}
    			}
    			valueArray[index] = value;
                index++;
    		}

			StringBuffer rowBuff = new StringBuffer();
			for (int i = 0; i < valueArray.length; i++) {
    			String value = valueArray[i];
    			if (value != null) {
    			    value.replaceAll("\"", "\\\"");
    			    rowBuff.append("\"").append(value).append("\"");
    			}
    			if (i + 1 < width) {
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
            VitroRequest vreq = new VitroRequest(req);
            
            OntologyDao daoObj = vreq.getUnfilteredWebappDaoFactory().getOntologyDao();
            List<Ontology> ontologiesObj = daoObj.getAllOntologies();
            ArrayList<String> prefixList = new ArrayList<String>();
            
            if(ontologiesObj !=null && ontologiesObj.size()>0){
            	for(Ontology ont: ontologiesObj) {
            		prefixList.add(ont.getPrefix() == null ? "(not yet specified)" : ont.getPrefix());
            		prefixList.add(ont.getURI() == null ? "" : ont.getURI());
            	}
            }
            else{
            	prefixList.add("<strong>" + "No Ontologies added" + "</strong>");
            	prefixList.add("<strong>" + "Load Ontologies" + "</strong>");
            }
            
            req.setAttribute("prefixList", prefixList);
            
            req.setAttribute("title","SPARQL Query");
            req.setAttribute("bodyJsp", "/admin/sparqlquery/sparqlForm.jsp");
            
            RequestDispatcher rd = req.getRequestDispatcher("/"+Controllers.BASIC_JSP);
            rd.forward(req,res);
    }

    /** Simple boolean vaule to improve the legibility of confiugrations. */
    private final static boolean CONVERT = true;

    /** Simple vaule to improve the legibility of confiugrations. */
    private final static String NO_CONVERSION = null;

    public static class FormatConfig{
        public String valueFromForm;
        public boolean converstionFromWireFormat;
        public String responseMimeType;
    }
    
    private static ModelFormatConfig[] fmts = {
        new ModelFormatConfig("RDF/XML", 
                              !CONVERT, ModelSerializationFormat.RDFXML,  NO_CONVERSION,    "application/rdf+xml" ),
        new ModelFormatConfig("RDF/XML-ABBREV", 
                              CONVERT,  ModelSerializationFormat.N3,      "RDF/XML-ABBREV", "application/rdf+xml" ),
        new ModelFormatConfig("N3", 
                              !CONVERT, ModelSerializationFormat.N3,      NO_CONVERSION,    "text/n3" ),
        new ModelFormatConfig("N-TRIPLE", 
                              !CONVERT, ModelSerializationFormat.NTRIPLE, NO_CONVERSION,    "text/plain" ),
        new ModelFormatConfig("TTL", 
                              CONVERT,  ModelSerializationFormat.N3,      "TTL",            "application/x-turtle" ),
        new ModelFormatConfig("JSON-LD", 
                              CONVERT,  ModelSerializationFormat.N3,      "JSON-LD",        "application/javascript" ) };

    public static class ModelFormatConfig extends FormatConfig{                
        public RDFService.ModelSerializationFormat wireFormat;
        public String jenaResponseFormat;        
        
        public ModelFormatConfig( String valueFromForm,
                                  boolean converstionFromWireFormat, 
                                  RDFService.ModelSerializationFormat wireFormat, 
                                  String jenaResponseFormat, 
                                  String responseMimeType){
            this.valueFromForm = valueFromForm;
            this.converstionFromWireFormat = converstionFromWireFormat;
            this.wireFormat = wireFormat;
            this.jenaResponseFormat = jenaResponseFormat;
            this.responseMimeType = responseMimeType;
        }
    }


    private static RSFormatConfig[] rsfs = {
        new RSFormatConfig( "RS_XML", 
                            !CONVERT, ResultFormat.XML,  null, "text/xml"),
        new RSFormatConfig( "RS_TEXT", 
                            !CONVERT, ResultFormat.TEXT, null, "text/plain"),
        new RSFormatConfig( "vitro:csv", 
                            !CONVERT, ResultFormat.CSV,  null, "text/csv"),
        new RSFormatConfig( "RS_JSON", 
                            !CONVERT, ResultFormat.JSON, null, "application/javascript") }; 

    public static class RSFormatConfig extends FormatConfig{        
        public ResultFormat wireFormat;
        public ResultSetFormat jenaResponseFormat;
        
        public RSFormatConfig( String valueFromForm,
                               boolean converstionFromWireFormat,
                               ResultFormat wireFormat,
                               ResultSetFormat jenaResponseFormat,
                               String responseMimeType ){
            this.valueFromForm = valueFromForm;
            this.converstionFromWireFormat = converstionFromWireFormat;
            this.wireFormat = wireFormat;
            this.jenaResponseFormat = jenaResponseFormat;
            this.responseMimeType = responseMimeType;
        }    
    }

    static{
        HashMap<String, Float> map = new HashMap<String, Float>();
        
        /* move the lists of configurations into maps for easy lookup
         * by both MIME content type and the parameters from the form */
        for( RSFormatConfig rsfc : rsfs ){
            rsFormats.put( rsfc.valueFromForm, rsfc );
            rsFormats.put( rsfc.responseMimeType, rsfc);
            map.put(rsfc.responseMimeType, 1.0f);
        }
        for( ModelFormatConfig mfc : fmts ){
            modelFormats.put( mfc.valueFromForm, mfc);
            modelFormats.put(mfc.responseMimeType, mfc);
            map.put(mfc.responseMimeType, 1.0f);
        }
        
        ACCEPTED_CONTENT_TYPES = Collections.unmodifiableMap(map);
    }


    /**
     * Get the content type based on content negotiation.
     * Returns null of no content type can be agreed on or
     * if there is no accept header.   
     */
    protected String checkForContentType( String acceptHeader ) {        
        if (acceptHeader == null) 
            return null;        
    
        try {
            Map<String, Float> typesAndQ = ContentType
                    .getTypesAndQ(acceptHeader);
            
            String ctStr = ContentType
                    .getBestContentType(typesAndQ,ACCEPTED_CONTENT_TYPES); 
                    
            if( ACCEPTED_CONTENT_TYPES.containsKey( ctStr )){
                return ctStr;
            }
        } catch (Throwable th) {
            log.error("Problem while checking accept header ", th);
        }
        return null;
    }
}
