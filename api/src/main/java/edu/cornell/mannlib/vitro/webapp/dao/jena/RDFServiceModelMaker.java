/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.graph.GraphMaker;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.CannotCreateException;
import org.apache.jena.shared.DoesNotExistException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import com.ibm.icu.text.Collator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

public class RDFServiceModelMaker implements ModelMaker {

	private final static Log log = LogFactory.getLog(RDFServiceModelMaker.class);

	private RDFService service;
	private RDFServiceDataset dataset;

	public RDFServiceModelMaker(RDFService service) {
		this.service = service;
		this.dataset = new RDFServiceDataset(service);
	}

	@Override
	public void close() {
		dataset.close();
		// service.close(); ?
	}

	@Override
	public Model createModel(String name) {
		Model model = getModel(name);
		if (model == null) {
			throw new CannotCreateException(name);
		} else {
			return model;
		}
	}

	@Override
	public Model createModel(String name, boolean strict) {
		if (this.hasModel(name) && strict) {
			throw new AlreadyExistsException(name);
		} else {
			return createModel(name);
		}
	}

	@Override
	public GraphMaker getGraphMaker() {
		throw new UnsupportedOperationException("GraphMaker not supported by "
				+ this.getClass().getName());
	}

	private Set<String> getModelNames() {
		try {
			@SuppressWarnings("unchecked")
			Set<String> names = new TreeSet<>(Collator.getInstance());
			names.addAll(service.getGraphURIs());
			return names;
		} catch (RDFServiceException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasModel(String name) {
		return getModelNames().contains(name);
	}

	@Override
	public ExtendedIterator<String> listModels() {
		return WrappedIterator.create(getModelNames().iterator());
	}

	@Override
	public Model openModel(String name, boolean strict) {
		if (strict && !this.hasModel(name)) {
			throw new DoesNotExistException(name);
		} else {
			return getModel(name);
		}
	}

	/**
	 * The contract says to disassociate the name while leaving the model
	 * undisturbed. However, you should then be able to create a new model with
	 * the same name, and that doesn't make any sense in this context.
	 */
	@Override
	public void removeModel(String name) {
		Model m = getModel(name);
		m.removeAll(null, null, null);
	}

	@Override
	public Model createDefaultModel() {
		return dataset.getDefaultModel();
	}

	@Override
	public Model createFreshModel() {
		throw new UnsupportedOperationException(
				"createFreshModel not supported by "
						+ this.getClass().getName());
	}

	@Override
	public Model openModel(String name) {
		Model m = getModel(name);
		if (m == null) {
			throw new DoesNotExistException(name);
		} else {
			return m;
		}
	}

	@Override
	public Model openModelIfPresent(String name) {
		if (this.hasModel(name)) {
			return getModel(name);
		} else {
			return null;
		}
	}

	@Override
	public Model getModel(String name, ModelReader loadIfNotAbsent) {
		Model m = getModel(name);
		if (m == null) {
			// Ignore the ModelReader. If the model is not present, give up.
			throw new CannotCreateException(name);
		} else {
			return m;
		}
	}

	/**
	 * Return a model from the RDFService. If the model does not exist, create
	 * an empty one.
	 */
	@Override
	public Model getModel(String name) {
		if (name == null) {
			return null;
		}
		return dataset.getNamedModel(name);
	}

	@Override
	public String toString() {
		return "RDFServiceModelMaker[service=" + service + "]";
	}

}
