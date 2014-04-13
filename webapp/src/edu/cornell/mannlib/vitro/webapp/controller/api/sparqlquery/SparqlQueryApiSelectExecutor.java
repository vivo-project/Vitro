/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

import java.io.InputStream;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ResultFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;

/**
 * Process SELECT queries.
 */
public class SparqlQueryApiSelectExecutor extends
		SparqlQueryApiResultSetProducer {

	public SparqlQueryApiSelectExecutor(RDFService rdfService,
			String queryString, String acceptHeader)
			throws AcceptHeaderParsingException, NotAcceptableException {
		super(rdfService, queryString, acceptHeader);
	}

	@Override
	protected InputStream getRawResultStream() throws RDFServiceException {
		ResultFormat format = ResultFormat.valueOf(mediaType
				.getRdfServiceFormat());
		return rdfService.sparqlSelectQuery(queryString, format);
	}

}
