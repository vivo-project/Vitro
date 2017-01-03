/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.org.apache.jena.rdf.model.ModelMaker;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.GraphMaker;
import org.apache.jena.graph.impl.SimpleGraphMaker;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.ModelReader;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.CannotCreateException;
import org.apache.jena.shared.DoesNotExistException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/**
 * A ModelMaker stub, but is it strict or relaxed? Choose one of the
 * sub-classes.
 * 
 * The only difference between strict and relaxed is on a call to
 * openModel(name), when no such model exists. The relaxed ModelMaker will
 * create a fresh model and associate it with the name. The strict modelMaker
 * will throw a DoesNotExistException.
 * 
 * Warning: the "fresh model" is the same every time, so calling
 * createFreshModel() more than once during a test will give illusory results.
 */
public abstract class ModelMakerStub implements ModelMaker {
	protected final Model defaultModel;
	protected final Model freshModel;
	protected final Map<String, Model> models = new HashMap<>();
	protected final GraphMaker graphMaker = new SimpleGraphMaker();

	// ----------------------------------------------------------------------
	// Factory methods
	// ----------------------------------------------------------------------

	public static ModelMakerStub rigorous(Model defaultModel, Model freshModel) {
		return new ModelMakerRigorousStub(defaultModel, freshModel);
	}

	public static ModelMakerStub relaxed(Model defaultModel, Model freshModel) {
		return new ModelMakerRelaxedStub(defaultModel, freshModel);
	}

	// ----------------------------------------------------------------------
	// The abstract class
	// ----------------------------------------------------------------------

	protected ModelMakerStub(Model defaultModel, Model freshModel) {
		this.defaultModel = defaultModel;
		this.freshModel = freshModel;
	}

	public ModelMakerStub put(String uri, Model model) {
		models.put(uri, model);
		return this;
	}

	@Override
	public GraphMaker getGraphMaker() {
		return graphMaker;
	}

	@Override
	public void close() {
		// Nothing to close.
	}

	@Override
	public boolean hasModel(String name) {
		return models.containsKey(name);
	}

	@Override
	public ExtendedIterator<String> listModels() {
		return WrappedIterator.create(models.keySet().iterator());
	}

	@Override
	public Model createModel(String name) {
		return createModel(name, false);
	}

	@Override
	public Model createModel(String name, boolean strict) {
		if (hasModel(name)) {
			if (strict) {
				throw new AlreadyExistsException(name);
			} else {
				return models.get(name);
			}
		}
		return freshModel;
	}

	@Override
	public Model createDefaultModel() {
		return defaultModel;
	}

	@Override
	public Model createFreshModel() {
		return freshModel;
	}

	@Override
	public Model openModel(String name, boolean strict) {
		if (strict && !hasModel(name)) {
			throw new DoesNotExistException(name);
		} else {
			return openModel(name);
		}
	}

	@Override
	public Model openModelIfPresent(String name) {
		return models.get(name);
	}

	@Override
	public void removeModel(String name) {
		if (hasModel(name)) {
			models.remove(name);
		} else {
			throw new DoesNotExistException(name);
		}
	}

	// ----------------------------------------------------------------------
	// Concrete sub-classes
	// ----------------------------------------------------------------------

	/**
	 * "Relaxed" means that if they ask for a model that doesn't exist, we
	 * create one.
	 * 
	 * Note: should return a new model, instead of the "fresh" model.
	 */
	private static class ModelMakerRelaxedStub extends ModelMakerStub {
		public ModelMakerRelaxedStub(Model defaultModel, Model freshModel) {
			super(defaultModel, freshModel);
		}

		@Override
		public Model openModel(String name) {
			if (hasModel(name)) {
				return models.get(name);
			} else {
				return freshModel;
			}
		}

		@Override
		public Model getModel(String name) {
			if (hasModel(name)) {
				return models.get(name);
			} else {
				return freshModel;
			}
		}

		/**
		 * TODO: Rather than having this part of "relaxed" or "rigorous", the
		 * result should depend on the ModelReader.
		 */
		@Override
		public Model getModel(String name, ModelReader loadIfAbsent) {
			if (hasModel(name)) {
				return models.get(name);
			} else {
				return freshModel;
			}
		}

	}

	/**
	 * "Rigorous" means that if they ask for a model that doesn't exist, we
	 * return null or throw an exception.
	 */
	private static class ModelMakerRigorousStub extends ModelMakerStub {
		public ModelMakerRigorousStub(Model defaultModel, Model freshModel) {
			super(defaultModel, freshModel);
		}

		@Override
		public Model openModel(String name) {
			if (hasModel(name)) {
				return models.get(name);
			} else {
				throw new DoesNotExistException(name);
			}
		}

		@Override
		public Model getModel(String name) {
			if (hasModel(name)) {
				return models.get(name);
			} else {
				return null;
			}
		}

		/**
		 * TODO: Rather than having this part of "relaxed" or "rigorous", the
		 * result should depend on the ModelReader.
		 */
		@Override
		public Model getModel(String name, ModelReader loadIfAbsent) {
			if (hasModel(name)) {
				return models.get(name);
			} else {
				throw new CannotCreateException(name);
			}
		}

	}

}
