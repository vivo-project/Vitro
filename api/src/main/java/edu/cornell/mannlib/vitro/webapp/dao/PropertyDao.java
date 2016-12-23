/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;
import java.util.Objects;

import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.RoleRestrictedProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

public interface PropertyDao {

	void addSuperproperty(Property property, Property superproperty);

	void addSuperproperty(String propertyURI, String superpropertyURI);

	void removeSuperproperty(Property property, Property superproperty);

	void removeSuperproperty(String propertyURI, String superpropertyURI);

	void addSubproperty(Property property, Property subproperty);

	void addSubproperty(String propertyURI, String subpropertyURI);

	void removeSubproperty(Property property, Property subproperty);

	void removeSubproperty(String propertyURI, String subpropertyURI);

	void addEquivalentProperty(String propertyURI, String equivalentPropertyURI);

	void addEquivalentProperty(Property property, Property equivalentProperty);

	void removeEquivalentProperty(String propertyURI,
			String equivalentPropertyURI);

	void removeEquivalentProperty(Property property, Property equivalentProperty);

	List<String> getSubPropertyURIs(String propertyURI);

	List<String> getAllSubPropertyURIs(String propertyURI);

	List<String> getSuperPropertyURIs(String propertyURI, boolean direct);

	List<String> getAllSuperPropertyURIs(String propertyURI);

	List<String> getEquivalentPropertyURIs(String propertyURI);

	List<VClass> getClassesWithRestrictionOnProperty(String propertyURI);

	/**
	 * An immutable key class for making maps of properties and faux properties.
	 * 
	 * The property URI is a significant part of the key, of course, but the
	 * range and domain URIs are significant also.
	 * 
	 * If the range or domain URI is not provided, it is assumed to be
	 * OWL:Thing.
	 */
	public static class FullPropertyKey {
		private static final String OWL_THING = OWL.Thing.getURI();

		private final String domainUri;
		private final String propertyUri;
		private final String rangeUri;

		private final int hash;
		private final String string;

		public FullPropertyKey(String uri) {
			this(OWL_THING, uri, OWL_THING);
		}

		public FullPropertyKey(Property p) {
			this(p.getDomainVClassURI(), p.getURI(), p.getRangeVClassURI());
		}

		public FullPropertyKey(RoleRestrictedProperty p) {
			this(p.getDomainVClassURI(), p.getURI(), p.getRangeVClassURI());
		}

		public FullPropertyKey(String domainUri, String propertyUri,
				String rangeUri) {
			this.propertyUri = Objects.requireNonNull(propertyUri,
					"propertyUri may not be null.");

			this.domainUri = (domainUri == null) ? OWL_THING : domainUri;
			this.rangeUri = (rangeUri == null) ? OWL_THING : rangeUri;

			this.hash = calculateHash();
			this.string = calculateString();
		}

		private int calculateHash() {
			return Objects
					.hash(this.domainUri, this.propertyUri, this.rangeUri);
		}

		private String calculateString() {
			return "FullPropertyKey[domainUri=" + this.domainUri
					+ ", propertyUri=" + this.propertyUri + ", rangeUri="
					+ this.rangeUri + "]";
		}

		public String getDomainUri() {
			return domainUri;
		}

		public String getPropertyUri() {
			return propertyUri;
		}

		public String getRangeUri() {
			return rangeUri;
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			FullPropertyKey that = (FullPropertyKey) obj;
			return Objects.equals(this.domainUri, that.domainUri)
					&& Objects.equals(this.propertyUri, that.propertyUri)
					&& Objects.equals(this.rangeUri, that.rangeUri);
		}

		@Override
		public String toString() {
			return string;
		}
	}
}
