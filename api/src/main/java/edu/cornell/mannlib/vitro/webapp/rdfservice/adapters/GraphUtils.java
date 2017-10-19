/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;

import java.util.List;

final public class GraphUtils {
    public static Graph unwrapUnionGraphs(Graph graph) {
        if (graph != null && graph instanceof MultiUnion) {
            List<Graph> subGraphs = ((MultiUnion)graph).getSubGraphs();
            if (subGraphs == null || subGraphs.isEmpty()) {
                return ((MultiUnion)graph).getBaseGraph();
            }
        }

        return graph;
    }
}
