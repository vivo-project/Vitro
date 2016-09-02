/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * Decorates an OntModelCache with some "virtual" OntModels, each of which is a
 * union of two actual OntModels.
 * 
 * For example, we might create a model called FULL_ASSERTIONS, that is the
 * union of two models called ABOX_ASSERTIONS and TBOX_ASSERTIONS.
 * 
 * The inner class UnionSpec holds the model names, and allows us to lazily
 * create the union models.
 */
public class UnionModelsOntModelsCache implements OntModelCache {
	private final OntModelCache inner;
	private final Map<String, UnionSpec> unionModelsMap;

	/**
	 * Create it like this:
	 * 
	 * <pre>
	 * new UnionModelsOntModelsCache(inner,
	 *     UnionSpec.base("baseUri").plus("plusUri").yields("unionUri"),
	 *     ...);
	 * </pre>
	 */
	public UnionModelsOntModelsCache(OntModelCache inner,
			UnionSpec... unionModelSpecs) {
		this.inner = inner;

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

		for (UnionSpec spec : unionModelsMap.values()) {
			if (unionModelsMap.containsKey(spec.getBaseUri())
					|| unionModelsMap.containsKey(spec.getPlusUri())) {
				throw new IllegalArgumentException(
						"A UnionSpec may not build on another UnionSpec: "
								+ spec);
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
	private OntModel getUnionModel(String name) {
		UnionSpec spec = unionModelsMap.get(name);
		synchronized (spec) {
			if (spec.getUnionModel() == null) {
				OntModel baseModel = inner.getOntModel(spec.getBaseUri());
				OntModel plusModel = inner.getOntModel(spec.getPlusUri());
				spec.setUnionModel(VitroModelFactory.createUnion(baseModel,
						plusModel));
			}
		}
		return spec.getUnionModel();
	}

	@Override
	public OntModel getOntModel(String name) {
		if (hasUnionModel(name)) {
			return getUnionModel(name);
		} else {
			return inner.getOntModel(name);
		}
	}

	@Override
	public SortedSet<String> getModelNames() {
		SortedSet<String> names = new TreeSet<>(inner.getModelNames());
		names.addAll(unionModelsMap.keySet());
		return names;
	}

	@Override
	public String toString() {
		return "UnionModelsOntModelsCache[" + ToString.hashHex(this)
				+ ", inner=" + inner + ", unionModelsMap=" + unionModelsMap
				+ "]";
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
		private OntModel unionModel;

		public UnionSpec(String baseUri, String plusUri, String unionUri) {
			this.baseUri = baseUri;
			this.plusUri = plusUri;
			this.unionUri = unionUri;
		}

		public OntModel getUnionModel() {
			return unionModel;
		}

		public void setUnionModel(OntModel unionModel) {
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
