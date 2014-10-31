/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;

import java.util.Objects;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.RoleRestrictedProperty;

/**
 * Represents a specialization on an ObjectProperty, only meaningful for
 * display.
 * 
 * BaseURI is required, may not be null, and may not be modified.
 * 
 * It would be nice to place the same restrictions on rangeURI, but it may be
 * null when the FauxProperty is being created, and it may be modified. The DAO
 * will need to check rangeURI for validity before accepting an insert or
 * modification.
 * 
 * TODO Can we do this more cleanly? Can handle this as two classes FauxProperty
 * and NewFauxProperty, and have each class enforce its own internal
 * constraints? For example, the range must not be null, must represent a valid
 * class, and must be equal to or a subclass of the range of the base property.
 */
public class FauxProperty extends BaseResourceBean implements ResourceBean, RoleRestrictedProperty {
	private String rangeURI;
	private String domainURI;

	private String rangeLabel;
	private String domainLabel;

	/**
	 * Arguments are in this order to mimic the relationship: subject ==>
	 * property ==> object
	 * 
	 * @param domainURI
	 *            URI of the subject class. May be null.
	 * @param baseURI
	 *            URI of the property. May not be null.
	 * @param rangeUri
	 *            URI of the object class. May be null.
	 */
	public FauxProperty(String domainURI, String baseURI, String rangeURI) {
		super(Objects.requireNonNull(baseURI, "baseURI may not be null"));
		this.rangeURI = rangeURI;
		this.domainURI = domainURI;
	}
	
	public FauxProperty() {
		// This is required by OperationUtils.cloneBean()
	}

	public void setRangeURI(String rangeURI) {
		this.rangeURI = rangeURI;
	}

	public String getRangeURI() {
		return rangeURI;
	}

	public void setRangeLabel(String rangeLabel) {
		this.rangeLabel = rangeLabel;
	}

	public String getRangeLabel() {
		return (rangeLabel == null) ? localName(rangeURI) : rangeLabel;
	}

	public void setDomainURI(String domainURI) {
		this.domainURI = domainURI;
	}
	
	public String getDomainURI() {
		return domainURI;
	}

	public void setDomainLabel(String domainLabel) {
		this.domainLabel = domainLabel;
	}

	public String getDomainLabel() {
		return (domainLabel == null) ? (domainURI == null ? "null"
				: localName(domainURI)) : domainLabel;
	}
	
	private String localName(String uriString) {
		try {
			return createResource(uriString).getLocalName();
		} catch (Exception e) {
			return uriString;
		}
	}

}
