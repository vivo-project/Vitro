/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import org.apache.jena.graph.GraphMaker;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Extend this to add decorator functionality to a ModelMaker. The sub-class can
 * override specific methods as needed.
 */
public class AbstractModelMakerDecorator implements ModelMaker {
	private final ModelMaker inner;

	public AbstractModelMakerDecorator(ModelMaker inner) {
		if (inner == null) {
			throw new NullPointerException("'inner' may not be null.");
		}
		this.inner = inner;
	}
	
	@Override public String toString() {
		return this.getClass().getSimpleName() + "[inner="
				+ String.valueOf(inner) + "]"; 
	}

	@Override
	public Model createDefaultModel() {
		return inner.createDefaultModel();
	}

	@Override
	public Model createFreshModel() {
		return inner.createFreshModel();

	}

	@Override
	public Model openModel(String name) {
		return inner.openModel(name);
	}

	@Override
	public Model openModelIfPresent(String string) {
		return inner.openModelIfPresent(string);
	}

	@Override
	public Model getModel(String URL) {
		return inner.getModel(URL);
	}

	@Override
	public Model getModel(String URL, ModelReader loadIfAbsent) {
		return inner.getModel(URL, loadIfAbsent);
	}

	@Override
	public Model createModel(String name, boolean strict) {
		return inner.createModel(name, strict);
	}

	@Override
	public Model createModel(String name) {
		return inner.createModel(name);
	}

	@Override
	public Model openModel(String name, boolean strict) {
		return inner.openModel(name, strict);
	}

	@Override
	public void removeModel(String name) {
		inner.removeModel(name);
	}

	@Override
	public boolean hasModel(String name) {
		return inner.hasModel(name);
	}

	@Override
	public void close() {
		inner.close();
	}

	@Override
	public GraphMaker getGraphMaker() {
		return inner.getGraphMaker();
	}

	@Override
	public ExtendedIterator<String> listModels() {
		return inner.listModels();
	}

}
