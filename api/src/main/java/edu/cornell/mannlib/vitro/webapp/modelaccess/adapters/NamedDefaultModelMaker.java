/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * This model maker allows you to refer to the default model by a name.
 */
public class NamedDefaultModelMaker extends AbstractModelMakerDecorator {
	private static final Log log = LogFactory
			.getLog(NamedDefaultModelMaker.class);

	private final String defaultModelUri;

	public NamedDefaultModelMaker(ModelMaker inner, String defaultModelUri) {
		super(inner);
		this.defaultModelUri = defaultModelUri;
	}

	private boolean isDefaultModel(String name) {
		return name != null && name.equals(defaultModelUri);
	}

	@Override
	public Model openModel(String name) {
		if (isDefaultModel(name)) {
			return super.createDefaultModel();
		} else {
			return super.openModel(name);
		}
	}

	@Override
	public Model openModelIfPresent(String name) {
		if (isDefaultModel(name)) {
			return super.createDefaultModel();
		} else {
			return super.openModelIfPresent(name);
		}
	}

	@Override
	public Model getModel(String name) {
		if (isDefaultModel(name)) {
			return super.createDefaultModel();
		} else {
			return super.getModel(name);
		}
	}

	@Override
	public Model getModel(String name, ModelReader loadIfAbsent) {
		if (isDefaultModel(name)) {
			return super.createDefaultModel();
		} else {
			return super.getModel(name, loadIfAbsent);
		}
	}

	@Override
	public Model createModel(String name, boolean strict) {
		if (isDefaultModel(name)) {
			return super.createDefaultModel();
		} else {
			return super.createModel(name, strict);
		}
	}

	@Override
	public Model createModel(String name) {
		if (isDefaultModel(name)) {
			return super.createDefaultModel();
		} else {
			return super.createModel(name);
		}
	}

	@Override
	public Model openModel(String name, boolean strict) {
		if (isDefaultModel(name)) {
			return super.createDefaultModel();
		} else {
			return super.openModel(name, strict);
		}
	}

	@Override
	public void removeModel(String name) {
		if (isDefaultModel(name)) {
			log.warn("Attempting to remove the default model.");
		} else {
			super.removeModel(name);
		}

	}

	@Override
	public boolean hasModel(String name) {
		if (isDefaultModel(name)) {
			return true;
		} else {
			return super.hasModel(name);
		}
	}

	@Override
	public ExtendedIterator<String> listModels() {
		return super.listModels().andThen(
				Collections.singleton(this.defaultModelUri).iterator());
	}

}
