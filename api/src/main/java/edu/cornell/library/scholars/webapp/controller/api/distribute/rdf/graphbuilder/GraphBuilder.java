/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import org.apache.jena.rdf.model.Model;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;

/**
 * Creates a local RDF graph to be distributed, or to use as the context for
 * queries.
 */
public interface GraphBuilder {
    /**
     * Call zero or more times. Each call should initialize as necessary and
     * close resources on completion.
     */
    Model buildGraph(DataDistributorContext ddContext)
            throws DataDistributorException;
}
