/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.library.scholars.webapp.controller.api.distribute.AbstractDataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilder;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder.GraphBuilderUtilities.GraphBuilders;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Execute one or more GraphBuilders, merge the results, and write them out as
 * Turtle RDF.
 */
public class RdfGraphDistributor extends AbstractDataDistributor {
    private List<GraphBuilder> graphBuilders = new ArrayList<>();

    @Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#graphBuilder", minOccurs = 1)
    public void addGraphBuilder(GraphBuilder builder) {
        graphBuilders.add(builder);
    }

    @Override
    public String getContentType() throws DataDistributorException {
        return "text/turtle";
    }

    @Override
    public void writeOutput(OutputStream output)
            throws DataDistributorException {
        new GraphBuilders(ddContext, graphBuilders).run().write(output, "TTL");
    }

    @Override
    public void close() throws DataDistributorException {
        // Nothing to close.
    }

}
