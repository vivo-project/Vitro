/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import static com.hp.hpl.jena.ontology.OntModelSpec.OWL_MEM;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.Union;
import com.hp.hpl.jena.graph.impl.WrappedBulkUpdateHandler;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.impl.OntModelImpl;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

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
		BulkUpdatingUnion unionGraph = new BulkUpdatingUnion(baseGraph,
				plusGraph);

		BulkUpdateHandler buh = getBulkUpdateHandler(unionGraph);
		Model unionModel = ModelFactory.createModelForGraph(unionGraph);
		return new BulkUpdatingModel(unionModel, buh);
	}

	public static OntModel createUnion(OntModel baseModel, OntModel plusModel) {
		Graph baseGraph = baseModel.getGraph();
		Graph plusGraph = plusModel.getGraph();
		BulkUpdatingUnion unionGraph = new BulkUpdatingUnion(baseGraph,
				plusGraph);

		Model unionModel = ModelFactory.createModelForGraph(unionGraph);
		OntModel unionOntModel = ModelFactory.createOntologyModel(OWL_MEM,
				unionModel);
		return new BulkUpdatingOntModel(unionOntModel);
	}

	public static Model createModelForGraph(Graph g) {
		BulkUpdateHandler buh = getBulkUpdateHandler(g);
		return new BulkUpdatingModel(ModelFactory.createModelForGraph(g), buh);
	}

	private static class BulkUpdatingUnion extends Union {
		@SuppressWarnings("deprecation")
		public BulkUpdatingUnion(Graph L, Graph R) {
			super(L, R);
			this.bulkHandler = new WrappedBulkUpdateHandler(this,
					L.getBulkUpdateHandler());
		}

		@Override
		public String toString() {
			return "BulkUpdatingUnion[" + ToString.hashHex(this) + ", L="
					+ ToString.graphToString(L) + ", R="
					+ ToString.graphToString(R) + "]";
		}

		
	}

	@SuppressWarnings("deprecation")
	private static BulkUpdateHandler getBulkUpdateHandler(Graph graph) {
		return graph.getBulkUpdateHandler();
	}
}
