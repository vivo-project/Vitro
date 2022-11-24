/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.Objects;

import org.apache.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;

/**
 * An ObjectProperty that has some of its values overridden by a FauxProperty.
 *
 * TODO This is a horrible kluge that should be discarded as soon as we can
 * rewrite GroupedPropertyList.
 */
public class FauxDataPropertyWrapper extends DataProperty {
	private final DataProperty innerDP;
	private final FauxProperty faux;

	public FauxDataPropertyWrapper(DataProperty inner, FauxProperty faux) {
		this.innerDP = inner;
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
		return innerDP.getGroupURI();
	}

	@Override
	public void setGroupURI(String groupUri) {
		faux.setGroupURI(groupUri);
		innerDP.setGroupURI(groupUri);
	}

	// -------

	@Override
	public String getDomainVClassURI() {
		String uri = faux.getDomainVClassURI();
		if (uri != null) {
			return uri;
		}
		return innerDP.getDomainVClassURI();
	}

	@Override
	public void setDomainVClassURI(String domainClassURI) {
		faux.setDomainURI(domainClassURI);
		innerDP.setDomainVClassURI(domainClassURI);
	}

	// -------

	@Override
	public String getRangeVClassURI() {
		String uri = faux.getRangeVClassURI();
		if (uri != null) {
			return uri;
		}
		return innerDP.getRangeVClassURI();
	}

	@Override
	public void setRangeVClassURI(String rangeClassURI) {
		faux.setRangeURI(rangeClassURI);
		innerDP.setRangeVClassURI(rangeClassURI);
	}

	// -------

	@Override
	public String getCustomEntryForm() {
		String s = faux.getCustomEntryForm();
		if (s != null) {
			return s;
		}
		return innerDP.getCustomEntryForm();
	}

	@Override
	public void setCustomEntryForm(String s) {
		faux.setCustomEntryForm(s);
		innerDP.setCustomEntryForm(s);
	}

	// -------

	
	@Override
	public String getPublicDescription() {
		String s = faux.getPublicDescription();
		if (s != null) {
			return s;
		}
		return innerDP.getPublicDescription();
	}

	@Override
	public void setPublicDescription(String s) {
		faux.setPublicDescription(s);
		innerDP.setPublicDescription(s);
	}

	// -------

	@Override
	public String getPickListName() {
		String name = faux.getDisplayName();
		if (name != null) {
			return name;
		}
		return innerDP.getPickListName();
	}

	@Override
	public void setPickListName(String pickListName) {
		faux.setDisplayName(pickListName);
		innerDP.setPickListName(pickListName);
	}

	// ----------------------------------------------------------------------
	// Methods from ObjectProperty
	// ----------------------------------------------------------------------


	@Override
	public String getLabel() {
		return innerDP.getLabel();
	}

	@Override
	public String getDescription() {
		return innerDP.getDescription();
	}

	@Override
	public void setDescription(String description) {
		innerDP.setDescription(description);
	}

	@Override
	public String getExample() {
		return innerDP.getExample();
	}

	@Override
	public void setExample(String example) {
		innerDP.setExample(example);
	}

	

	@Override
	public boolean getFunctional() {
		return innerDP.getFunctional();
	}

	@Override
	public void setFunctional(boolean functional) {
		innerDP.setFunctional(functional);
	}

	@Override
	public void setLabel(String label) {
		innerDP.setLabel(label);
	}

	@Override
	public boolean isSubjectSide() {
		return innerDP.isSubjectSide();
	}

	@Override
	public boolean isEditLinkSuppressed() {
		return innerDP.isEditLinkSuppressed();
	}

	@Override
	public boolean isAddLinkSuppressed() {
		return innerDP.isAddLinkSuppressed();
	}

	@Override
	public boolean isDeleteLinkSuppressed() {
		return innerDP.isDeleteLinkSuppressed();
	}

	@Override
	public void setEditLinkSuppressed(boolean editLinkSuppressed) {
		innerDP.setEditLinkSuppressed(editLinkSuppressed);
	}

	@Override
	public void setAddLinkSuppressed(boolean addLinkSuppressed) {
		innerDP.setAddLinkSuppressed(addLinkSuppressed);
	}

	@Override
	public void setDeleteLinkSuppressed(boolean deleteLinkSuppressed) {
		innerDP.setDeleteLinkSuppressed(deleteLinkSuppressed);
	}

	@Override
	public boolean isAnonymous() {
		return innerDP.isAnonymous();
	}

	@Override
	public String getURI() {
		return innerDP.getURI();
	}

	@Override
	public void setURI(String URI) {
		innerDP.setURI(URI);
	}

	@Override
	public String getNamespace() {
		return innerDP.getNamespace();
	}

	@Override
	public void setNamespace(String namespace) {
		innerDP.setNamespace(namespace);
	}

	@Override
	public String getLocalName() {
		return innerDP.getLocalName();
	}

	@Override
	public void setLocalName(String localName) {
		innerDP.setLocalName(localName);
	}

	@Override
	public String getLocalNameWithPrefix() {
		return innerDP.getLocalNameWithPrefix();
	}

	@Override
	public void setLocalNameWithPrefix(String prefixedLocalName) {
		innerDP.setLocalNameWithPrefix(prefixedLocalName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((faux == null) ? 0 : faux.hashCode());
		result = prime * result + ((innerDP == null) ? 0 : innerDP.hashCode());
		return Objects.hash(innerDP, faux);
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
		FauxDataPropertyWrapper that = (FauxDataPropertyWrapper) obj;
		return Objects.equals(this.innerDP, that.innerDP)
				&& Objects.equals(this.faux, that.faux);
	}

	@Override
	public List<DataPropertyStatement> getDataPropertyStatements() {
		return innerDP.getDataPropertyStatements();
	}
	
    @Override
	public String toString() {
		return String.format("FauxDataPropertyWrapper[ %s ==> %s ==> %s, statementCount=%d, group=%s, customEntryForm=%s ]",
				localName(getDomainVClassURI()),
				localName(getURI()), 
				localName(getRangeVClassURI()),
				(getDataPropertyStatements() == null ? 0: getDataPropertyStatements().size()),
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
