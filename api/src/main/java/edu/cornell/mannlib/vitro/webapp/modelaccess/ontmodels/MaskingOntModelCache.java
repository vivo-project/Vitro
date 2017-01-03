/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modelaccess.ontmodels;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

/**
 * The named models in the masking cache will be used in preference to a model
 * of the same name in the base cache.
 * 
 * If a named model doesn't exist in the masking cache, a warning is written to
 * the log and the name will be ignored.
 * 
 * New models are created in the base cache only.
 */
public class MaskingOntModelCache implements OntModelCache {
	private static final Log log = LogFactory
			.getLog(MaskingOntModelCache.class);

	private final OntModelCache baseCache;
	private final OntModelCache maskingCache;
	private final Set<String> maskingNames;

	public MaskingOntModelCache(OntModelCache baseCache,
			OntModelCache maskingCache, Collection<String> maskingNames) {
		this.baseCache = baseCache;
		this.maskingCache = maskingCache;
		this.maskingNames = new HashSet<>(maskingNames);
		checkForMissingNamedModels();
	}

	private void checkForMissingNamedModels() {
		Set<String> missingModelNames = new HashSet<>(this.maskingNames);
		missingModelNames.removeAll(maskingCache.getModelNames());
		if (!missingModelNames.isEmpty()) {
			log.warn("Specifed models do not exist in the masking cache: "
					+ missingModelNames);
			maskingNames.removeAll(missingModelNames);
		}
	}

	@Override
	public OntModel getOntModel(String name) {
		if (maskingNames.contains(name)) {
			return maskingCache.getOntModel(name);
		} else {
			return baseCache.getOntModel(name);
		}
	}

	/** The list of names in the baseCache may have changed. */
	@Override
	public SortedSet<String> getModelNames() {
		SortedSet<String> allNames = new TreeSet<>(baseCache.getModelNames());
		allNames.addAll(maskingNames);
		return allNames;
	}

	@Override
	public String toString() {
		return "MaskingOntModelCache[" + ToString.hashHex(this)
				+ ", baseCache=" + baseCache + ", maskingCache=" + maskingCache
				+ ", maskingNames=" + maskingNames + "]";
	}

}
