package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;

public class BulkOntModelImpl extends OntModelImpl {

    public BulkOntModelImpl(OntModelSpec spec) {
        super(spec);
    }

    public BulkOntModelImpl(OntModelSpec owlMem, Model bareModel) {
        super(owlMem, bareModel);
    }

    @Override
    public Model remove(Model m) {
        Graph unwrappedGraph = GraphUtils.unwrapUnionGraphs(graph);
        if (unwrappedGraph instanceof BulkGraphMem) {
            GraphUtils.deleteFrom((BulkGraphMem) unwrappedGraph, m.getGraph());
        } else {
            super.remove(m);
        }
        return this;
    }

    @Override
    public Model add(Model m) {
        Graph unwrappedGraph = GraphUtils.unwrapUnionGraphs(graph);
        if (unwrappedGraph instanceof BulkGraphMem) {
            GraphUtils.addInto((BulkGraphMem) unwrappedGraph, m.getGraph());
        } else {
            super.add(m);
        }
        return this;
    }
}
