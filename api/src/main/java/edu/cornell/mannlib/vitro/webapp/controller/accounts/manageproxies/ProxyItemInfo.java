/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;

/**
 * An immutable collection of fields that will be displayed in a
 * ProxyRelationship.
 */
public class ProxyItemInfo {
	private final String uri;
	private final String label;
	private final String classLabel;
	private final String imageUrl;

	public ProxyItemInfo(String uri, String label, String classLabel,
			String imageUrl) {
		this.uri = uri;
		this.label = label;
		this.classLabel = classLabel;
		this.imageUrl = imageUrl;
	}

	public String getUri() {
		return uri;
	}

	public String getLabel() {
		return label;
	}

	public String getClassLabel() {
		return classLabel;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!o.getClass().equals(this.getClass())) {
			return false;
		}
		ProxyItemInfo that = (ProxyItemInfo) o;
		return equivalent(this.uri, that.uri)
				&& equivalent(this.label, that.label)
				&& equivalent(this.classLabel, that.classLabel)
				&& equivalent(this.imageUrl, that.imageUrl);
	}

	private boolean equivalent(Object o1, Object o2) {
		return (o1 == null) ? (o2 == null) : o1.equals(o2);
	}

	@Override
	public int hashCode() {
		return hash(this.uri) ^ hash(this.label) ^ hash(this.classLabel)
				^ hash(this.imageUrl);
	}

	private int hash(Object o) {
		return (o == null) ? 0 : o.hashCode();
	}

	@Override
	public String toString() {
		return "ProxyItemInfo[uri=" + uri + ", label=" + label
				+ ", classLabel=" + classLabel + ", imageUrl=" + imageUrl + "]";
	}

}
