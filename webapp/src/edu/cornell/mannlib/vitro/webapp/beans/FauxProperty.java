/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;

import java.util.Objects;

/**
 * Represents a specialization on an ObjectProperty, only meaningful for
 * display.
 * 
 * Must have a baseURI and a rangeURI. Other fields are optional.
 */
public class FauxProperty extends BaseResourceBean implements ResourceBean {
	private final String rangeURI;
	private final String domainURI;

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
	 *            URI of the object class. May not be null.
	 */
	public FauxProperty(String domainURI, String baseURI, String rangeURI) {
		super(Objects.requireNonNull(baseURI, "baseURI may not be null"));
		this.rangeURI = Objects.requireNonNull(rangeURI,
				"rangeURI may not be null");
		this.domainURI = domainURI;
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
