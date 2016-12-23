/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * Use two OntModelCaches as one.
 * 
 * If both caches contain models with the same name, a warning will be written
 * to the log, and the model from the primary cache will be used.
 * 
 * Any new models will be created on the primary cache.
 */
public class JoinedOntModelCache implements OntModelCache {
	private static final Log log = LogFactory.getLog(JoinedOntModelCache.class);

	private final OntModelCache primary;
	private final OntModelCache secondary;

	public JoinedOntModelCache(OntModelCache primary, OntModelCache secondary) {
		this.primary = primary;
		this.secondary = secondary;

		Set<String> duplicateNames = new HashSet<>(primary.getModelNames());
		duplicateNames.retainAll(secondary.getModelNames());
		if (!duplicateNames.isEmpty()) {
			log.warn("These model names appear in both caches: "
					+ duplicateNames);
		}
	}

	@Override
	public OntModel getOntModel(String name) {
		if (primary.getModelNames().contains(name)) {
			return primary.getOntModel(name);
		}
		if (secondary.getModelNames().contains(name)) {
			return secondary.getOntModel(name);
		}
		return primary.getOntModel(name);
	}

	@Override
	public SortedSet<String> getModelNames() {
		SortedSet<String> allNames = new TreeSet<>(primary.getModelNames());
		allNames.addAll(secondary.getModelNames());
		return allNames;
	}

	@Override
	public String toString() {
		return "JoinedOntModelCache[" + ToString.hashHex(this) + ", primary="
				+ primary + ", secondary=" + secondary + "]";
	}

}
