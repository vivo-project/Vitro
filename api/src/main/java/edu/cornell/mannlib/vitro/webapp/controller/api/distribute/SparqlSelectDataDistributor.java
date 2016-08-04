/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.io.OutputStream;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.SelectQueryContext;

/**
 * <pre>
 * Issue a SPARQL SELECT query and return the results as JSON. You provide:
 * - the action name
 * - the query string
 * - names of request parameters, whose values will be bound as URIs in the query
 * - names of request parameters, whose values will  be bound as plain literals in the query
 * 
 * So if the configuration looks like this:
 * :sample_sparql_query_distributor
 *     a   <java:edu.cornell.mannlib.vitro.webapp.controller.api.distribute.DataDistributor> ,
 *         <java:edu.cornell.mannlib.vitro.webapp.controller.api.distribute.SparqlSelectDataDistributor> ;
 *     :actionName "sampleAction" ;
 *     :query """
 *       PREFIX foo: <http://some.silly.domain/foo#>
 *       SELECT ?article
 *       WHERE {
 *         ?person foo:isAuthor ?article .
 *         ?article foo:hasTopic ?topic .
 *       }
 *     """ ;
 *     :uriBinding "person" ;
 *     :literalBinding "topic" .
 * 
 * Then this request: 
 *    dataRequest/sampleAction?person=http%3A%2F%2Fmy.domain.edu%2Findividual%2Fn1234&topic=Oncology
 *    
 * Will execute this query:
 *    PREFIX foo: <http://some.silly.domain/foo#>
 *    SELECT ?article
 *    WHERE {
 *      <http://my.domain.edu/individual/n1234> foo:isAuthor ?article .
 *      ?article foo:hasTopic "Oncology" .
 *    }
 * 
 * Each specified binding name must have exactly one value in the request parameters.
 * </pre>
 */
public class SparqlSelectDataDistributor extends SparqlSelectDataDistributorBase {

	private String rawQuery;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#query", minOccurs = 1, maxOccurs = 1)
	public void setRawQuery(String query) {
		rawQuery = query;
	}

	@Override
	public void writeOutput(OutputStream output)
			throws DataDistributorException {
		SelectQueryContext queryContext = createSelectQueryContext(
				this.models.getRDFService(), this.rawQuery);
		queryContext = binder.bindUriParameters(uriBindingNames, queryContext);
		queryContext = binder.bindLiteralParameters(literalBindingNames,
				queryContext);
		queryContext.execute().writeToOutput(output);
	}

	@Override
	public void close() throws DataDistributorException {
		// Nothing to do.
	}

}
