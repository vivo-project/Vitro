/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * When an OntModel is requested for the first time, get a model from the
 * ModelMaker, wrap it, and cache it for subsequent requests.
 */
public class ModelMakerOntModelCache implements OntModelCache {
	private final Map<String, OntModel> cache = new HashMap<>();
	private final ModelMaker mm;

	public ModelMakerOntModelCache(ModelMaker mm) {
		this.mm = mm;
	}

	@Override
	public OntModel getOntModel(String name) {
		synchronized (cache) {
			if (cache.containsKey(name)) {
				return cache.get(name);
			}
			return wrapAndCache(name);
		}
	}

	private OntModel wrapAndCache(String name) {
		Model m = mm.getModel(name);
		OntModel om = VitroModelFactory.createOntologyModel(m);
		cache.put(name, om);
		return om;
	}

	@Override
	public SortedSet<String> getModelNames() {
		return new TreeSet<>(mm.listModels().toList());
	}

	@Override
	public String toString() {
		return "ModelMakerOntModelCache[" + ToString.hashHex(this) + ", mm=" + mm
				+ "]";
	}

}
