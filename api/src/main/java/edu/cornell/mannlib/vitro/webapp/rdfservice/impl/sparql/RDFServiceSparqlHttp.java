/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.sparql;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;

/**
 * This is a RDFService that passes SPARQL queries to
 * the SPARQL endpoint unaltered and without parsing them.
 * 
 *  This is useful if the endpoint accepts syntax that does
 *  not pass the ARQ SPARQL 1.1 parser.  The disadvantage
 *  of this is that it currently returns no useful debugging
 *  messages when there is a syntax error. 
 */
public class RDFServiceSparqlHttp extends RDFServiceSparql {

	public RDFServiceSparqlHttp(String readEndpointURI, String updateEndpointURI) {
		super(readEndpointURI, updateEndpointURI);
	}

	public RDFServiceSparqlHttp(String endpointURI) {
		super(endpointURI);
	}

	public RDFServiceSparqlHttp(String readEndpointURI,
			String updateEndpointURI, String defaultWriteGraphURI) {
		super(readEndpointURI, updateEndpointURI, defaultWriteGraphURI);
	}

	
	/**
	 * Performs a SPARQL construct query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param queryStr - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - type of serialization for RDF result of the SPARQL query
	 */
	@Override
	public InputStream sparqlConstructQuery(String queryStr,
			                                RDFServiceImpl.ModelSerializationFormat resultFormat) throws RDFServiceException {
		
		Model model = ModelFactory.createDefaultModel();
		//Query query = QueryFactory.create(queryStr);
		//QueryExecution qe = QueryExecutionFactory.sparqlService(readEndpointURI, queryStr);
		
		QueryEngineHTTP qeh = new QueryEngineHTTP( readEndpointURI, queryStr);
		try {
			qeh.execConstruct(model);
		} finally {
			qeh.close();
		}

		ByteArrayOutputStream serializedModel = new ByteArrayOutputStream(); 
		model.write(serializedModel,getSerializationFormatString(resultFormat));
		InputStream result = new ByteArrayInputStream(serializedModel.toByteArray());
		return result;
	}

	@Override
	public void sparqlConstructQuery(String queryStr, Model model) throws RDFServiceException {

		QueryEngineHTTP qeh = new QueryEngineHTTP( readEndpointURI, queryStr);
		try {
			qeh.execConstruct(model);
		} finally {
			qeh.close();
		}
	}

	/**
	 * Performs a SPARQL describe query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param queryStr - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - type of serialization for RDF result of the SPARQL query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	@Override
	public InputStream sparqlDescribeQuery(String queryStr,
			                               RDFServiceImpl.ModelSerializationFormat resultFormat) throws RDFServiceException {
		
		Model model = ModelFactory.createDefaultModel();				
		QueryEngineHTTP qeh = new QueryEngineHTTP( readEndpointURI, queryStr);
		
		try {
			qeh.execDescribe(model);
		} finally {
			qeh.close();
		}

		ByteArrayOutputStream serializedModel = new ByteArrayOutputStream(); 
		model.write(serializedModel,getSerializationFormatString(resultFormat));
		InputStream result = new ByteArrayInputStream(serializedModel.toByteArray());
		return result;
	}

	/**
	 * Performs a SPARQL select query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param queryStr - the SPARQL query to be executed against the RDF store
	 * @param resultFormat - format for the result of the Select query
	 * 
	 * @return InputStream - the result of the query
	 * 
	 */
	@Override
	public InputStream sparqlSelectQuery(String queryStr, RDFService.ResultFormat resultFormat) throws RDFServiceException {		                

		QueryEngineHTTP qeh = new QueryEngineHTTP( readEndpointURI, queryStr);
        
        try {
        	ResultSet resultSet = qeh.execSelect();        	        	
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
        	
        	switch (resultFormat) {
        	   case CSV:
        		  ResultSetFormatter.outputAsCSV(outputStream,resultSet);
        		  break;
        	   case TEXT:
        		  ResultSetFormatter.out(outputStream,resultSet);
        		  break;
        	   case JSON:
        		  ResultSetFormatter.outputAsJSON(outputStream, resultSet);
        		  break;
        	   case XML:
        		  ResultSetFormatter.outputAsXML(outputStream, resultSet);
        		  break;
        	   default: 
        		  throw new RDFServiceException("unrecognized result format");
        	}
        	
        	InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
        	return result;
        	
        } finally {
            qeh.close();
        }
	}

	@Override
	public void sparqlSelectQuery(String queryStr, ResultSetConsumer consumer) throws RDFServiceException {

		QueryEngineHTTP qeh = new QueryEngineHTTP( readEndpointURI, queryStr);

		try {
			consumer.processResultSet(qeh.execSelect());
		} finally {
			qeh.close();
		}
	}

	/**
	 * Performs a SPARQL ASK query against the knowledge base. The query may have
	 * an embedded graph identifier.
	 * 
	 * @param queryStr - the SPARQL query to be executed against the RDF store
	 * 
	 * @return  boolean - the result of the SPARQL query 
	 */
	@Override
	public boolean sparqlAskQuery(String queryStr) throws RDFServiceException {
			    	    
	    QueryEngineHTTP qeh = new QueryEngineHTTP( readEndpointURI, queryStr);
	    
	    try {
	         return qeh.execAsk();
	    } finally {
	         qeh.close();
	    }
	}
	
}
