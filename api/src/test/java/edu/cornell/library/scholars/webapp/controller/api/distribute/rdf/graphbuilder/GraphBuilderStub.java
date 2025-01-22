/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.graphbuilder;

import static edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext.arraysToLists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * TODO
 */
public class GraphBuilderStub implements GraphBuilder {
    // ----------------------------------------------------------------------
    // Stub infrastructure
    // ----------------------------------------------------------------------

    private final List<Map<String, List<String>>> parameterMaps = new ArrayList<>();

    private Model graph = ModelFactory.createDefaultModel();

    public GraphBuilderStub setGraph(Model g) {
        this.graph = g;
        return this;
    }

    public List<Map<String, List<String>>> getParameterMaps() {
        return parameterMaps;
    }

    // ----------------------------------------------------------------------
    // Stub methods
    // ----------------------------------------------------------------------

    @Override
    public Model buildGraph(DataDistributorContext ddContext)
            throws DataDistributorException {
        parameterMaps.add(arraysToLists(ddContext.getRequestParameters()));
        return ModelFactory.createDefaultModel().add(graph);
    }

    @Override
    public String getBuilderName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setBuilderName(String name) {
        // TODO Auto-generated method stub

    }

}
