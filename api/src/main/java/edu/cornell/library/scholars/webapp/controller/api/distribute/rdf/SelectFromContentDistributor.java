/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.io.OutputStream;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

/**
 * Issue a SPARQL SELECT query against VIVO's content models and return the
 * results as JSON. You provide:
 * <ul>
 * <li>the action name</li>
 * <li>the query string</li>
 * <li>names of request parameters whose values will be bound as URIs in the
 * query</li>
 * <li>names of request parameters whose values will be bound as plain literals
 * in the query</li>
 * </ul>
 * 
 * So if the configuration looks like this: <pre>
 * :sample_select_from_content_distributor
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.SelectFromContentDistributor&gt; ;
 *     :actionName "sampleAction" ;
 *     :query """
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       SELECT ?article
 *       WHERE {
 *         ?person foo:isAuthor ?article .
 *         ?article foo:hasTopic ?topic .
 *       }
 *     """ ;
 *     :uriBinding "person" ;
 *     :literalBinding "topic" .
 * </pre>
 * 
 * Then this request: <pre>
 *    dataRequest/sampleAction?person=http%3A%2F%2Fmy.domain.edu%2Findividual%2Fn1234&amp;topic=Oncology
 * </pre>
 * 
 * Will execute this query: <pre>
 *    PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *    SELECT ?article
 *    WHERE {
 *      &lt;http://my.domain.edu/individual/n1234&gt; foo:isAuthor ?article .
 *      ?article foo:hasTopic "Oncology" .
 *    }
 * </pre>
 * 
 * Each specified binding name must have exactly one value in the request
 * parameters.
 */
public class SelectFromContentDistributor
        extends AbstractSparqlBindingDistributor {
    private RequestModelAccess models;
    private String rawQuery;

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#query", minOccurs = 1, maxOccurs = 1)
    public void setRawQuery(String query) {
        rawQuery = query;
    }

    @Override
    public void init(DataDistributorContext ddc)
            throws DataDistributorException {
        super.init(ddc);
        this.models = ddc.getRequestModels();
    }

    @Override
    public String getContentType() throws DataDistributorException {
        return "application/sparql-results+json";
    }

    @Override
    public void writeOutput(OutputStream output)
            throws DataDistributorException {
        QueryHolder boundQuery = binder.bindValuesToQuery(uriBindingNames,
                literalBindingNames, new QueryHolder(rawQuery));
        createSelectQueryContext(this.models.getRDFService(), boundQuery)
                .execute().writeToOutput(output);
    }

    @Override
    public void close() throws DataDistributorException {
        // Nothing to do.
    }

}
