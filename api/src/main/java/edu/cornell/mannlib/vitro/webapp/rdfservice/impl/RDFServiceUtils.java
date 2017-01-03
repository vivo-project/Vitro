/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ResultFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging.LoggingRDFServiceFactory;

public class RDFServiceUtils {
	private static final Log log = LogFactory.getLog(RDFServiceUtils.class);
	
    private static final String RDFSERVICEFACTORY_ATTR = 
            RDFServiceUtils.class.getName() + ".RDFServiceFactory";
    
    public static RDFServiceFactory getRDFServiceFactory(ServletContext context) {
    	return getRDFServiceFactory(context, WhichService.CONTENT);
    }
    
	/*
	 * Every factory is wrapped in a logger, so we can dynamically enable or
	 * disable logging.
	 */
	public static RDFServiceFactory getRDFServiceFactory(
			ServletContext context, WhichService which) {
		String attribute = RDFSERVICEFACTORY_ATTR + "." + which.name();
		Object o = context.getAttribute(attribute);
		if (o instanceof RDFServiceFactory) {
			RDFServiceFactory factory = (RDFServiceFactory) o;
			return new LoggingRDFServiceFactory(factory);
		} else {
			throw new IllegalStateException(
					"Expecting an RDFServiceFactory on the context at '"
							+ attribute + "', but found " + o);
		}
	}
    
    public static void setRDFServiceFactory(ServletContext context, 
    		RDFServiceFactory factory) {
    	setRDFServiceFactory(context, factory, WhichService.CONTENT);
    }
    
    public static void setRDFServiceFactory(ServletContext context, 
    		RDFServiceFactory factory, WhichService which) {
		String attribute = RDFSERVICEFACTORY_ATTR + "." + which.name();
    	context.setAttribute(attribute, factory);
    }
    
    public static InputStream toInputStream(String serializedRDF) {
        try {
            return new ByteArrayInputStream(serializedRDF.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Model parseModel(InputStream in, ModelSerializationFormat format) {
        Model model = ModelFactory.createDefaultModel();
        model.read(in, null,
                getSerializationFormatString(format));
        return model;
    }
    
    public static ResultsFormat getJenaResultSetFormat(ResultFormat resultFormat) {
        switch(resultFormat) {
            case JSON:
                return ResultsFormat.FMT_RS_JSON;
            case CSV:
                return ResultsFormat.FMT_RS_CSV;
            case XML:
                return ResultsFormat.FMT_RS_XML;
            case TEXT:
                return ResultsFormat.FMT_TEXT;
            default:
                throw new RuntimeException("unsupported ResultFormat");
        }
    }
    
    public static String getSerializationFormatString(RDFService.ModelSerializationFormat format) {
        switch (format) {
            case RDFXML: 
                return "RDF/XML";
            case N3: 
                return "N3";
            case NTRIPLE:
                return "N-TRIPLE";
            default: 
                throw new RuntimeException("unexpected format in getSerializationFormatString");
        }
    }
    
    public static ModelSerializationFormat getSerializationFormatFromJenaString(String jenaString) {
        if ("N3".equals(jenaString) || "TTL".equals(jenaString) 
                || "TURTLE".equals(jenaString)) {
            return ModelSerializationFormat.N3;
        } else if ("N-TRIPLE".equals(jenaString)) {
            return ModelSerializationFormat.NTRIPLE;
        } else if ("RDF/XML".equals(jenaString) 
                || "RDF/XML-ABBREV".equals(jenaString)) {
            return ModelSerializationFormat.RDFXML;
        } else {
            throw new RuntimeException("unrecognized format " + jenaString);
        }
    }
    
    public static RDFService getRDFService(VitroRequest vreq) {
    	return getRDFService(vreq, WhichService.CONTENT);
    }

    public static RDFService getRDFService(VitroRequest vreq, WhichService which) {
    	return getRDFServiceFactory(
    			vreq.getSession().getServletContext(), which).getRDFService();
    }
    
    public static ResultSet sparqlSelectQuery(String query, RDFService rdfService) {
    	
    	ResultSet resultSet = null;
    	
        try {
            InputStream resultStream = rdfService.sparqlSelectQuery(query, RDFService.ResultFormat.JSON);
            resultSet = ResultSetFactory.fromJSON(resultStream);
            return resultSet;
        } catch (RDFServiceException e) {        	
            log.error("error executing sparql select query: " + e.getMessage());
        }
        
        return resultSet;
    }    
}
