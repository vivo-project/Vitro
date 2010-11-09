/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;

public class JenaExportController extends BaseEditController {

	public void doGet( HttpServletRequest request, HttpServletResponse response ) {
		
		VitroRequest vreq = new VitroRequest(request);
		
        if (!checkLoginStatus(vreq,response))
            return;
		
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
		JenaModelUtils xutil = new JenaModelUtils();
		String formatParam = vreq.getParameter("format");
		String subgraphParam = vreq.getParameter("subgraph");
		String assertedOrInferredParam = vreq.getParameter("assertedOrInferred");
		String ontologyURI = vreq.getParameter("ontologyURI");
		
		Model model = null;
		
		boolean limitToInferred = false;
		Model inferenceModel = null;
		
		if(!subgraphParam.equalsIgnoreCase("tbox") && !subgraphParam.equalsIgnoreCase("abox") && !subgraphParam.equalsIgnoreCase("full")){
			ontologyURI = subgraphParam;
			subgraphParam = "tbox";
			char[] uri =  ontologyURI.toCharArray();
			ontologyURI="";
			for(int i =0; i < uri.length-1;i++)
				ontologyURI = ontologyURI + uri[i];
		}
		if ( "inferred".equals(assertedOrInferredParam) ) {
			limitToInferred = true;
			inferenceModel = getOntModelFromAttribute( INFERENCES_ONT_MODEL_ATTR, vreq );
			model = inferenceModel;
		} else if ( "full".equals(assertedOrInferredParam) ) {
			model = getOntModelFromAttribute( FULL_ONT_MODEL_ATTR, vreq );
		} else { // default 
			model = getOntModelFromAttribute( ASSERTIONS_ONT_MODEL_ATTR, vreq );
		}
		
		if ( "abox".equals(subgraphParam) ) {
			if (limitToInferred) {
				Model fullModel = getOntModelFromAttribute( FULL_ONT_MODEL_ATTR, vreq );
				model = xutil.extractABox( fullModel );
				try { 
					inferenceModel.enterCriticalSection(Lock.READ);
					model = model.intersection(inferenceModel);
				} finally {
					inferenceModel.leaveCriticalSection();
				}
			} else {
				model = xutil.extractABox( model );
			}
		} else if ( "tbox".equals(subgraphParam) ) {
			if (limitToInferred) {
				Model fullModel = getOntModelFromAttribute( FULL_ONT_MODEL_ATTR, vreq );
				model = xutil.extractTBox( fullModel, ontologyURI );
				try { 
					inferenceModel.enterCriticalSection(Lock.READ);
					model = model.intersection(inferenceModel);
				} finally {
					inferenceModel.leaveCriticalSection();
				}
			} else {
				model = xutil.extractTBox( model, ontologyURI );
			}
		} 
		
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
			
		try {
			OutputStream outStream = response.getOutputStream();
			if ( formatParam.startsWith("RDF/XML") ) {
				outStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
			}
			// 2010-11-02 workaround for the fact that ARP now always seems to 
			// try to parse N3 using strict Turtle rules.  Avoiding headaches
			// by always serializing out as Turtle instead of using N3 sugar.
			model.write( outStream, "N3".equals(formatParam) ? "TTL" : formatParam );
			outStream.flush();
			outStream.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		
	}
	
	private void prepareExportSelectionPage( VitroRequest vreq, HttpServletResponse response ) {
		vreq.setAttribute( "bodyJsp", Controllers.EXPORT_SELECTION_JSP );
		RequestDispatcher dispatcher = vreq.getRequestDispatcher( Controllers.BASIC_JSP );
		try {
			dispatcher.forward( vreq, response );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private OntModel getOntModelFromAttribute( String attributeName, VitroRequest vreq ) {
		Object o = vreq.getAttribute( attributeName );
		if ( (o != null) && (o instanceof OntModel) ) {
			return (OntModel) o;
		} else {
			o = getServletContext().getAttribute( attributeName );
			if ( (o != null) && (o instanceof OntModel) ) {
				return (OntModel) o;
			} else {
				throw new RuntimeException("Unable to find OntModel in request or context attribute "+attributeName);
			}
		}
	}
	
	static final String FULL_ONT_MODEL_ATTR = "jenaOntModel";
	static final String ASSERTIONS_ONT_MODEL_ATTR = "baseOntModel";
	static final String INFERENCES_ONT_MODEL_ATTR = "inferenceOntModel";
	
	static Map<String,String> formatToExtension;
	static Map<String,String> formatToMimetype;
	
	static {
		
		formatToExtension = new HashMap<String,String>();
		formatToExtension.put("RDF/XML",".rdf");
		formatToExtension.put("RDF/XML-ABBREV",".rdf");
		formatToExtension.put("N3",".n3");
		formatToExtension.put("N-TRIPLES",".nt");
		formatToExtension.put("TURTLE",".ttl");
		
		formatToMimetype = new HashMap<String,String>();
		formatToMimetype.put("RDF/XML","application/rdf+xml");
		formatToMimetype.put("RDF/XML-ABBREV","application/rdf+xml");
		formatToMimetype.put("N3","text/n3");
		formatToMimetype.put("N-TRIPLES", "text/plain");
		formatToMimetype.put("TURTLE", "application/x-turtle");
		
	}
		
}
