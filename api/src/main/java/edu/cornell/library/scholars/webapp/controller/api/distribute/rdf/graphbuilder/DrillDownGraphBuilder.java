/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.SelectFromContentDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderUtilities.EnhancedDataDistributionContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderUtilities.GraphBuilders;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.QueryHolder;

/**
 * A decorator that runs one or more subordinate graph builders zero or more
 * times, based the results of a SPARQL SELECT query. The results are
 * accumulated and returned as a single RDF graph.
 * <p>
 * This is similar in concept to an {@link IteratingGraphBuilder}, but the
 * iteration values are not configured; instead, they are discovered.
 * <p>
 * To discover the iteration values, the builder run one or more "top-level"
 * graph builders and runs a SELECT query against the resulting graph. The
 * results of the SELECT query determine how many times the "child" graph
 * builders are run, and what values will be added to their parameter maps on
 * each iteration.
 * <p>
 * The result is the union of the graphs from the top-level graph builders and
 * the child graph builders.
 * <p>
 * 
 * NOTE: if a named parameter from the SELECT results already appears with one
 * or more values in the parameter map, then each value provided here will be
 * added in turn to the array of values for that parameter.
 * 
 * 
 * <p>
 * You provide:
 * 
 * <ul>
 * <li>one or more top-level graph builders, to be used in the SELECT query</li>
 * <li>the SELECT query string</li>
 * <li>names of request parameters whose values will be bound as URIs in the
 * query</li>
 * <li>names of request parameters whose values will be bound as plain literals
 * in the query</li>
 * <li>one or more child (subordinate) graph builders</li>
 * </ul>
 * 
 * For details of the variable binding, see
 * {@link SelectFromContentDistributor}.
 * 
 * <p>
 * Consider a configuration that looks like this:
 * 
 * <pre>
 * :sample_distributor
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.RdfGraphDistributor&gt; ;
 *     :actionName "sampleAction" ;
 *     :graphBuilder :drilldown_graph_builder .
 *     
 * :drilldown_graph_builder
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.DrillDownGraphBuilder&gt; ;
 *     :topLevelGraphBuilder :top_level_builder ;
 *     :childGraphBuilder :child_graph_builder ;
 *     :drillDownQuery """
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       SELECT ?article ?subjectArea
 *       WHERE {
 *         foo:author_123 foo:writes ?article ;
 *         ?article foo:pertainsTo ?subjectArea .
 *       }
 *     """ .
 * 
 * :top_level_graph_builder
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.ConstructQueryGraphBuilder&gt; .
 *     :literalBinding "subjectArea" ;
 *     :constructQuery """
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         ?author foo:writes ?article ;
 *         ?article foo:pertainsTo ?subjectArea .
 *       }
 *       WHERE {
 *         ?author foo:writes ?article ;
 *         ?article ?property ?subjectArea .
 *       }
 *     """ .
 *     
 * :child_graph_builder
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.ConstructQueryGraphBuilder&gt; .
 *     :literalBinding "subjectArea" ;
 *     :constructQuery """
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         ?article ?property ?subjectArea .
 *       }
 *       WHERE {
 *         ?article ?property ?subjectArea .
 *       }
 *     """ .
 * </pre>
 * 
 * When this request is received:
 * 
 * <pre>
 *    dataRequest/sampleAction
 * </pre>
 * 
 * <ul>
 * <li>The top-level graph builder will extract all author/article/subjectArea
 * triples into a local graph.</li>
 * <li>The <code>drillDownQuery</code> will obtain the article/subjectArea
 * information for a particular author.
 * <li>The child graph builder will be run several times, once for each row in
 * the result set of the <code>drillDownQuery</code>.
 * </ul>
 * 
 * So, if the drillDownQuery returns these results:
 * 
 * <pre>
 * article = http://some.silly.domain/foo#article_142 subjectArea = cancer
 * article = http://some.silly.domain/foo#article_207 subjectArea = tissues
 * </pre>
 * 
 * Then the child graph builder will behave as if it had received these two
 * requests:
 * 
 * <pre>
 *    dataRequest/sampleAction?subjectArea=cancer&amp;article=http://some.silly.domain/foo#article_142
 *    dataRequest/sampleAction?subjectArea=tissues&amp;article=http://some.silly.domain/foo#article_207
 * </pre>
 * 
 * and will execute these two queries against the VIVO content model:
 * 
 * <pre>
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         &lt;http://some.silly.domain/foo#article_142&gt; ?property "cancer" .
 *       }
 *       WHERE {
 *         &lt;http://some.silly.domain/foo#article_142&gt; ?property "cancer" .
 *       }
 *       
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         &lt;http://some.silly.domain/foo#article_207&gt; ?property "tissues" .
 *       }
 *       WHERE {
 *         &lt;http://some.silly.domain/foo#article_207&gt; ?property "tissues" .
 *       }
 * </pre>
 * 
 * The results of all builders, top-level and children, will be merged into a
 * single graph, and distributed as RDF in Turtle format.
 */
