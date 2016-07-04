/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.controller.api.distribute.modelbuilder.ModelBuilder;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Execute one or more ModelBuilders, merge the results, and write them out as
 * Turtle RDF.
 */
public class RdfGraphDistributor extends DataDistributorBase {
	private static final Log log = LogFactory.getLog(RdfGraphDistributor.class);

	private List<ModelBuilder> modelBuilders = new ArrayList<>();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#modelBuilder", minOccurs = 1)
	public void addModelBuilder(ModelBuilder builder) {
		modelBuilders.add(builder);
	}

	@Override
	public String getContentType() throws DataDistributorException {
		return "text/turtle";
	}

	@Override
	public void writeOutput(OutputStream output)
			throws DataDistributorException {
		runModelBuilders().write(output, "TTL");
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

	@Override
	public void close() throws DataDistributorException {
		// Nothing to close.
	}

}
