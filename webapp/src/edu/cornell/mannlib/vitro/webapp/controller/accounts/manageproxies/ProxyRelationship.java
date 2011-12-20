/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An immutable relationship between Proxies and Profiles.
 * 
 * In most cases, this will either be between one Proxy and many Profiles (view
 * by Proxy), or between on Profile and many Proxies (view by Profile). However,
 * we can imagine it being a many-to-many relationship.
 */
public class ProxyRelationship {
	private final List<ProxyItemInfo> proxyInfos;
	private final List<ProxyItemInfo> profileInfos;

	public ProxyRelationship(List<ProxyItemInfo> proxyInfos,
			List<ProxyItemInfo> profileInfos) {
		this.proxyInfos = Collections
				.unmodifiableList(new ArrayList<ProxyItemInfo>(proxyInfos));
		this.profileInfos = Collections
				.unmodifiableList(new ArrayList<ProxyItemInfo>(profileInfos));
	}

	public List<ProxyItemInfo> getProxyInfos() {
		return proxyInfos;
	}

	public List<ProxyItemInfo> getProfileInfos() {
		return profileInfos;
	}

	@Override
	public String toString() {
		return "ProxyRelationship[proxyInfos=" + proxyInfos + ", profileInfos="
				+ profileInfos + "]";
	}

}
