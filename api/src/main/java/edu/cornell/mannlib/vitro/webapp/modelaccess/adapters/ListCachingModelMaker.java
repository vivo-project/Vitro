/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * This ModelMaker keeps a cached list of the available models.
 * 
 * The methods that create a model must add its name to the list. The methods
 * that remove a model must remove its name from the list.
 * 
 * This is a useful decorator for some ModelMakers where listModels() is a
 * costly operation. The drawback is that it will not see models that were
 * created at a lower level; perhaps by RDFServiceDataset.getNamedModel().
 * 
 * If listModels() is not costly, you might be better off with a
 * ModelMakerWithPersistentEmptyModels.
 */
public class ListCachingModelMaker extends AbstractModelMakerDecorator {
	private final Set<String> modelNames;

	public ListCachingModelMaker(ModelMaker inner) {
		super(inner);
		this.modelNames = new TreeSet<>(inner.listModels().toSet());
	}

	@Override
	public boolean hasModel(String name) {
		return modelNames.contains(name);

	}

	@Override
	public ExtendedIterator<String> listModels() {
		return WrappedIterator.create(modelNames.iterator());
	}

	@Override
	public Model getModel(String URL) {
		Model m = super.getModel(URL);
		if (m != null) {
			modelNames.add(URL);
		}
		return m;
	}

	@Override
	public Model getModel(String URL, ModelReader loadIfAbsent) {
		Model m = super.getModel(URL, loadIfAbsent);
		modelNames.add(URL);
		return m;
	}

	@Override
	public Model createModel(String name, boolean strict) {
		Model m = super.createModel(name, strict);
		modelNames.add(name);
		return m;
	}

	@Override
	public Model createModel(String name) {
		return createModel(name, false);
	}

	@Override
	public Model openModel(String name) {
		Model m = super.openModel(name);
		modelNames.add(name);
		return m;
	}

	@Override
	public Model openModelIfPresent(String name) {
		Model m = super.openModelIfPresent(name);
		if (m != null) {
			modelNames.add(name);
		}
		return m;
	}

	@Override
	public Model openModel(String name, boolean strict) {
		Model m = super.openModel(name, strict);
		modelNames.add(name);
		return m;
	}

	@Override
	public void removeModel(String name) {
		super.removeModel(name);
		modelNames.remove(name);
	}

}
