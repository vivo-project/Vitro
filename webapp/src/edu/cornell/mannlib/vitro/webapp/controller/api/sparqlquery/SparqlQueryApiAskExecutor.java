/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;

/**
 * Process ASK queries.
 */
public class SparqlQueryApiAskExecutor extends SparqlQueryApiResultSetProducer {
	public SparqlQueryApiAskExecutor(RDFService rdfService, String queryString,
			String acceptHeader) throws AcceptHeaderParsingException,
			NotAcceptableException {
		super(rdfService, queryString, acceptHeader);
	}

	/**
	 * The RDFService returns a boolean from an ASK query, without regard to a
	 * requested format.
	 * 
	 * For TEXT, CSV and TSV, we can simple return the String value of the
	 * boolean as an InputStream. For XML and JSON, however, the W3C documents
	 * require something a bit more fancy.
	 */
	@Override
	protected InputStream getRawResultStream() throws RDFServiceException {
		boolean queryResult = rdfService.sparqlAskQuery(queryString);
		String resultString;
		if (mediaType == ResultSetMediaType.XML) {
			resultString = String
					.format("<?xml version=\"1.0\"?>\n" //
							+ "<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">\n" //
							+ "  <head></head>\n" //
							+ "  <boolean>%b</boolean>\n" //
							+ "</sparql>", queryResult);
		} else if (mediaType == ResultSetMediaType.JSON) {
			resultString = String.format(
					"{\n  \"head\" : { } ,\n  \"boolean\" : %b\n}\n",
					queryResult);
		} else {
			resultString = String.valueOf(queryResult);
		}
		return new ByteArrayInputStream(resultString.getBytes());
	}

}
