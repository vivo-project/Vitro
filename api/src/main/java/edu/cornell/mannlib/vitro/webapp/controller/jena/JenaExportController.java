/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.APPLICATION_METADATA;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_INFERENCES;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_UNION;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaOutputUtils;

public class JenaExportController extends BaseEditController {
	private static final AuthorizationRequest REQUIRED_ACTIONS = SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION
			.or(SimplePermission.EDIT_ONTOLOGY.ACTION);

	
	private static final Log log = LogFactory.getLog(JenaExportController.class);
	
	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response ) {
		if (!isAuthorizedToDisplayPage(request, response, REQUIRED_ACTIONS)) {
			return;
		}

		VitroRequest vreq = new VitroRequest(request);
		
		if ( vreq.getRequestURL().indexOf("/download/") > -1 ) { 
			outputRDF( vreq, response );
			return;
		}
		
		String formatParam = vreq.getParameter("format");
		
		if (formatParam != null) {
			redirectToDownload( vreq, response );
		} else {
			prepareExportSelectionPage( vreq, response );
		}
		
	}
	
	private void redirectToDownload( VitroRequest vreq, HttpServletResponse response ) {
		String formatStr = vreq.getParameter("format");
		String subgraphParam = vreq.getParameter("subgraph");
		String filename = null;
		if ("abox".equals(subgraphParam)) {
			filename = "ABox";
		} else if ("tbox".equals(subgraphParam)) {
			filename = "TBox";
		} else {
			filename = "export";
		}
		String extension =
			( (formatStr != null) && formatStr.startsWith("RDF/XML") && "tbox".equals(subgraphParam) ) 
			? ".owl"
			: formatToExtension.get(formatStr);
		if (extension == null) {
			throw new RuntimeException("Unsupported RDF export format "+formatStr);
		}
		String[] uriParts = vreq.getRequestURI().split("/");
		String base = uriParts[uriParts.length-1];
		try {
			response.sendRedirect("./"+base+"/download/"+filename+extension+"?"+vreq.getQueryString());
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	private void outputRDF( VitroRequest vreq, HttpServletResponse response ) {
		Dataset dataset = vreq.getDataset();
		JenaModelUtils xutil = new JenaModelUtils();
		String formatParam = vreq.getParameter("format");
		String subgraphParam = vreq.getParameter("subgraph");
		String assertedOrInferredParam = vreq.getParameter("assertedOrInferred");
		String ontologyURI = vreq.getParameter("ontologyURI");
		
		Model model = null;
		OntModel ontModel = ModelFactory.createOntologyModel();
		
		if(!subgraphParam.equalsIgnoreCase("tbox") 
				&& !subgraphParam.equalsIgnoreCase("abox") 
				&& !subgraphParam.equalsIgnoreCase("full")){
			ontologyURI = subgraphParam;
			subgraphParam = "tbox";
			char[] uri =  ontologyURI.toCharArray();
			ontologyURI="";
			for(int i =0; i < uri.length-1;i++)
				ontologyURI = ontologyURI + uri[i];
		}
	
		if( "abox".equals(subgraphParam)){
			model = ModelFactory.createDefaultModel();
			if("inferred".equals(assertedOrInferredParam)){
				model = ModelAccess.on(getServletContext()).getOntModel(ABOX_INFERENCES);
			}
			else if("full".equals(assertedOrInferredParam)){
			    outputSparqlConstruct(ABOX_FULL_CONSTRUCT, formatParam, response);
			}
			else if("asserted".equals(assertedOrInferredParam)){
			    outputSparqlConstruct(ABOX_ASSERTED_CONSTRUCT, formatParam, response);
			}
		}
		else if("tbox".equals(subgraphParam)){
		    if ("inferred".equals(assertedOrInferredParam)) {
		        // the extraction won't work on just the inferred graph,
		        // so we'll extract the whole ontology and then include
		        // only those statements that are in the inferred graph
		        Model tempModel = xutil.extractTBox(
		        		ModelAccess.on(getServletContext()).getOntModel(TBOX_UNION),
		                ontologyURI);
		        Model inferenceModel = ModelAccess.on(getServletContext()).getOntModel(TBOX_INFERENCES);
		        inferenceModel.enterCriticalSection(Lock.READ);
		        try {
    		        model = tempModel.intersection(inferenceModel);
		        } finally {
		            inferenceModel.leaveCriticalSection();
		        }
		    } else if ("full".equals(assertedOrInferredParam)) {
                model = xutil.extractTBox(
                		ModelAccess.on(getServletContext()).getOntModel(TBOX_UNION),
                        ontologyURI);		        
		    } else {
                model = xutil.extractTBox(
                        ModelAccess.on(getServletContext()).getOntModel(TBOX_ASSERTIONS), ontologyURI);              		        
		    }
			
		}
		else if("full".equals(subgraphParam)){
			if("inferred".equals(assertedOrInferredParam)){
				ontModel = xutil.extractTBox(
						dataset, ontologyURI, ABOX_INFERENCES);
				ontModel.addSubModel(ModelAccess.on(getServletContext()).getOntModel(ABOX_INFERENCES));
				ontModel.addSubModel(ModelAccess.on(getServletContext()).getOntModel(TBOX_INFERENCES));
			}
			else if("full".equals(assertedOrInferredParam)){
			    outputSparqlConstruct(FULL_FULL_CONSTRUCT, formatParam, response);
			}
			else{
			    outputSparqlConstruct(FULL_ASSERTED_CONSTRUCT, formatParam, response);
			}
			
		}
		
		JenaOutputUtils.setNameSpacePrefixes(model,vreq.getWebappDaoFactory());
		setHeaders(response, formatParam);

		try {
			OutputStream outStream = response.getOutputStream();
			if ( formatParam.startsWith("RDF/XML") ) {
				outStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
			}
			// 2010-11-02 workaround for the fact that ARP now always seems to 
			// try to parse N3 using strict Turtle rules.  Avoiding headaches
			// by always serializing out as Turtle instead of using N3 sugar.
			try {
				if(!"full".equals(subgraphParam)) {
					model.write( outStream, "N3".equals(formatParam) ? "TTL" : formatParam );
				} else {
					ontModel.writeAll(outStream, "N3".equals(formatParam) ? "TTL" : formatParam, null );
				}
			} catch (JenaException je) {
				response.setContentType("text/plain");
				response.setHeader("content-disposition", "attachment; filename=" + "export.txt");

				if(!"full".equals(subgraphParam)) {
					model.write( outStream, "N-TRIPLE", null);
				} else {
					ontModel.writeAll(outStream, "N-TRIPLE", null );
				}
				
				log.info("exported model in N-TRIPLE format instead of N3 because there was a problem exporting in N3 format");
			}

			outStream.flush();
			outStream.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		
	}
	
	private void setHeaders(HttpServletResponse response, String formatParam) {
	       if ( formatParam == null ) {
	            formatParam = "RDF/XML-ABBREV";  // default
	        }
	        String mime = formatToMimetype.get( formatParam );
	        if ( mime == null ) {
	            throw new RuntimeException( "Unsupported RDF format " + formatParam);
	        }
	        
	        response.setContentType( mime );
	        if(mime.equals("application/rdf+xml"))
	            response.setHeader("content-disposition", "attachment; filename=" + "export.rdf");
	        else if(mime.equals("text/n3"))
	            response.setHeader("content-disposition", "attachment; filename=" + "export.n3");
	        else if(mime.equals("text/plain"))
	            response.setHeader("content-disposition", "attachment; filename=" + "export.txt");
	        else if(mime.equals("application/x-turtle"))
	            response.setHeader("content-disposition", "attachment; filename=" + "export.ttl");
	}
	
	private void outputSparqlConstruct(String queryStr, String formatParam, 
	        HttpServletResponse response) {
	    RDFService rdfService = ModelAccess.on(getServletContext()).getRDFService();
	    try {
	        setHeaders(response, formatParam);
	        OutputStream out = response.getOutputStream();
            if ( formatParam.startsWith("RDF/XML") ) {
                out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
            }
	        InputStream in = rdfService.sparqlConstructQuery(
	                queryStr, RDFServiceUtils.getSerializationFormatFromJenaString(
	                        formatParam));
	        IOUtils.copy(in, out);
	        out.flush();
	        out.close();
	    } catch (RDFServiceException e) {
	        throw new RuntimeException(e);
	    } catch (IOException ioe) {
	        throw new RuntimeException(ioe);
	    } finally {
	        rdfService.close();
	    }	    
	}
	
	private void prepareExportSelectionPage( VitroRequest vreq, HttpServletResponse response ) {
		try {
			JSPPageHandler.renderBasicPage(vreq, response, Controllers.EXPORT_SELECTION_JSP );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static final String FULL_GRAPH = "?g";
	static Map<String,String> formatToExtension;
	static Map<String,String> formatToMimetype;
	
	static {
		
		formatToExtension = new HashMap<String,String>();
		formatToExtension.put("RDF/XML",".rdf");
		formatToExtension.put("RDF/XML-ABBREV",".rdf");
		formatToExtension.put("N3",".n3");
		formatToExtension.put("N-TRIPLE",".nt");
		formatToExtension.put("TURTLE",".ttl");
		
		formatToMimetype = new HashMap<String,String>();
		formatToMimetype.put("RDF/XML","application/rdf+xml");
		formatToMimetype.put("RDF/XML-ABBREV","application/rdf+xml");
		formatToMimetype.put("N3","text/n3");
		formatToMimetype.put("N-TRIPLE", "text/plain");
		formatToMimetype.put("TURTLE", "application/x-turtle");
		
	}
	
	private static final String ABOX_FULL_CONSTRUCT = "CONSTRUCT { ?s ?p ?o } " +
	        "WHERE { GRAPH ?g { ?s ?p ?o } FILTER (!regex(str(?g), \"tbox\")) " +
            "FILTER (?g != <" + APPLICATION_METADATA + ">) }";
	
	private static final String ABOX_ASSERTED_CONSTRUCT = "CONSTRUCT { ?s ?p ?o } " +
            "WHERE { GRAPH ?g { ?s ?p ?o } FILTER (!regex(str(?g), \"tbox\")) " + 
	        "FILTER (?g != <" + ABOX_INFERENCES + ">) " +
	        "FILTER (?g != <" + APPLICATION_METADATA + ">) }";
	
	private static final String FULL_FULL_CONSTRUCT = "CONSTRUCT { ?s ?p ?o } " +
            "WHERE { ?s ?p ?o }";

    private static final String FULL_ASSERTED_CONSTRUCT = "CONSTRUCT { ?s ?p ?o } " +
            "WHERE { GRAPH ?g { ?s ?p ?o } " + 
            "FILTER (?g != <" + ABOX_INFERENCES + ">) " +
            "FILTER (?g != <" + TBOX_INFERENCES + ">) }";
    
}
