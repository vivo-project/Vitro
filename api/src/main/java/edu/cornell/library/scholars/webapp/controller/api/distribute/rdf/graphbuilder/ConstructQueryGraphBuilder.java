/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.formatParameters;
import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createConstructQueryContext;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.SelectFromContentDistributor;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

/**
 * Run one or more construct queries to build the graph. Bind parameters from
 * the request, as needed. You provide:
 *
 * <ul>
 * <li>the query string(s)</li>
 * <li>names of request parameters whose values will be bound as URIs in the
 * query</li>
 * <li>names of request parameters whose values will be bound as plain literals
 * in the query</li>
 * </ul>
 * 
 * For details of the variable binding, see
 * {@link SelectFromContentDistributor}.
 * 
 * <p>
 * So if the configuration looks like this: <pre>
 * :sample_distributor
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.RdfGraphDistributor&gt; ;
 *     :actionName "sampleAction" ;
 *     :graphBuilder :construct_graph_builder .
 *     
 * :construct_graph_builder
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.ConstructQueryGraphBuilder&gt; ;
 *     :literalBinding "subjectArea" ;
 *     :constructQuery """
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         ?article foo:hasKeyword ?subjectArea .
 *       }
 *       WHERE {
 *         ?article foo:hasKeyword ?subjectArea .
 *       }
 *     """ .
 * </pre>
 * 
 * Then this request: <pre>
 *    dataRequest/sampleAction?subjectArea=cancer
 * </pre>
 * 
 * Will execute this query: <pre>
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         ?article foo:hasKeyword "cancer" .
 *       }
 *       WHERE {
 *         ?article foo:hasKeyword "cancer" .
 *       }
 * </pre>
 * 
 * against the VIVO content model, distributing the result as RDF in Turtle
 * format.
 * 
 * If the constructQuery property is repeated, the bindings will apply to all
 * queries. The sequence of execution of the queries is indeterminate.
 */
public class ConstructQueryGraphBuilder
        extends AbstractSparqlBindingGraphBuilder {
    private static final Log log = LogFactory
            .getLog(ConstructQueryGraphBuilder.class);

    private List<String> rawQueries = new ArrayList<>();

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#constructQuery", minOccurs = 1)
    public void addRawQuery(String query) {
        rawQueries.add(query);
    }

    @Override
    public Model buildGraph(DataDistributorContext ddContext)
            throws DataDistributorException {
        log.debug("Parameters: " + formatParameters(ddContext));

        RDFService rdfService = ddContext.getRequestModels().getRDFService();
        Model m = ModelFactory.createDefaultModel();

        for (String rawQuery : rawQueries) {
            QueryHolder query = bindParametersToQuery(ddContext,
                    new QueryHolder(rawQuery));
            log.debug("Query is: " + query);

            m.add(createConstructQueryContext(rdfService, query).execute()
                    .toModel());
        }
        return m;
    }
}
