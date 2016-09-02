/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.Objects;

import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;

/**
 * An ObjectProperty that has some of its values overridden by a FauxProperty.
 * 
 * TODO This is a horrible kluge that should be discarded as soon as we can
 * rewrite GroupedPropertyList.
 */
public class FauxObjectPropertyWrapper extends ObjectProperty {
	private final ObjectProperty innerOP;
	private final FauxProperty faux;

	public FauxObjectPropertyWrapper(ObjectProperty inner, FauxProperty faux) {
		this.innerOP = inner;
		this.faux = faux;
	}

	// ----------------------------------------------------------------------
	// Methods where FauxProperty overrides.
	// ----------------------------------------------------------------------

	@Override
	public String getGroupURI() {
		String uri = faux.getGroupURI();
		if (uri != null) {
			return uri;
		}
		return innerOP.getGroupURI();
	}

	@Override
	public void setGroupURI(String groupUri) {
		faux.setGroupURI(groupUri);
		innerOP.setGroupURI(groupUri);
	}

	// -------

	@Override
	public String getDomainVClassURI() {
		String uri = faux.getDomainVClassURI();
		if (uri != null) {
			return uri;
		}
		return innerOP.getDomainVClassURI();
	}

	@Override
	public void setDomainVClassURI(String domainClassURI) {
		faux.setDomainURI(domainClassURI);
		innerOP.setDomainVClassURI(domainClassURI);
	}

	// -------

	@Override
	public String getRangeVClassURI() {
		String uri = faux.getRangeVClassURI();
		if (uri != null) {
			return uri;
		}
		return innerOP.getRangeVClassURI();
	}

	@Override
	public void setRangeVClassURI(String rangeClassURI) {
		faux.setRangeURI(rangeClassURI);
		innerOP.setRangeVClassURI(rangeClassURI);
	}

	// -------

	@Override
	public String getCustomEntryForm() {
		String s = faux.getCustomEntryForm();
		if (s != null) {
			return s;
		}
		return innerOP.getCustomEntryForm();
	}

	@Override
	public void setCustomEntryForm(String s) {
		faux.setCustomEntryForm(s);
		innerOP.setCustomEntryForm(s);
	}

	// -------

	@Override
	public boolean getSelectFromExisting() {
		return faux.isSelectFromExisting();
	}

	@Override
	public void setSelectFromExisting(boolean b) {
		faux.setSelectFromExisting(b);
		innerOP.setSelectFromExisting(b);
	}

	// -------

	@Override
	public void setOfferCreateNewOption(boolean b) {
		faux.setOfferCreateNewOption(b);
		innerOP.setOfferCreateNewOption(b);
	}

	@Override
	public boolean getOfferCreateNewOption() {
		return faux.isOfferCreateNewOption();
	}

	// -------

	@Override
	public void setCollateBySubclass(boolean collate) {
		faux.setCollateBySubclass(collate);
		innerOP.setCollateBySubclass(collate);
	}

	@Override
	public boolean getCollateBySubclass() {
		return faux.isCollateBySubclass();
	}

	// -------

	@Override
	public String getPublicDescription() {
		String s = faux.getPublicDescription();
		if (s != null) {
			return s;
		}
		return innerOP.getPublicDescription();
	}

	@Override
	public void setPublicDescription(String s) {
		faux.setPublicDescription(s);
		innerOP.setPublicDescription(s);
	}

	// -------

	@Override
	public String getPickListName() {
		String name = faux.getDisplayName();
		if (name != null) {
			return name;
		}
		return innerOP.getPickListName();
	}

	@Override
	public void setPickListName(String pickListName) {
		faux.setDisplayName(pickListName);
		innerOP.setPickListName(pickListName);
	}

	// ----------------------------------------------------------------------
	// Methods from ObjectProperty
	// ----------------------------------------------------------------------

	@Override
	public void xmlToSysOut() {
		innerOP.xmlToSysOut();
	}

	@Override
	public String getDomainEntityURI() {
		return innerOP.getDomainEntityURI();
	}

	@Override
	public void setDomainEntityURI(String domainEntityURI) {
		innerOP.setDomainEntityURI(domainEntityURI);
	}

	@Override
	public String getLabel() {
		return innerOP.getLabel();
	}

	@Override
	public String getDomainPublic() {
		return innerOP.getDomainPublic();
	}

	@Override
	public void setDomainPublic(String domainPublic) {
		innerOP.setDomainPublic(domainPublic);
	}

	@Override
	public VClass getDomainVClass() {
		return innerOP.getDomainVClass();
	}

	@Override
	public void setDomainVClass(VClass domainVClass) {
		innerOP.setDomainVClass(domainVClass);
	}

	@Override
	public String getParentURI() {
		return innerOP.getParentURI();
	}

	@Override
	public void setParentURI(String parentURI) {
		innerOP.setParentURI(parentURI);
	}

	@Override
	public String getRangeEntityURI() {
		return innerOP.getRangeEntityURI();
	}

	@Override
	public void setRangeEntityURI(String rangeEntityURI) {
		innerOP.setRangeEntityURI(rangeEntityURI);
	}

