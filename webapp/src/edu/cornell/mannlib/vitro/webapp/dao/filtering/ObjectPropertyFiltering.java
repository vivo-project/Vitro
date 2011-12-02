/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.LinkedList;
import java.util.List;

import net.sf.jga.algorithms.Filter;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

public class ObjectPropertyFiltering extends ObjectProperty {

    private VitroFilters filters;
    private ObjectProperty innerObjectProperty;
    
    public ObjectPropertyFiltering(ObjectProperty innerObjectProperty, VitroFilters filters){
        this.innerObjectProperty = innerObjectProperty;
        this.filters = filters;
    }
    
    /**
     * Need to filter ObjectPropertyStatements and return ObjectPropertyStatements
     * wrapped in ObjectPropertyStatementsFiltering. 
     */
    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements() {        
        List<ObjectPropertyStatement> propStmts =  innerObjectProperty.getObjectPropertyStatements();
        if( propStmts == null ) return null;
        
        List<ObjectPropertyStatement> fileredStmts = new LinkedList<ObjectPropertyStatement>();
        Filter.filter(propStmts, filters.getObjectPropertyStatementFilter(), fileredStmts);
        
        List<ObjectPropertyStatement> filteredOnSubjStmts = new LinkedList<ObjectPropertyStatement>();
        for( ObjectPropertyStatement stmt : fileredStmts){
            //uncomment condition to get hard edge portals:
            //if( filters.getIndividualFilter().fn( stmt.getObject()) )
                filteredOnSubjStmts.add( new ObjectPropertyStatementFiltering(stmt, filters) );
        }
        
        return filteredOnSubjStmts;        
    }
               
    /* the rest of the methods are delegated with no filtering */
    @Override
    public int compareTo(ObjectProperty op) {
        return innerObjectProperty.compareTo(op);
    }

    @Override
    public boolean equals(Object obj) {
        return innerObjectProperty.equals(obj);
    }

    @Override
    public String getCustomEntryForm() {
        return innerObjectProperty.getCustomEntryForm();
    }

    @Override
    public String getDescription() {
        return innerObjectProperty.getDescription();
    }

    @Override
    public int getDomainDisplayLimit() {
        return innerObjectProperty.getDomainDisplayLimit();
    }

    @Override
    public Integer getDomainDisplayLimitInteger() {
        return innerObjectProperty.getDomainDisplayLimitInteger();
    }
    
    @Override
    public int getDomainDisplayTier() {
        return innerObjectProperty.getDomainDisplayTier();
    }

    @Override
    public Integer getDomainDisplayTierInteger() {
        return innerObjectProperty.getDomainDisplayTierInteger();
    }
    
    @Override
    public String getDomainEntitySortDirection() {
        return innerObjectProperty.getDomainEntitySortDirection();
    }

    @Override
    public String getDomainEntityURI() {
        return innerObjectProperty.getDomainEntityURI();
    }    

    @Override
    public String getDomainPublic() {
        return innerObjectProperty.getDomainPublic();
    }
    
    @Override
    public VClass getDomainVClass() {
        return innerObjectProperty.getDomainVClass();
    }

    @Override
    public String getDomainVClassURI() {
        return innerObjectProperty.getDomainVClassURI();
    }

    @Override
    public String getLabel() {
        return innerObjectProperty.getLabel();
    }

    @Override
    public String getExample() {
        return innerObjectProperty.getExample();
    }

    @Override
    public boolean getFunctional() {
        return innerObjectProperty.getFunctional();
    }

    @Override
    public String getGroupURI() {
        return innerObjectProperty.getGroupURI();
    }

