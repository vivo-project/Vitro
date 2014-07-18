/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

/**
 * Allow some models in the "shadowing" ModelMaker to hide the corresponding
 * models in the "shadowed" ModelMaker.
 * 
 * Specify both ModelMakers, and the list of URIs for the shadowing models.
 */
public class ShadowingModelMaker extends AbstractModelMakerDecorator {
	private final ModelMaker shadowing;
	private final Set<String> shadowUris;

	public ShadowingModelMaker(ModelMaker shadowed, ModelMaker shadowing,
			String... shadowUris) {
		super(shadowed);
		this.shadowing = shadowing;
		this.shadowUris = new HashSet<>(Arrays.asList(shadowUris));
	}

	private boolean isShadow(String name) {
		return shadowUris.contains(name);
	}

	@Override
	public Model createDefaultModel() {
		return super.createDefaultModel();
	}

	@Override
	public Model createFreshModel() {
		return super.createFreshModel();
	}

	@Override
	public Model openModel(String name) {
		if (isShadow(name)) {
			return shadowing.openModel(name);
		} else {
			return super.openModel(name);
		}
	}

	@Override
	public Model openModelIfPresent(String name) {
		if (isShadow(name)) {
			return shadowing.openModelIfPresent(name);
		} else {
			return super.openModelIfPresent(name);
		}
	}

	@Override
	public Model getModel(String name) {
		if (isShadow(name)) {
			return shadowing.getModel(name);
		} else {
			return super.getModel(name);
		}
	}

	@Override
	public Model getModel(String name, ModelReader loadIfAbsent) {
		if (isShadow(name)) {
			return shadowing.getModel(name, loadIfAbsent);
		} else {
			return super.getModel(name, loadIfAbsent);
		}
	}

	@Override
	public Model createModel(String name, boolean strict) {
		if (isShadow(name)) {
			return shadowing.createModel(name, strict);
		} else {
			return super.createModel(name, strict);
		}
	}

	@Override
	public Model createModel(String name) {
		if (isShadow(name)) {
			return shadowing.createModel(name);
		} else {
			return super.createModel(name);
		}
	}

	@Override
	public Model openModel(String name, boolean strict) {
		if (isShadow(name)) {
			return shadowing.openModel(name, strict);
		} else {
			return super.openModel(name, strict);
		}
	}

	@Override
	public void removeModel(String name) {
		if (isShadow(name)) {
			shadowing.removeModel(name);
		} else {
			super.removeModel(name);
		}
	}

	@Override
	public boolean hasModel(String name) {
		if (isShadow(name)) {
			return shadowing.hasModel(name);
		} else {
			return super.hasModel(name);
		}
	}

	@Override
	public void close() {
		shadowing.close();
		super.close();
	}

	@Override
	public ExtendedIterator<String> listModels() {
		Set<String> allNames = super.listModels().toSet();
		allNames.addAll(shadowUris);
		return WrappedIterator.create(allNames.iterator());
	}
}
