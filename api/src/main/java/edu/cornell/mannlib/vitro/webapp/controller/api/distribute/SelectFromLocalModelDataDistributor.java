/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import static edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.createSelectQueryContext;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.modelbuilder.ModelBuilder;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner.SparqlQueryRunner.SelectQueryContext;

/**
 * Execute one or more ModelBuilders to build a local model. Execute a SELECT
 * query against that model.
 */
public class SelectFromLocalModelDataDistributor
		extends
			SparqlSelectDataDistributorBase {
	private static final Log log = LogFactory
			.getLog(SelectFromLocalModelDataDistributor.class);

	private String rawSelectQuery;
	private List<ModelBuilder> modelBuilders = new ArrayList<>();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#modelBuilder", minOccurs = 1)
	public void addModelBuilder(ModelBuilder builder) {
		modelBuilders.add(builder);
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#selectQuery", minOccurs = 1, maxOccurs = 1)
	public void setRawSelectQuery(String query) {
		rawSelectQuery = query;
	}

	@Override
	public void writeOutput(OutputStream output)
			throws DataDistributorException {
		Model localModel = runModelBuilders();
		runSelectQuery(output, localModel);
	}

	private Model runModelBuilders() throws DataDistributorException {
		Model localModel = ModelFactory.createDefaultModel();
		for (ModelBuilder modelBuilder : modelBuilders) {
			localModel.add(runModelBuilder(modelBuilder));
			log.debug("Model size is  " + localModel.size());
		}
		return localModel;
	}

	private Model runModelBuilder(ModelBuilder modelBuilder)
			throws DataDistributorException {
		modelBuilder.init(ddContext);
		Model model = modelBuilder.buildModel();
		modelBuilder.close();
		return model;
	}

	private void runSelectQuery(OutputStream output, Model localModel)
			throws MissingParametersException {
		SelectQueryContext queryContext = createSelectQueryContext(localModel,
				this.rawSelectQuery);
		queryContext = binder.bindUriParameters(uriBindingNames, queryContext);
		queryContext = binder.bindLiteralParameters(literalBindingNames,
				queryContext);

		log.debug("Query context is: " + queryContext);
		queryContext.execute().writeToOutput(output);
	}

	@Override
	public String toString() {
		return "SelectFromLocalModelDataDistributor [actionName=" + actionName
				+ ", rawSelectQuery=" + rawSelectQuery + ", modelBuilders="
				+ modelBuilders + ", uriBindingNames=" + uriBindingNames
				+ ", literalBindingNames=" + literalBindingNames
				+ ", parameters=" + parameters + "]";
	}

	@Override
	public void close() throws DataDistributorException {
		// Nothing to do.
	}

}
