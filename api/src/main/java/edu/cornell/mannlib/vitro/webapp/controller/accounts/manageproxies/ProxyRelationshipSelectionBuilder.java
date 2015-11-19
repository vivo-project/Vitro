/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import java.util.ArrayList;
import java.util.List;

/**
 * A mutable version of ProxyRelationshipSelection, that can be assembled
 * piecemeal as the info becomes available and then translated to an immutable
 * ProxyRelationshipSelection.
 * 
 * Uses mutable subclasses Relationship and ItemInfo.
 * 
 * ItemInfo contains a field for externalAuthId only because it is useful when
 * gathering the classLabel and imageUrl.
 */
public class ProxyRelationshipSelectionBuilder {
	final ProxyRelationshipSelectionCriteria criteria;

	final List<Relationship> relationships = new ArrayList<Relationship>();
	int count;

	public ProxyRelationshipSelectionBuilder(
			ProxyRelationshipSelectionCriteria criteria) {
		this.criteria = criteria;
	}

	public ProxyRelationshipSelection build() {
		List<ProxyRelationship> proxyRelationships = new ArrayList<ProxyRelationship>();
		for (Relationship r : relationships) {
			proxyRelationships.add(buildProxyRelationship(r));
		}
		return new ProxyRelationshipSelection(criteria, proxyRelationships,
				count);
	}

	private ProxyRelationship buildProxyRelationship(Relationship r) {
		List<ProxyItemInfo> proxyInfos = buildInfos(r.proxyInfos);
		List<ProxyItemInfo> profileInfos = buildInfos(r.profileInfos);
		return new ProxyRelationship(proxyInfos, profileInfos);
	}

	private List<ProxyItemInfo> buildInfos(List<ItemInfo> infos) {
		List<ProxyItemInfo> result = new ArrayList<ProxyItemInfo>();
		for (ItemInfo info : infos) {
			result.add(new ProxyItemInfo(info.uri, info.label, info.classLabel,
					info.imageUrl));
		}
		return result;
	}

	public static class Relationship {
		final List<ItemInfo> proxyInfos = new ArrayList<ItemInfo>();
		final List<ItemInfo> profileInfos = new ArrayList<ItemInfo>();
	}

	public static class ItemInfo {
		String uri = "";
		String label = "";
		String externalAuthId = "";
		String classLabel = "";
		String imageUrl = "";

		public ItemInfo() {
			// leave fields at default values.
		}

		public ItemInfo(String uri, String label, String externalAuthId,
				String classLabel, String imageUrl) {
			this.uri = uri;
			this.label = label;
			this.externalAuthId = externalAuthId;
			this.classLabel = classLabel;
			this.imageUrl = imageUrl;
		}

	}
}
