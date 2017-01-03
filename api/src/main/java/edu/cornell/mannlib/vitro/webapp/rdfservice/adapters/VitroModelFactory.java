/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import static org.apache.jena.ontology.OntModelSpec.OWL_MEM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.impl.OntModelImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.ModelCom;

import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * Make models that will do proper bulk updates.
 */
public class VitroModelFactory {
	private static final Log log = LogFactory.getLog(VitroModelFactory.class);

	public static Model createModel() {
		return ModelFactory.createDefaultModel();
	}

	public static OntModel createOntologyModel() {
		return ModelFactory.createOntologyModel(OWL_MEM);
	}

	public static OntModel createOntologyModel(Model model) {
		Graph graph = model.getGraph();
		Model bareModel = new ModelCom(graph);
		OntModel ontModel = new OntModelImpl(OWL_MEM, bareModel);
		return new BulkUpdatingOntModel(ontModel);
	}

	public static Model createUnion(Model baseModel, Model plusModel) {
		Graph baseGraph = baseModel.getGraph();
		Graph plusGraph = plusModel.getGraph();

		BulkUpdatingUnion unionGraph = new BulkUpdatingUnion(baseGraph, plusGraph);

		Model unionModel = ModelFactory.createModelForGraph(unionGraph);

		return new BulkUpdatingModel(unionModel, baseGraph);
	}

	public static OntModel createUnion(OntModel baseModel, OntModel plusModel) {
		Graph baseGraph = baseModel.getGraph();
		Graph plusGraph = plusModel.getGraph();

		BulkUpdatingUnion unionGraph = new BulkUpdatingUnion(baseGraph, plusGraph);

		Model unionModel = ModelFactory.createModelForGraph(unionGraph);
		OntModel unionOntModel = ModelFactory.createOntologyModel(OWL_MEM, unionModel);

		return new BulkUpdatingOntModel(unionOntModel, baseGraph);
	}

	public static Model createModelForGraph(Graph g) {
		return new BulkUpdatingModel(ModelFactory.createModelForGraph(g));
	}

	private static class BulkUpdatingUnion extends Union {
		@SuppressWarnings("deprecation")
		public BulkUpdatingUnion(Graph L, Graph R) {
			super(L, R);
		}

		@Override
		public String toString() {
			return "BulkUpdatingUnion[" + ToString.hashHex(this) + ", L="
					+ ToString.graphToString(L) + ", R="
					+ ToString.graphToString(R) + "]";
		}
	}
}
