/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;


import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.arraysToLists;
import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.listsToArrays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.RequestModelAccess;

/**
 * Helpful classes and methods for dealing with GraphBuilders.
 */
public class GraphBuilderUtilities {
    private static final Log log = LogFactory
            .getLog(GraphBuilderUtilities.class);

    /**
     * Run a collection of GraphBuilders and produce a merged result.
     */
    public static class GraphBuilders {
        private final DataDistributorContext ddContext;
        private final List<GraphBuilder> builders;

        public GraphBuilders(DataDistributorContext ddContext,
                Collection<GraphBuilder> builders) {
            this.ddContext = ddContext;
            this.builders = Collections
                    .unmodifiableList(new ArrayList<>(builders));
        }

        public Model run() throws DataDistributorException {
            Model graph = ModelFactory.createDefaultModel();
            for (GraphBuilder builder : builders) {
                graph.add(runBuilder(builder));
                log.debug("Graph size is  " + graph.size());
            }
            return graph;
        }

        private Model runBuilder(GraphBuilder builder)
                throws DataDistributorException {
            return builder.buildGraph(ddContext);
        }
    }

    public static class EnhancedDataDistributionContext
            implements DataDistributorContext {
        private final DataDistributorContext inner;
        private final Map<String, List<String>> enhancedParameters;

        public EnhancedDataDistributionContext(DataDistributorContext inner) {
            this.inner = inner;
            this.enhancedParameters = arraysToLists(
                    inner.getRequestParameters());
        }

        public EnhancedDataDistributionContext addParameterValue(String key,
                String value) {
            List<String> values = enhancedParameters.get(key);
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(value);
            enhancedParameters.put(key, values);
            return this;
        }
        
        public EnhancedDataDistributionContext addParameterValues(Map<String, String> map) {
            for (String key: map.keySet()) {
                addParameterValue(key, map.get(key));
            }
            return this;
        }
        
        @Override
        public Map<String, String[]> getRequestParameters() {
            return listsToArrays(enhancedParameters);
        }

        @Override
        public RequestModelAccess getRequestModels() {
            return inner.getRequestModels();
        }

        @Override
        public boolean isAuthorized(AuthorizationRequest actions) {
            return inner.isAuthorized(actions);
        }

    }
}
