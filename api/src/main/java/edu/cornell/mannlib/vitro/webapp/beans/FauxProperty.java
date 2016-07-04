/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;

import java.util.Objects;

import edu.cornell.mannlib.vitro.webapp.auth.policy.bean.RoleRestrictedProperty;

/**
 * Represents a specialization on an ObjectProperty, only meaningful for
 * display.
 */
public class FauxProperty extends BaseResourceBean implements ResourceBean,
		RoleRestrictedProperty {
	// Must be null on insert. Must not be null on update. Ignored on delete.
	private String contextUri;
	// Must be null on insert. Must not be null on update. Ignored on delete.
	private String configUri;

	// Must not be null on insert or update. Partial identifier on delete.
	private String rangeURI;
	// May be null. Partial identifier on delete.
	private String domainURI;

	private String baseLabel;
	private String rangeLabel;
	private String domainLabel;

	private String groupURI;

	private String publicDescription;

	private int displayTier;
	private int displayLimit;

	private boolean collateBySubclass;
	private boolean selectFromExisting;
	private boolean offerCreateNewOption;

	private String customEntryForm;
	private String customListView;

	/**
	 * Arguments are in this order to mimic the relationship: subject ==&gt;
	 * property ==&gt; object
	 * 
	 * @param domainURI
	 *            URI of the subject class. May be null.
	 * @param baseURI
	 *            URI of the property. May not be null.
	 * @param rangeURI
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

	public String getContextUri() {
		return contextUri;
	}

	public void setContextUri(String contextUri) {
		this.contextUri = contextUri;
	}

	public String getConfigUri() {
		return configUri;
	}

	public void setConfigUri(String configUri) {
		this.configUri = configUri;
	}

	// BaseURI becomes an alias for URI
	public String getBaseURI() {
		return getURI();
	}

	public void setBaseURI(String baseURI) {
		setURI(baseURI);
	}

	public String getRangeURI() {
		return rangeURI;
	}

	public void setRangeURI(String rangeURI) {
		this.rangeURI = rangeURI;
	}

	public String getBaseLabel() {
		return (baseLabel == null) ? localName(getBaseURI()) : baseLabel;
	}

	public void setBaseLabel(String baseLabel) {
		this.baseLabel = baseLabel;
	}

	public String getRangeLabel() {
		return (rangeLabel == null) ? localName(rangeURI) : rangeLabel;
	}

	public void setRangeLabel(String rangeLabel) {
		this.rangeLabel = rangeLabel;
	}

	public String getDomainURI() {
		return domainURI;
	}

	public void setDomainURI(String domainURI) {
		this.domainURI = domainURI;
	}

	public String getDomainLabel() {
		return (domainLabel == null) ? (domainURI == null ? "null"
				: localName(domainURI)) : domainLabel;
	}

	public void setDomainLabel(String domainLabel) {
		this.domainLabel = domainLabel;
	}

	public String getGroupURI() {
		return groupURI;
	}

	public void setGroupURI(String groupURI) {
		this.groupURI = groupURI;
	}

	// DisplayName becomes an alias for PickListName
	public String getDisplayName() {
		return getPickListName();
	}

	public void setDisplayName(String displayName) {
		setPickListName(displayName);
	}

	public String getPublicDescription() {
		return publicDescription;
	}

	public void setPublicDescription(String publicDescription) {
		this.publicDescription = publicDescription;
	}

	public int getDisplayTier() {
		return displayTier;
	}

	public void setDisplayTier(int displayTier) {
		this.displayTier = displayTier;
	}

	public int getDisplayLimit() {
		return displayLimit;
	}

	public void setDisplayLimit(int displayLimit) {
		this.displayLimit = displayLimit;
	}

	public boolean isCollateBySubclass() {
		return collateBySubclass;
	}

	public void setCollateBySubclass(boolean collateBySubclass) {
		this.collateBySubclass = collateBySubclass;
	}

	public boolean isSelectFromExisting() {
		return selectFromExisting;
	}

	public void setSelectFromExisting(boolean selectFromExisting) {
		this.selectFromExisting = selectFromExisting;
	}

	public boolean isOfferCreateNewOption() {
		return offerCreateNewOption;
	}

	public void setOfferCreateNewOption(boolean offerCreateNewOption) {
		this.offerCreateNewOption = offerCreateNewOption;
	}

	public String getCustomEntryForm() {
		return customEntryForm;
	}

	public void setCustomEntryForm(String customEntryForm) {
		this.customEntryForm = customEntryForm;
	}

	public String getCustomListView() {
		return customListView;
	}

	public void setCustomListView(String customListView) {
		this.customListView = customListView;
	}

	private String localName(String uriString) {
		try {
			return createResource(uriString).getLocalName();
		} catch (Exception e) {
			return uriString;
		}
	}

	@Override
	public String toString() {
		return "FauxProperty[domainURI=" + domainURI + ", baseUri=" + getURI()
				+ ", baseLabel=" + baseLabel + ", rangeURI=" + rangeURI
				+ ", rangeLabel=" + rangeLabel + ", domainLabel=" + domainLabel
				+ ", pickListName=" + getPickListName() + ", contextUri="
				+ contextUri + ", configUri=" + configUri + ", groupURI="
				+ groupURI + "publicDescription=" + publicDescription
				+ ", displayTier=" + displayTier + ", displayLimit="
				+ displayLimit + ", collateBySubclass=" + collateBySubclass
				+ ", selectFromExisting=" + selectFromExisting
				+ ", offerCreateNewOption=" + offerCreateNewOption
				+ ", customEntryForm=" + customEntryForm + ", customListView="
				+ customListView + "]";
	}

	// ----------------------------------------------------------------------
	// Satisfy the RoleRestrictedProperty interface.
	// ----------------------------------------------------------------------

	@Override
	public String getDomainVClassURI() {
		return getDomainURI();
	}

	@Override
	public String getRangeVClassURI() {
		return getRangeURI();
	}
}