    @Override
    public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
        return innerObjectProperty.getHiddenFromDisplayBelowRoleLevel();
    }
    
    @Override
    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        return innerObjectProperty.getProhibitedFromUpdateBelowRoleLevel();
    }

    @Override
    public boolean getInverseFunctional() {
        return innerObjectProperty.getInverseFunctional();
    }

    @Override
    public String getLocalName() {
        return innerObjectProperty.getLocalName();
    }

    @Override
    public String getLocalNameInverse() {
        return innerObjectProperty.getLocalNameInverse();
    }

    @Override
    public String getLocalNameWithPrefix() {
        return innerObjectProperty.getLocalNameWithPrefix();
    }
    
    @Override
    public String getPickListName() {
        return innerObjectProperty.getPickListName();
    }

    @Override
    public String getNamespace() {
        return innerObjectProperty.getNamespace();
    }

    @Override
    public String getNamespaceInverse() {
        return innerObjectProperty.getNamespaceInverse();
    }

    @Override
    public String getObjectIndividualSortPropertyURI() {
        return innerObjectProperty.getObjectIndividualSortPropertyURI();
    }



    @Override
    public boolean getOfferCreateNewOption() {
        return innerObjectProperty.getOfferCreateNewOption();
    }

    @Override
    public String getParentURI() {
        return innerObjectProperty.getParentURI();
    }

    @Override
    public String getPublicDescription() {
        return innerObjectProperty.getPublicDescription();
    }

    @Override
    public int getRangeDisplayLimit() {
        return innerObjectProperty.getRangeDisplayLimit();
    }

    @Override
    public Integer getRangeDisplayLimitInteger() {
        return innerObjectProperty.getRangeDisplayLimitInteger();
    }
    
    @Override
    public int getRangeDisplayTier() {
        return innerObjectProperty.getRangeDisplayTier();
    }

    @Override
    public Integer getRangeDisplayTierInteger() {
        return innerObjectProperty.getRangeDisplayTierInteger();
    }
    
    @Override
    public String getRangeEntitySortDirection() {
        return innerObjectProperty.getRangeEntitySortDirection();
    }

    @Override
    public String getRangeEntityURI() {
        return innerObjectProperty.getRangeEntityURI();
    }

    @Override
    public String getRangePublic() {
        return innerObjectProperty.getRangePublic();
    }

    @Override
    public VClass getRangeVClass() {
        return innerObjectProperty.getRangeVClass();
    }

    @Override
    public String getRangeVClassURI() {
        return innerObjectProperty.getRangeVClassURI();
    }

    @Override
    public boolean getSelectFromExisting() {
        return innerObjectProperty.getSelectFromExisting();
    }

    @Override
    public boolean getSymmetric() {
        return innerObjectProperty.getSymmetric();
    }

    @Override
    public boolean getTransitive() {
        return innerObjectProperty.getTransitive();
    }

    @Override
    public String getURI() {
        return innerObjectProperty.getURI();
    }

    @Override
    public String getURIInverse() {
        return innerObjectProperty.getURIInverse();
    }

    @Override
    public int hashCode() {
        return innerObjectProperty.hashCode();
    }

    @Override
    public boolean isAnonymous() {
        return innerObjectProperty.isAnonymous();
    }

    @Override
    public boolean isSubjectSide() {
        return innerObjectProperty.isSubjectSide();
    }

    @Override
    public void setCustomEntryForm(String s) {
        innerObjectProperty.setCustomEntryForm(s);
    }

    @Override
    public void setDescription(String description) {
        innerObjectProperty.setDescription(description);
    }

    @Override
    public void setDomainDisplayLimit(Integer domainDisplayLimit) {
        innerObjectProperty.setDomainDisplayLimit(domainDisplayLimit);
    }

    @Override
    public void setDomainDisplayTier(Integer domainDisplayTier) {
        innerObjectProperty.setDomainDisplayTier(domainDisplayTier);
    }

    @Override
    public void setDomainEntitySortDirection(String domainEntitySortDirection) {
        innerObjectProperty
                .setDomainEntitySortDirection(domainEntitySortDirection);
    }

    @Override
    public void setDomainEntityURI(String domainEntityURI) {
        innerObjectProperty.setDomainEntityURI(domainEntityURI);
    }

    @Override
    public void setDomainPublic(String domainPublic) {
        innerObjectProperty.setDomainPublic(domainPublic);
    }

    @Override
    public void setDomainVClass(VClass domainVClass) {
        innerObjectProperty.setDomainVClass(domainVClass);
    }

    @Override
    public void setDomainVClassURI(String domainClassURI) {
        innerObjectProperty.setDomainVClassURI(domainClassURI);
    }

    @Override
    public void setLabel(String label) {
        innerObjectProperty.setLabel(label);
    }

    @Override
    public void setExample(String example) {
        innerObjectProperty.setExample(example);
    }

    @Override
    public void setFunctional(boolean functional) {
        innerObjectProperty.setFunctional(functional);
    }

    @Override
    public void setGroupURI(String in) {
        innerObjectProperty.setGroupURI(in);
    }

    @Override
    public void setHiddenFromDisplayBelowRoleLevel(RoleLevel eR) {
        innerObjectProperty.setHiddenFromDisplayBelowRoleLevel(eR);
    }
    
    @Override
    public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) {
        innerObjectProperty.setHiddenFromDisplayBelowRoleLevel(BaseResourceBean.RoleLevel.getRoleByUri(roleUri));
    }

    @Override
    public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel eR) {
        innerObjectProperty.setProhibitedFromUpdateBelowRoleLevel(eR);
    }
    
    @Override
    public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) {
        innerObjectProperty.setProhibitedFromUpdateBelowRoleLevel(BaseResourceBean.RoleLevel.getRoleByUri(roleUri));
    }

    @Override
    public void setInverseFunctional(boolean inverseFunctional) {
        innerObjectProperty.setInverseFunctional(inverseFunctional);
    }

    @Override
    public void setLocalName(String localName) {
        innerObjectProperty.setLocalName(localName);
    }
    
    @Override
    public void setLocalNameInverse(String localNameInverse) {
        innerObjectProperty.setLocalNameInverse(localNameInverse);
    }

    @Override
    public void setLocalNameWithPrefix(String localNameWithPrefix) {
        innerObjectProperty.setLocalNameWithPrefix(localNameWithPrefix);
    }
    
    @Override
    public void setPickListName(String pickListName) {
        innerObjectProperty.setPickListName(pickListName);
    }

    @Override
    public void setNamespace(String namespace) {
        innerObjectProperty.setNamespace(namespace);
    }

    @Override
    public void setNamespaceInverse(String namespaceInverse) {
        innerObjectProperty.setNamespaceInverse(namespaceInverse);
    }

    @Override
    public void setObjectIndividualSortPropertyURI(
            String objectIndividualSortPropertyURI) {
        innerObjectProperty
                .setObjectIndividualSortPropertyURI(objectIndividualSortPropertyURI);
    }

    @Override
    public void setObjectPropertyStatements(
            List<ObjectPropertyStatement> objectPropertyStatements) {
        innerObjectProperty
                .setObjectPropertyStatements(objectPropertyStatements);
    }

    @Override
    public void setOfferCreateNewOption(boolean b) {
        innerObjectProperty.setOfferCreateNewOption(b);
    }

    @Override
    public void setParentURI(String parentURI) {
        innerObjectProperty.setParentURI(parentURI);
    }


    @Override
    public void setPublicDescription(String s) {
        innerObjectProperty.setPublicDescription(s);
    }

    @Override
    public void setRangeDisplayLimit(int rangeDisplayLimit) {
        innerObjectProperty.setRangeDisplayLimit(rangeDisplayLimit);
    }

    @Override
    public void setRangeDisplayTier(Integer rangeDisplayTier) {
        innerObjectProperty.setRangeDisplayTier(rangeDisplayTier);
    }

    @Override
    public void setRangeEntitySortDirection(String rangeEntitySortDirection) {
        innerObjectProperty
                .setRangeEntitySortDirection(rangeEntitySortDirection);
    }

    @Override
    public void setRangeEntityURI(String rangeEntityURI) {
        innerObjectProperty.setRangeEntityURI(rangeEntityURI);
    }

    @Override
    public void setRangePublic(String rangePublic) {
        innerObjectProperty.setRangePublic(rangePublic);
    }

    @Override
    public void setRangeVClass(VClass rangeVClass) {
        innerObjectProperty.setRangeVClass(rangeVClass);
    }

    @Override
    public void setRangeVClassURI(String rangeClassURI) {
        innerObjectProperty.setRangeVClassURI(rangeClassURI);
    }

    @Override
    public void setSelectFromExisting(boolean b) {
        innerObjectProperty.setSelectFromExisting(b);
    }

    @Override
    public void setSymmetric(boolean symmetric) {
        innerObjectProperty.setSymmetric(symmetric);
    }

    @Override
    public void setTransitive(boolean transitive) {
        innerObjectProperty.setTransitive(transitive);
    }

    @Override
    public void setURI(String URI) {
        innerObjectProperty.setURI(URI);
    }

    @Override
    public void setURIInverse(String URIInverse) {
        innerObjectProperty.setURIInverse(URIInverse);
    }

    @Override
    public String toString() {
        return innerObjectProperty.toString();
    }

    @Override
    public void xmlToSysOut() {
        innerObjectProperty.xmlToSysOut();
    }

    @Override
    public boolean getStubObjectRelation() {
        return innerObjectProperty.getStubObjectRelation();
    }

    @Override
    public void setStubObjectRelation(boolean b) {
        innerObjectProperty.setStubObjectRelation(b);
    }

	@Override
	public boolean getCollateBySubclass() {		
		return innerObjectProperty.getCollateBySubclass();
	}

	@Override
	public void setCollateBySubclass(boolean collate) {		
		innerObjectProperty.setCollateBySubclass(collate);
	}

    
}
