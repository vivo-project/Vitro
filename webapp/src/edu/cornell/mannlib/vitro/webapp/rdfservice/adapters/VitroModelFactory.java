/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Make models that will do proper bulk updates.
 */
public class VitroModelFactory {

	public static OntModel createOntologyModel() {
		return new BulkUpdatingOntModel();
	}

	public static OntModel createOntologyModel(Model model) {
		return new BulkUpdatingOntModel(model);
	}

	public static Model createUnion(Model baseModel, Model otherModel) {
		@SuppressWarnings("deprecation")
		BulkUpdateHandler buh = baseModel.getGraph().getBulkUpdateHandler();

		Graph unionGraph = ModelFactory.createUnion(baseModel, otherModel)
				.getGraph();
		return new BulkUpdatingModel(unionGraph, buh);
	}

	public static OntModel createUnion(OntModel baseModel, OntModel otherModel) {
		return new BulkUpdatingOntModel(createUnion((Model) baseModel,
				(Model) otherModel));
	}

	public static Model createModelForGraph(Graph g) {
		return new BulkUpdatingModel(g);
	}

}
