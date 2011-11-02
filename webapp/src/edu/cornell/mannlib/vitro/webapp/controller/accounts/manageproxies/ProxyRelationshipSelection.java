/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An immutable group of relationships (might be empty), with the criteria that
 * were used to select them.
 */

public class ProxyRelationshipSelection {
	private final ProxyRelationshipSelectionCriteria criteria;
	private final List<ProxyRelationship> proxyRelationships;
	private final int totalResultCount;

	public ProxyRelationshipSelection(
			ProxyRelationshipSelectionCriteria criteria,
			List<ProxyRelationship> proxyRelationships, int totalResultCount) {
		this.criteria = criteria;
		this.proxyRelationships = Collections
				.unmodifiableList(new ArrayList<ProxyRelationship>(
						proxyRelationships));
		this.totalResultCount = totalResultCount;
	}

	public ProxyRelationshipSelectionCriteria getCriteria() {
		return criteria;
	}

	public List<ProxyRelationship> getProxyRelationships() {
		return proxyRelationships;
	}

	public int getTotalResultCount() {
		return totalResultCount;
	}

	@Override
	public String toString() {
		return "ProxyRelationshipSelection[count=" + totalResultCount
				+ ", relationships=" + proxyRelationships + ", criteria="
				+ criteria + "]";
	}

}