public class DrillDownGraphBuilder extends AbstractSparqlBindingGraphBuilder {
    private static final Log log = LogFactory
            .getLog(DrillDownGraphBuilder.class);

    private List<GraphBuilder> topGraphBuilders = new ArrayList<>();
    private List<GraphBuilder> childGraphBuilders = new ArrayList<>();
    private String drillDownQuery;

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#topLevelGraphBuilder", minOccurs = 1)
    public void addTopLevelGraphBuilder(GraphBuilder builder) {
        topGraphBuilders.add(builder);
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#childGraphBuilder", minOccurs = 1)
    public void addChildGraphBuilder(GraphBuilder builder) {
        childGraphBuilders.add(builder);
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#drillDownQuery", minOccurs = 1, maxOccurs = 1)
    public void setDrillDownQuery(String query) {
        drillDownQuery = query;
    }

    @Override
    public Model buildGraph(DataDistributorContext ddContext)
            throws DataDistributorException {
        Model topGraph = new GraphBuilders(ddContext, topGraphBuilders).run();
        log.debug("Size of topGraph: " + topGraph.size());

        List<Map<String, String>> valueMaps = findDrillDownValues(topGraph,
                ddContext);
        log.debug("Results from SELECT: " + valueMaps);

        Model childBuildersGraph = runChildBuildersWithDrillDownValues(
                ddContext, valueMaps);
        return topGraph.add(childBuildersGraph);
    }

    private List<Map<String, String>> findDrillDownValues(Model topGraph,
            DataDistributorContext ddContext) throws DataDistributorException {
        QueryHolder query = bindParametersToQuery(ddContext,
                new QueryHolder(drillDownQuery));
        log.debug("binding the query\n   " + drillDownQuery + " becomes "
                + query.getQueryString());

        return createSelectQueryContext(topGraph, query).execute()
                .toStringFields().getListOfMaps();
    }

    private Model runChildBuildersWithDrillDownValues(
            DataDistributorContext ddContext,
            List<Map<String, String>> valueMaps)
            throws DataDistributorException {
        Model resultGraph = ModelFactory.createDefaultModel();
        for (Map<String, String> parameterSet : valueMaps) {
            resultGraph.add(runChildBuilders(ddContext, parameterSet));
        }
        return resultGraph;
    }

    private Model runChildBuilders(DataDistributorContext ddContext,
            Map<String, String> parameterSet) throws DataDistributorException {
        return new GraphBuilders(enhanceContext(ddContext, parameterSet),
                childGraphBuilders).run();
    }

    private EnhancedDataDistributionContext enhanceContext(
            DataDistributorContext ddContext,
            Map<String, String> parameterSet) {
        return new EnhancedDataDistributionContext(ddContext)
                .addParameterValues(parameterSet);
    }

}
