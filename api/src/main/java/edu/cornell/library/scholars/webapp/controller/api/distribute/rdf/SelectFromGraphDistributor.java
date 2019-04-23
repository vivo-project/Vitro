/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderUtilities.GraphBuilders;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

/**
 * Issue a SPARQL SELECT query against an internal graph, and return the results
 * as JSON. You provide:
 * <ul>
 * <li>the action name</li>
 * <li>the query string</li>
 * <li>names of request parameters whose values will be bound as URIs in the
 * query</li>
 * <li>names of request parameters whose values will be bound as plain literals
 * in the query</li>
 * <li>one or more graph builders; the SELECT query will run against the union
 * of their output.</li>
 * </ul>
 * 
 * For details of the variable binding, see
 * {@link SelectFromContentDistributor}.
 * <p>
 * So if the configuration looks like this: <pre>
 * :sample_select_from_graph_distributor
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.SelectFromGraphDistributor&gt; ;
 *     :actionName "sampleAction" ;
 *     :query """
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       SELECT ?article
 *       WHERE {
 *         ?person foo:isAuthor ?article .
 *       }
 *     """ ;
 *     :uriBinding "person" ;
 *     :graphBuilder :empty_graph_builder .
 *     
 * :empty_graph_builder
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.EmptyGraphBuilder&gt; .
 * </pre>
 * 
 * Then this request: <pre>
 *    dataRequest/sampleAction?person=http%3A%2F%2Fmy.domain.edu%2Findividual%2Fn1234
 * </pre>
 * 
 * Will execute this query: <pre>
 *    PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *    SELECT ?article
 *    WHERE {
 *      &lt;http://my.domain.edu/individual/n1234&gt; foo:isAuthor ?article .
 *    }
 * </pre>
 * 
 * against an empty graph, producing an empty result.
 */
public class SelectFromGraphDistributor
        extends AbstractSparqlBindingDistributor {
    private String rawQuery;
    private List<GraphBuilder> graphBuilders = new ArrayList<>();

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#query", minOccurs = 1, maxOccurs = 1)
    public void setRawQuery(String query) {
        rawQuery = query;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#graphBuilder", minOccurs = 1)
    public void addGraphBuilder(GraphBuilder builder) {
        graphBuilders.add(builder);
    }

    @Override
    public void init(DataDistributorContext ddc)
            throws DataDistributorException {
        super.init(ddc);
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
        Model graph = new GraphBuilders(ddContext, graphBuilders).run();
        createSelectQueryContext(graph, boundQuery).execute()
                .writeToOutput(output);
    }

    @Override
    public void close() throws DataDistributorException {
        // Nothing to do.
    }

}
