/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.shared.AlreadyExistsException;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.MemoryMappedModel;

/**
 * Provides fast read access to small models, by creating a "mapped" model in
 * memory.
 * 
 * When updates are detected on the "mapped" model, they are propagated to the
 * base model.
 */
public class MemoryMappingModelMaker extends AbstractModelMakerDecorator {
	private final Map<String, Model> mappedModels;

	public MemoryMappingModelMaker(ModelMaker inner,
			String... modelUrisForMapping) {
		super(inner);

		this.mappedModels = new HashMap<>();
		for (String name : modelUrisForMapping) {
			mappedModels.put(name, createMemoryMapping(name));
		}
	}

	private Model createMemoryMapping(String name) {
		return new MemoryMappedModel(super.openModel(name), name);
	}

	private boolean isMapped(String name) {
		return mappedModels.containsKey(name);
	}

	private Model getMapped(String name) {
		return mappedModels.get(name);
	}

	@Override
	public Model openModel(String name) {
		return isMapped(name) ? getMapped(name) : super.openModel(name);
	}

	@Override
	public Model openModelIfPresent(String name) {
		return isMapped(name) ? getMapped(name) : super
				.openModelIfPresent(name);
	}

	@Override
	public Model getModel(String name) {
		return isMapped(name) ? getMapped(name) : super.getModel(name);
	}

	@Override
	public Model getModel(String name, ModelReader loadIfAbsent) {
		return isMapped(name) ? getMapped(name) : super.getModel(name,
				loadIfAbsent);
	}

	@Override
	public Model createModel(String name, boolean strict) {
		if (isMapped(name)) {
			if (strict) {
				throw new AlreadyExistsException(name);
			} else {
				return getMapped(name);
			}
		} else {
			return super.createModel(name, strict);
		}
	}

	@Override
	public Model createModel(String name) {
		return isMapped(name) ? getMapped(name) : super.createModel(name);
	}

	@Override
	public Model openModel(String name, boolean strict) {
		return isMapped(name) ? getMapped(name) : super.openModel(name, strict);
	}

	@Override
	public void removeModel(String name) {
		if (isMapped(name)) {
			Model m = mappedModels.remove(name);
			m.close();
		}
		super.removeModel(name);
	}

}
