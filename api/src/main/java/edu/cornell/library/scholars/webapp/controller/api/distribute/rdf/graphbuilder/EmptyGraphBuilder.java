/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;

/**
 * Creates an empty RDF graph. For use as a placeholder or in tests.
 */
public class EmptyGraphBuilder extends AbstractGraphBuilder {

    @Override
    public Model buildGraph(DataDistributorContext ddContext)
            throws DataDistributorException {
        return ModelFactory.createDefaultModel();
    }

}
