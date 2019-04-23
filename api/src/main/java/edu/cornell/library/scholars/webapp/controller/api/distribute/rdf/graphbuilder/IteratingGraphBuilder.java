/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderUtilities.EnhancedDataDistributionContext;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderUtilities.GraphBuilders;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * A decorator that runs one or more subordinate graph builders multiple times,
 * modifying the map of request parameters each time. The results are
 * accumulated and returned as a single RDF graph.
 * 
 * You provide:
 * 
 * <ul>
 * <li>the name of the request parameter that will be added to the map</li>
 * <li>one or more values for the request parameter</li>
 * <li>one or more subordinate graph builders</li>
 * </ul>
 * 
 * NOTE: if the named parameter already appears with one or more values in the
 * parameter map, then each value provided here will be added in turn to the
 * array of values for that parameter.
 * 
 * <p>
 * So if the configuration looks like this: <pre>
 * :sample_distributor
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.RdfGraphDistributor&gt; ;
 *     :actionName "sampleAction" ;
 *     :graphBuilder :iterating_graph_builder .
 *     
 * :iterating_graph_builder
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.IteratingGraphBuilder&gt; .
 *     :parameterName "subjectArea" ;
 *     :parameterValue "cancer", "tissues", "echidnae" ;
 *     :childGraphBuilder :subordinate_graph_builder .
 * 
 * :subordinate_graph_builder
 *     a   &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder&gt; ,
 *         &lt;java:edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.ConstructQueryGraphBuilder&gt; .
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
 * Then this request:
 * 
 * <pre>
 *    dataRequest/sampleAction
 * </pre>
 * 
 * will be treated by iterator as if it were these three successive requests:
 * 
 * <pre>
 *    dataRequest/sampleAction?subjectArea=cancer
 *    dataRequest/sampleAction?subjectArea=tissues
 *    dataRequest/sampleAction?subjectArea=echidnae
 * </pre>
 * 
 * and the subordinate graph builder will execute these three queries against
 * the VIVO content model:
 * 
 * <pre>
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         ?article foo:hasKeyword "cancer" .
 *       }
 *       WHERE {
 *         ?article foo:hasKeyword "cancer" .
 *       }
 *       
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         ?article foo:hasKeyword "tissues" .
 *       }
 *       WHERE {
 *         ?article foo:hasKeyword "tissues" .
 *       }
 *
 *       PREFIX foo: &lt;http://some.silly.domain/foo#&gt;
 *       CONSTRUCT {
 *         ?article foo:hasKeyword "echidnae" .
 *       }
 *       WHERE {
 *         ?article foo:hasKeyword "echidnae" .
 *       }
 * </pre>
 * 
 * The results will be merged into a single graph, and distributed as RDF in
 * Turtle format.
 */

public class IteratingGraphBuilder implements GraphBuilder {
    private String parameterName;
    private final List<String> parameterValues = new ArrayList<>();
    private final List<GraphBuilder> childGraphBuilders = new ArrayList<>();

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#parameterName", minOccurs = 1, maxOccurs = 1)
    public void setParameterName(String name) {
        this.parameterName = name;
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#parameterValue", minOccurs = 1)
    public void addParameterValue(String value) {
        this.parameterValues.add(value);
    }

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#childGraphBuilder", minOccurs = 1)
    public void addChildGraphBuilder(GraphBuilder builder) {
        this.childGraphBuilders.add(builder);
    }

    @Override
    public Model buildGraph(DataDistributorContext ddContext)
            throws DataDistributorException {
        Model graph = ModelFactory.createDefaultModel();
        for (String parameterValue : parameterValues) {
            DataDistributorContext enhancedContext = new EnhancedDataDistributionContext(
                    ddContext).addParameterValue(parameterName, parameterValue);
            graph.add(new GraphBuilders(enhancedContext, childGraphBuilders).run());
        }
        return graph;
    }

}
