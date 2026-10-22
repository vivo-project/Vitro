/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.io.OutputStream;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderUtilities;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ResultFormat;
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
 * So if the configuration looks like this:
 * 
 * <pre>
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
 * Then this request:
 * 
 * <pre>
 *    dataRequest/sampleAction?person=http%3A%2F%2Fmy.domain.edu%2Findividual%2Fn1234&amp;topic=Oncology
 * </pre>
 * 
 * Will execute this query:
 * 
 * <pre>
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
public class SelectFromContentDistributor extends AbstractSparqlBindingDistributor {
    private RequestModelAccess models;
    private String rawQuery;
    private String resultFormat = "JSON";

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#query", minOccurs = 1, maxOccurs = 1)
    public void setRawQuery(String query) {
        rawQuery = query;
    }

    @Override
    public void init(DataDistributorContext ddc) throws DataDistributorException {
        super.init(ddc);
        this.models = ddc.getRequestModels();
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#resultFormat", maxOccurs = 1)
    public void setResultFormat(String resultFormat) {
        this.resultFormat = resultFormat;
    }

    @Override
    public String getContentType() throws DataDistributorException {
        return getContentType(resultFormat);
    }

    @Override
    public void writeOutput(OutputStream output) throws DataDistributorException {
        binder.checkAuthorization(ddContext, uriBindingNames);
        QueryHolder boundQuery = binder.bindValuesToQuery(uriBindingNames, literalBindingNames,
                new QueryHolder(rawQuery));
        RDFService rdfService;
        if (GraphBuilderUtilities.isLanguageFilteringDisabledForRequest(ddContext)) {
            rdfService = ModelAccess.getInstance().getRDFService();
        } else {
            rdfService = this.models.getRDFService();
        }

        createSelectQueryContext(rdfService, boundQuery).execute().writeToOutput(output,
                ResultFormat.valueOf(resultFormat));
    }

    @Override
    public void close() throws DataDistributorException {
        // Nothing to do.
    }

}
