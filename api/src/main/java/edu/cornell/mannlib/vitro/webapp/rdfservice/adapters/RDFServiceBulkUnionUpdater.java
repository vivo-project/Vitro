/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SparqlGraph;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory.BulkUpdatingUnion;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;

public class RDFServiceBulkUnionUpdater extends AbstractBulkUpdater {

	private AbstractBulkUpdater leftUpdater;
	private AbstractBulkUpdater rightUpdater;
	private Model baseModel;
	private Model plusModel;

	public RDFServiceBulkUnionUpdater(Model baseModel, Model plusModel) {
		this.baseModel = baseModel;
		this.plusModel = plusModel;
		if (baseModel != null) {
			leftUpdater = getUpdater(baseModel);
		}
		if (plusModel != null) {
			rightUpdater = getUpdater(plusModel);
		}
	}
	
	public RDFServiceBulkUnionUpdater(BulkUpdatingUnion union) {
		baseModel = union.getBaseModel();
		if (baseModel != null) {
			leftUpdater = getUpdater(baseModel);
		}
		plusModel = union.getPlusModel();
		if (plusModel != null) {
			rightUpdater = getUpdater(plusModel);
		}
	}
	
	private AbstractBulkUpdater getUpdater(Model model) {
		AbstractBulkUpdater updater = null;
		if (model instanceof BulkUpdatingOntModel) {
			updater = ((BulkUpdatingOntModel) model).updater;
			return updater;
		} else if (model instanceof BulkUpdatingModel) {
			updater = ((BulkUpdatingModel) model).updater;
			return updater;
		}
		Graph graph = GraphUtils.unwrapUnionGraphs(model.getGraph());
		if (graph instanceof BulkUpdatingUnion) {
			updater = new RDFServiceBulkUnionUpdater((BulkUpdatingUnion) graph);
		} else if (graph instanceof RDFServiceGraph) {
			updater = new RDFServiceBulkUpdater((RDFServiceGraph) graph);
		} else if (graph instanceof BulkUpdatingUnion) {
			updater = new RDFServiceBulkUnionUpdater((BulkUpdatingUnion) graph);
		} else if (graph instanceof SparqlGraph) {
			updater = new SparqlBulkUpdater((SparqlGraph) graph);
		} else {
			updater = null;
		}
		return updater;
	}

	@Override
	protected void performAddModel(Model model) {
		if (leftUpdater != null) {
			leftUpdater.performAddModel(model);
		} else if (baseModel != null) {
			baseModel.remove(model);
		}
	}

	@Override
	protected void performRemoveModel(Model model) {
		if (leftUpdater != null) {
			leftUpdater.performRemoveModel(model);
		} else if (baseModel != null) {
			baseModel.remove(model);
		}
		if (rightUpdater != null) {
			rightUpdater.performRemoveModel(model);
		} else if (plusModel != null) {
			plusModel.remove(model);
		}
	}

	@Override
	protected void performRemoveAll() {
		if (leftUpdater != null) {
			leftUpdater.performRemoveAll();
		} else if (baseModel != null) {
			baseModel.removeAll();
		}
		if (rightUpdater != null) {
			rightUpdater.performRemoveAll();
		} else if (plusModel != null) {
			plusModel.removeAll();
		}
	}
}
