/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.exclusions;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * Skip individual if its URI is from any of the excludeNamespaces.
 */
public class ExcludeBasedOnNamespace implements SearchIndexExcluder {

	private List<String> excludeNamespaces = new ArrayList<>();

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#excludes")
	public void addExcludedNamespace(String ns) {
		excludeNamespaces.add(ns);
	}

	@Override
	public String checkForExclusion(Individual ind) {
		for (String ns : excludeNamespaces) {
			if (ns.equals(ind.getNamespace())) {
				return "skipping because of namespace " + ns;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "ExcludeBasedOnNamespace[namespaces=" + excludeNamespaces + "]";
	}
}