	@Override
	public String getRangePublic() {
		return innerOP.getRangePublic();
	}

	@Override
	public void setRangePublic(String rangePublic) {
		innerOP.setRangePublic(rangePublic);
	}

	@Override
	public VClass getRangeVClass() {
		return innerOP.getRangeVClass();
	}

	@Override
	public void setRangeVClass(VClass rangeVClass) {
		innerOP.setRangeVClass(rangeVClass);
	}

	@Override
	public String getDescription() {
		return innerOP.getDescription();
	}

	@Override
	public void setDescription(String description) {
		innerOP.setDescription(description);
	}

	@Override
	public String getExample() {
		return innerOP.getExample();
	}

	@Override
	public void setExample(String example) {
		innerOP.setExample(example);
	}

	@Override
	public List<ObjectPropertyStatement> getObjectPropertyStatements() {
		return innerOP.getObjectPropertyStatements();
	}

	@Override
	public void setObjectPropertyStatements(
			List<ObjectPropertyStatement> objectPropertyStatements) {
		innerOP.setObjectPropertyStatements(objectPropertyStatements);
	}

	@Override
	public String getURIInverse() {
		return innerOP.getURIInverse();
	}

	@Override
	public void setURIInverse(String URIInverse) {
		innerOP.setURIInverse(URIInverse);
	}

	@Override
	public String getNamespaceInverse() {
		return innerOP.getNamespaceInverse();
	}

	@Override
	public void setNamespaceInverse(String namespaceInverse) {
		innerOP.setNamespaceInverse(namespaceInverse);
	}

	@Override
	public String getLocalNameInverse() {
		return innerOP.getLocalNameInverse();
	}

	@Override
	public void setLocalNameInverse(String localNameInverse) {
		innerOP.setLocalNameInverse(localNameInverse);
	}

	@Override
	public boolean getTransitive() {
		return innerOP.getTransitive();
	}

	@Override
	public void setTransitive(boolean transitive) {
		innerOP.setTransitive(transitive);
	}

	@Override
	public boolean getSymmetric() {
		return innerOP.getSymmetric();
	}

	@Override
	public void setSymmetric(boolean symmetric) {
		innerOP.setSymmetric(symmetric);
	}

	@Override
	public boolean getFunctional() {
		return innerOP.getFunctional();
	}

	@Override
	public void setFunctional(boolean functional) {
		innerOP.setFunctional(functional);
	}

	@Override
	public boolean getInverseFunctional() {
		return innerOP.getInverseFunctional();
	}

	@Override
	public void setInverseFunctional(boolean inverseFunctional) {
		innerOP.setInverseFunctional(inverseFunctional);
	}

	@Override
	public int getDomainDisplayLimit() {
		return innerOP.getDomainDisplayLimit();
	}

	@Override
	public Integer getDomainDisplayLimitInteger() {
		return innerOP.getDomainDisplayLimitInteger();
	}

	@Override
	public void setDomainDisplayLimit(Integer domainDisplayLimit) {
		innerOP.setDomainDisplayLimit(domainDisplayLimit);
	}

	@Override
	public int getDomainDisplayTier() {
		return innerOP.getDomainDisplayTier();
	}

	@Override
	public Integer getDomainDisplayTierInteger() {
		return innerOP.getDomainDisplayTierInteger();
	}

	@Override
	public void setDomainDisplayTier(Integer domainDisplayTier) {
		innerOP.setDomainDisplayTier(domainDisplayTier);
	}

	@Override
	public String getDomainEntitySortDirection() {
		return innerOP.getDomainEntitySortDirection();
	}

	@Override
	public void setDomainEntitySortDirection(String domainEntitySortDirection) {
		innerOP.setDomainEntitySortDirection(domainEntitySortDirection);
	}

	@Override
	public String getObjectIndividualSortPropertyURI() {
		return innerOP.getObjectIndividualSortPropertyURI();
	}

	@Override
	public void setObjectIndividualSortPropertyURI(
			String objectIndividualSortPropertyURI) {
		innerOP.setObjectIndividualSortPropertyURI(objectIndividualSortPropertyURI);
	}

	@Override
	public int getRangeDisplayLimit() {
		return innerOP.getRangeDisplayLimit();
	}

	@Override
	public Integer getRangeDisplayLimitInteger() {
		return innerOP.getRangeDisplayLimitInteger();
	}

	@Override
	public void setRangeDisplayLimit(int rangeDisplayLimit) {
		innerOP.setRangeDisplayLimit(rangeDisplayLimit);
	}

	@Override
	public int getRangeDisplayTier() {
		return innerOP.getRangeDisplayTier();
	}

	@Override
	public Integer getRangeDisplayTierInteger() {
		return innerOP.getRangeDisplayTierInteger();
	}

	@Override
	public void setRangeDisplayTier(Integer rangeDisplayTier) {
		innerOP.setRangeDisplayTier(rangeDisplayTier);
	}

	@Override
	public String getRangeEntitySortDirection() {
		return innerOP.getRangeEntitySortDirection();
	}

