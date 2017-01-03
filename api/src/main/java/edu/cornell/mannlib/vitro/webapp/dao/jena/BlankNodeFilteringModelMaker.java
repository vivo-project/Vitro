/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.shared.CannotCreateException;

import edu.cornell.mannlib.vitro.webapp.modelaccess.adapters.AbstractModelMakerDecorator;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;

/**
 * Still not sure why this is needed, but...
 * 
 * Let's assume that there are some model operations that are implemented by
 * multiple SPARQL queries against the RDFService. Those multiple queries might
 * return different values for the same blank node, so when the results of the
 * queries were combined, the relationships would be lost.
 * 
 * To avoid this, we assume that all of the statements involving blank nodes
 * will fit nicely into memory, and we fetch them all at once. After that, all
 * of our operations are against the union of the actual model minus blank nodes
 * and the memory model.
 * 
 * The models do retain the same ID for each blank node on successive
 * operations, so we can execute repeated queries and it will work fine.
 * 
 * Writing blank nodes is probably a different matter, unless unrelated to
 * existing blank nodes.
 */
public class BlankNodeFilteringModelMaker extends AbstractModelMakerDecorator {
	private static final Log log = LogFactory
			.getLog(BlankNodeFilteringModelMaker.class);

	private final RDFService rdfService;

	public BlankNodeFilteringModelMaker(RDFService rdfService, ModelMaker inner) {
		super(inner);
		this.rdfService = rdfService;
	}

	@Override
	public Model createModel(String name) {
		return wrapModelWithFilter(name, super.createModel(name));
	}

	@Override
	public Model createModel(String name, boolean strict) {
		return wrapModelWithFilter(name, super.createModel(name, strict));
	}

	@Override
	public Model openModel(String name) {
		return wrapModelWithFilter(name, super.openModel(name));
	}

	@Override
	public Model openModelIfPresent(String name) {
		return wrapModelWithFilter(name, super.openModelIfPresent(name));
	}

	@Override
	public Model getModel(String name) {
		return wrapModelWithFilter(name, super.getModel(name));
	}

	@Override
	public Model getModel(String name, ModelReader loadIfAbsent) {
		return wrapModelWithFilter(name, super.getModel(name, loadIfAbsent));
	}

	@Override
	public Model openModel(String name, boolean strict) {
		return wrapModelWithFilter(name, super.openModel(name, strict));
	}

	public Model wrapModelWithFilter(String name, Model model) {
		if (model == null) {
			return null;
		}

		String bnodeQuery = String.format("construct { ?s ?p ?o } \n" //
				+ "where {  \n" //
				+ "  graph <%s> { \n" //
				+ "    ?s ?p ?o \n "
				+ "    filter (isBlank(?s) || isBlank(?o)) \n" + "  } \n" //
				+ "}", name);

		Model bnodeModel = ModelFactory.createDefaultModel();
		long start = System.currentTimeMillis();
		try {
			bnodeModel.read(rdfService.sparqlConstructQuery(bnodeQuery,
					ModelSerializationFormat.N3), null, "N3");
			log.debug("constructed a model of blank nodes of size: "
					+ bnodeModel.size() + " for graph " + name);
		} catch (Exception e) {
			log.error("error trying to create a blank node model: ", e);
			throw new CannotCreateException(name);
		}

		long timeElapsedMillis = System.currentTimeMillis() - start;
		log.debug("msecs to find blank nodes for graph " + name + " "
				+ timeElapsedMillis);

		Graph bnodeFilteringGraph = new BlankNodeFilteringGraph(
				model.getGraph());
		Model bnodeFilteringModel = ModelFactory
				.createModelForGraph(bnodeFilteringGraph);

		Model specialUnionModel = VitroModelFactory.createUnion(
				bnodeFilteringModel, bnodeModel);
		bnodeFilteringModel
				.register(new BlankNodeStatementListener(bnodeModel));

		return specialUnionModel;
	}
}
