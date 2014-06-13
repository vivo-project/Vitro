/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import static com.hp.hpl.jena.ontology.OntModelSpec.OWL_MEM;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Make models that will do proper bulk updates.
 */
public class VitroModelFactory {
	public static Model createModel() {
		return ModelFactory.createDefaultModel();
	}

	public static OntModel createOntologyModel() {
		return ModelFactory.createOntologyModel(OWL_MEM);
	}
	
	public static OntModel createOntologyModel(Model model) {
		@SuppressWarnings("deprecation")
		BulkUpdateHandler buh = model.getGraph().getBulkUpdateHandler();

		OntModel ontModel = ModelFactory.createOntologyModel(OWL_MEM, model);
		return new BulkUpdatingOntModel(ontModel, buh);
	}

	public static Model createUnion(Model baseModel, Model otherModel) {
		@SuppressWarnings("deprecation")
		BulkUpdateHandler buh = baseModel.getGraph().getBulkUpdateHandler();

		Model unionModel = ModelFactory.createUnion(baseModel, otherModel);
		return new BulkUpdatingModel(unionModel, buh);
	}

	public static OntModel createUnion(OntModel baseModel, OntModel otherModel) {
		@SuppressWarnings("deprecation")
		BulkUpdateHandler buh = baseModel.getGraph().getBulkUpdateHandler();

		Model unionModel = createUnion((Model) baseModel, (Model) otherModel);
		OntModel unionOntModel = ModelFactory.createOntologyModel(OWL_MEM,
				unionModel);
		return new BulkUpdatingOntModel(unionOntModel, buh);
	}

	public static Model createModelForGraph(Graph g) {
		@SuppressWarnings("deprecation")
		BulkUpdateHandler buh = g.getBulkUpdateHandler();

		return new BulkUpdatingModel(ModelFactory.createModelForGraph(g), buh);
	}

}
