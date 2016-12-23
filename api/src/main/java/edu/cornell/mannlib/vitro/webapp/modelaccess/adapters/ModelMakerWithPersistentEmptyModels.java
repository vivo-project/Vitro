/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * This ModelMaker will remember the URIs of models you create, even if you
 * don't add triples to them. Of course, when VIVO is shut down, this ModelMaker
 * forgets.
 * 
 * This is a useful decorator for some triple stores which do not remember empty
 * models. SDB, for example. With this decorator, a GUI can offer to let you
 * create a model, and then offer to let you add triples to it. Without this
 * decorator, you would need to do that in a single step.
 * 
 * If you are dealing with a triple store where listModels() is a costly
 * operation, you might be better off using a ListCachingModelMaker instead. The
 * drawback is that ListCachingModelMaker will not see models that were created
 * at a lower level; perhaps by RDFServiceDataset.getNamedModel().
 * 
 * The methods that create a model must add its name to the list. The methods
 * that remove a model must remove its name from the list.
 */
public class ModelMakerWithPersistentEmptyModels extends
		AbstractModelMakerDecorator {
	private final Set<String> modelNames = new HashSet<>();

	public ModelMakerWithPersistentEmptyModels(ModelMaker inner) {
		super(inner);
	}

	@Override
	public boolean hasModel(String name) {
		return modelNames.contains(name) || super.hasModel(name);
	}

	@Override
	public ExtendedIterator<String> listModels() {
		Set<String> allNames = new TreeSet<>(modelNames);
		allNames.addAll(super.listModels().toList());
		return WrappedIterator.create(allNames.iterator());
	}

	@Override
	public Model getModel(String URL) {
		Model m = super.getModel(URL);
		if (m != null) {
			addModelNameIfNeeded(URL);
		}
		return m;
	}

	@Override
	public Model getModel(String URL, ModelReader loadIfAbsent) {
		Model m = super.getModel(URL, loadIfAbsent);
		addModelNameIfNeeded(URL);
		return m;
	}

	@Override
	public Model createModel(String name, boolean strict) {
		Model m = super.createModel(name, strict);
		addModelNameIfNeeded(name);
		return m;
	}

	@Override
	public Model createModel(String name) {
		return createModel(name, false);
	}

	@Override
	public Model openModel(String name) {
		Model m = super.openModel(name);
		addModelNameIfNeeded(name);
		return m;
	}

	@Override
	public Model openModelIfPresent(String name) {
		Model m = super.openModelIfPresent(name);
		if (m != null) {
			addModelNameIfNeeded(name);
		}
		return m;
	}

	@Override
	public Model openModel(String name, boolean strict) {
		Model m = super.openModel(name, strict);
		addModelNameIfNeeded(name);
		return m;
	}

	@Override
	public void removeModel(String name) {
		super.removeModel(name);
		modelNames.remove(name);
	}

	private void addModelNameIfNeeded(String name) {
		if (!super.listModels().toList().contains(name)) {
			modelNames.add(name);
		}
	}
}