	@Override
	public void setRangeEntitySortDirection(String rangeEntitySortDirection) {
		innerOP.setRangeEntitySortDirection(rangeEntitySortDirection);
	}

	@Override
	public boolean getStubObjectRelation() {
		return innerOP.getStubObjectRelation();
	}

	@Override
	public void setStubObjectRelation(boolean b) {
		innerOP.setStubObjectRelation(b);
	}

	@Override
	public int compareTo(ObjectProperty op) {
		return innerOP.compareTo(op);
	}

	@Override
	public ObjectProperty clone() {
		return innerOP.clone();
	}

	@Override
	public void setLabel(String label) {
		innerOP.setLabel(label);
	}

	@Override
	public boolean isSubjectSide() {
		return innerOP.isSubjectSide();
	}

	@Override
	public boolean isEditLinkSuppressed() {
		return innerOP.isEditLinkSuppressed();
	}

	@Override
	public boolean isAddLinkSuppressed() {
		return innerOP.isAddLinkSuppressed();
	}

	@Override
	public boolean isDeleteLinkSuppressed() {
		return innerOP.isDeleteLinkSuppressed();
	}

	@Override
	public void setEditLinkSuppressed(boolean editLinkSuppressed) {
		innerOP.setEditLinkSuppressed(editLinkSuppressed);
	}

	@Override
	public void setAddLinkSuppressed(boolean addLinkSuppressed) {
		innerOP.setAddLinkSuppressed(addLinkSuppressed);
	}

	@Override
	public void setDeleteLinkSuppressed(boolean deleteLinkSuppressed) {
		innerOP.setDeleteLinkSuppressed(deleteLinkSuppressed);
	}

	@Override
	public boolean isAnonymous() {
		return innerOP.isAnonymous();
	}

	@Override
	public String getURI() {
		return innerOP.getURI();
	}

	@Override
	public void setURI(String URI) {
		innerOP.setURI(URI);
	}

	@Override
	public String getNamespace() {
		return innerOP.getNamespace();
	}

	@Override
	public void setNamespace(String namespace) {
		innerOP.setNamespace(namespace);
	}

	@Override
	public String getLocalName() {
		return innerOP.getLocalName();
	}

	@Override
	public void setLocalName(String localName) {
		innerOP.setLocalName(localName);
	}

	@Override
	public String getLocalNameWithPrefix() {
		return innerOP.getLocalNameWithPrefix();
	}

	@Override
	public void setLocalNameWithPrefix(String prefixedLocalName) {
		innerOP.setLocalNameWithPrefix(prefixedLocalName);
	}

	@Override
	public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
		return innerOP.getHiddenFromDisplayBelowRoleLevel();
	}

	@Override
	public void setHiddenFromDisplayBelowRoleLevel(RoleLevel level) {
		innerOP.setHiddenFromDisplayBelowRoleLevel(level);
	}

	@Override
	public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) {
		innerOP.setHiddenFromDisplayBelowRoleLevelUsingRoleUri(roleUri);
	}

	@Override
	public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
		return innerOP.getProhibitedFromUpdateBelowRoleLevel();
	}

	@Override
	public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel level) {
		innerOP.setProhibitedFromUpdateBelowRoleLevel(level);
	}

	@Override
	public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) {
		innerOP.setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(roleUri);
	}

	@Override
	public RoleLevel getHiddenFromPublishBelowRoleLevel() {
		return innerOP.getHiddenFromPublishBelowRoleLevel();
	}

	@Override
	public void setHiddenFromPublishBelowRoleLevel(RoleLevel level) {
		innerOP.setHiddenFromPublishBelowRoleLevel(level);
	}

	@Override
	public void setHiddenFromPublishBelowRoleLevelUsingRoleUri(String roleUri) {
		innerOP.setHiddenFromPublishBelowRoleLevelUsingRoleUri(roleUri);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((faux == null) ? 0 : faux.hashCode());
		result = prime * result + ((innerOP == null) ? 0 : innerOP.hashCode());
		return Objects.hash(innerOP, faux);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FauxObjectPropertyWrapper that = (FauxObjectPropertyWrapper) obj;
		return Objects.equals(this.innerOP, that.innerOP)
				&& Objects.equals(this.faux, that.faux);
	}

    @Override
	public String toString() {
		return String.format("FauxObjectPropertyWrapper[ %s <==> %s | %s ==> %s ==> %s, statementCount=%d, group=%s, customEntryForm=%s ]",
				getDomainPublic(), getRangePublic(),
				localName(getDomainVClassURI()), localName(getURI()), localName(getRangeVClassURI()),
				(getObjectPropertyStatements() == null ? 0: getObjectPropertyStatements().size()),
				localName(getGroupURI()),
				simpleName(getCustomEntryForm()));
	}

	private Object simpleName(String classname) {
		if (classname == null) {
			return null;
		} else {
			return classname.substring(classname.lastIndexOf(".") + 1);
		}
	}

	private Object localName(String uri) {
		if (uri == null) {
			return null;
		} else {
			return ResourceFactory.createResource(uri).getLocalName();
		}
	}

}
