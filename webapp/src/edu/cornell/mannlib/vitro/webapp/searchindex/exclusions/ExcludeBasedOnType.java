/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.exclusions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Exclude individual from search index if it is a member of any of the
 * excluding types.
 */
public class ExcludeBasedOnType implements SearchIndexExcluder {

	private static final String SKIP_MSG = "skipping due to type: ";

	private final Set<String> typeURIs = new HashSet<>();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#excludes")
	public void addTypeURI(String uri) {
		typeURIs.add(uri);
	}

	@Override
	public String checkForExclusion(Individual ind) {
		if (ind == null) {
			return DONT_EXCLUDE;
		}

		List<VClass> vclasses = new ArrayList<>();
		addToList(vclasses, ind.getVClasses());

		for (VClass vclz : vclasses) {
			if (typeURIinExcludeList(vclz))
				return SKIP_MSG + vclz;
		}

		return DONT_EXCLUDE;
	}

	private void addToList(List<VClass> list, List<VClass> additions) {
		if (additions != null) {
			list.addAll(additions);
		}
	}

	protected boolean typeURIinExcludeList(VClass vclz) {
		if (vclz != null && vclz.getURI() != null && !vclz.isAnonymous()) {
			synchronized (typeURIs) {
				return typeURIs.contains(vclz.getURI());
			}
		} else {
			return false;
		}
	}

	protected void setExcludedTypes(List<String> typeURIs) {
		synchronized (typeURIs) {
			this.typeURIs.clear();
			this.typeURIs.addAll(typeURIs);
		}
	}

	protected void addTypeToExclude(String typeURI) {
		if (typeURI != null && !typeURI.isEmpty()) {
			synchronized (typeURIs) {
				typeURIs.add(typeURI);
			}
		}
	}

	protected void removeTypeToExclude(String typeURI) {
		synchronized (typeURIs) {
			typeURIs.remove(typeURI);
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[typeURIs=" + typeURIs + "]";
	}
}
