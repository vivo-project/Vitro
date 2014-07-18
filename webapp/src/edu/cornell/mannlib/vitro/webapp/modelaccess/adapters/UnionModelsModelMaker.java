/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.adapters;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;

/**
 * This ModelMaker decorator creates one or more "union models" over the models
 * provided to it by the inner ModelMaker.
 * 
 * Each union model contains all of the triples of both its base model and its
 * "plus" model. Any changes to the union model are delegated to the base model.
 * If changes are desired in the "plus" model, it must be accessed directly.
 * 
 * This can create surprises, since the union model will claim to have a given
 * statement that is part of the plus model, but an attempt to delete that
 * statement from the union model has no effect.
 */
public class UnionModelsModelMaker extends AbstractModelMakerDecorator {
	private final Map<String, UnionSpec> unionModelsMap;

	/**
	 * Create it like this:
	 * 
	 * <pre>
	 * new UnionModelsModelMaker(inner,
	 *     UnionSpec.base("baseUri").plus("plusUri").yields("unionUri"),
	 *     ...);
	 * </pre>
	 */
	public UnionModelsModelMaker(ModelMaker inner, UnionSpec... unionModelSpecs) {
		super(inner);

		this.unionModelsMap = new HashMap<>();
		
		for (UnionSpec spec : unionModelSpecs) {
			String unionUri = spec.getUnionUri();
			if (unionModelsMap.containsKey(unionUri)) {
				throw new IllegalArgumentException(
						"Two UnionSpecs may not have the same union URI: "
								+ spec + ", " + unionModelsMap.get(unionUri));
			}
			this.unionModelsMap.put(unionUri, spec);
		}

		for (UnionSpec spec1 : unionModelsMap.values()) {
			if (unionModelsMap.containsKey(spec1.getBaseUri())
					|| unionModelsMap.containsKey(spec1.getPlusUri())) {
				throw new IllegalArgumentException(
						"A UnionSpec may not build on another UnionSpec: "
								+ spec1);
			}
		}
	}

	private boolean hasUnionModel(String name) {
		return unionModelsMap.containsKey(name);
	}

	/**
	 * The union models use lazy initialization, so there is no overhead if the
	 * model is never requested.
	 */
	private Model getUnionModel(String name) {
		UnionSpec spec = unionModelsMap.get(name);
		synchronized (spec) {
			if (spec.getUnionModel() == null) {
				Model baseModel = super.openModel(spec.getBaseUri());
				Model plusModel = super.openModel(spec.getPlusUri());
				spec.setUnionModel(VitroModelFactory.createUnion(baseModel,
						plusModel));
			}
		}
		return spec.getUnionModel();
	}

	// ----------------------------------------------------------------------
	// Overridden methods.
	// ----------------------------------------------------------------------

	@Override
	public Model createModel(String name) {
		return createModel(name, false);
	}

	@Override
	public Model createModel(String name, boolean strict) {
		if (hasUnionModel(name)) {
			if (strict) {
				throw new AlreadyExistsException(name);
			} else {
				return getUnionModel(name);
			}
		} else {
			return super.createModel(name, strict);
		}
	}

	@Override
	public Model openModel(String name, boolean strict) {
		if (hasUnionModel(name)) {
			return getUnionModel(name);
		} else {
			return super.openModel(name, strict);
		}
	}

	@Override
	public Model openModel(String name) {
		if (hasUnionModel(name)) {
			return getUnionModel(name);
		} else {
			return super.openModel(name);
		}
	}

	@Override
	public Model openModelIfPresent(String name) {
		if (hasUnionModel(name)) {
			return getUnionModel(name);
		} else {
			return super.openModelIfPresent(name);
		}
	}

	@Override
	public boolean hasModel(String name) {
		if (hasUnionModel(name)) {
			return true;
		} else {
			return super.hasModel(name);
		}
	}

	@Override
	public ExtendedIterator<String> listModels() {
		return super.listModels().andThen(unionModelsMap
				.keySet().iterator());
	}

	@Override
	public void removeModel(String name) {
		if (hasUnionModel(name)) {
			unionModelsMap.remove(name);
		} else {
			super.removeModel(name);
		}
	}

	@Override
	public Model getModel(String URL) {
		if (hasUnionModel(URL)) {
			return getUnionModel(URL);
		} else {
			return super.getModel(URL);
		}
	}

	@Override
	public Model getModel(String URL, ModelReader loadIfAbsent) {
		if (hasUnionModel(URL)) {
			return getUnionModel(URL);
		} else {
			return super.getModel(URL, loadIfAbsent);
		}
	}

	// ----------------------------------------------------------------------
	// UnionSpec and builder classes.
	// ----------------------------------------------------------------------

	public static class UnionSpec {
		public static UnionSpecBase base(String baseUri) {
			return new UnionSpecBase(baseUri);
		}

		private final String baseUri;
		private final String plusUri;
		private final String unionUri;
		private Model unionModel;

		public UnionSpec(String baseUri, String plusUri, String unionUri) {
			this.baseUri = baseUri;
			this.plusUri = plusUri;
			this.unionUri = unionUri;
		}

		public Model getUnionModel() {
			return unionModel;
		}

		public void setUnionModel(Model unionModel) {
			this.unionModel = unionModel;
		}

		public String getBaseUri() {
			return baseUri;
		}

		public String getPlusUri() {
			return plusUri;
		}

		public String getUnionUri() {
			return unionUri;
		}

		@Override
		public String toString() {
			return "UnionSpec[baseUri=" + baseUri + ", plusUri=" + plusUri
					+ ", unionUri=" + unionUri + "]";
		}
	}

	public static class UnionSpecBase {
		private final String baseUri;

		UnionSpecBase(String baseUri) {
			this.baseUri = baseUri;
		}

		public UnionSpecPair plus(String plusUri) {
			return new UnionSpecPair(baseUri, plusUri);
		}
	}

	public static class UnionSpecPair {
		private final String baseUri;
		private final String plusUri;

		public UnionSpecPair(String baseUri, String plusUri) {
			this.baseUri = baseUri;
			this.plusUri = plusUri;
		}

		public UnionSpec yields(String unionUri) {
			return new UnionSpec(baseUri, plusUri, unionUri);
		}
	}
}
