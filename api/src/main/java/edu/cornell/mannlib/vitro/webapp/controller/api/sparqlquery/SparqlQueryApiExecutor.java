/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.sparqlquery;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.utils.http.AcceptHeaderParsingException;
import edu.cornell.mannlib.vitro.webapp.utils.http.NotAcceptableException;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.SparqlQueryUtils;

/**
 * The base class for the SPARQL query API.
 */
public abstract class SparqlQueryApiExecutor {
	/**
	 * Get an instance that is appropriate to the query and the acceptable
	 * types.
	 * 
	 * @throws AcceptHeaderParsingException
	 *             if the accept header was not in a valid format
	 * @throws NotAcceptableException
	 *             if the accept header did not contain a content type that is
	 *             supported by the query
	 * @throws QueryParseException
	 *             if the query was not syntactically valid
	 * @throws InvalidQueryTypeException
	 *             if the query was not SELECT, ASK, CONSTRUCT, or DESCRIBE
	 */
	public static SparqlQueryApiExecutor instance(RDFService rdfService,
			String queryString, String acceptHeader)
			throws NotAcceptableException, QueryParseException,
			InvalidQueryTypeException, AcceptHeaderParsingException {
		if (rdfService == null) {
			throw new NullPointerException("rdfService may not be null.");
		}
		if (queryString == null) {
			throw new NullPointerException("queryString may not be null.");
		}

		Query query = SparqlQueryUtils.create(queryString);

		if (query.isSelectType()) {
			return new SparqlQueryApiSelectExecutor(rdfService, queryString,
					acceptHeader);
		} else if (query.isAskType()) {
			return new SparqlQueryApiAskExecutor(rdfService, queryString,
					acceptHeader);
		} else if (query.isConstructType()) {
			return new SparqlQueryApiConstructExecutor(rdfService, queryString,
					acceptHeader);
		} else if (query.isDescribeType()) {
			return new SparqlQueryApiDescribeExecutor(rdfService, queryString,
					acceptHeader);
		} else {
			throw new InvalidQueryTypeException("The API only accepts SELECT, "
					+ "ASK, CONSTRUCT, or DESCRIBE queries: '" + queryString
					+ "'");
		}
	}

	protected final RDFService rdfService;
	protected final String queryString;

	protected SparqlQueryApiExecutor(RDFService rdfService, String queryString) {
		this.rdfService = rdfService;
		this.queryString = queryString;
	}

	/**
	 * What media type was selected, based on the Accept header?
	 */
	public abstract String getMediaType();

	/**
	 * Execute the query and write it to the output stream, in the selected
	 * format.
	 */
	public abstract void executeAndFormat(OutputStream out)
			throws RDFServiceException, IOException;

}
