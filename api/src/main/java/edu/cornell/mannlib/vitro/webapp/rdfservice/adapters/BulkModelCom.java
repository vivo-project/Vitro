package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;

public class BulkModelCom extends ModelCom {

	public BulkModelCom(Graph graph) {
		super(graph);
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
