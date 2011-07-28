/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.filtering;

import java.util.LinkedList;
import java.util.List;

import net.sf.jga.algorithms.Filter;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.filtering.filters.VitroFilters;

public class DataPropertyFiltering extends DataProperty {
    private VitroFilters filters;
    private DataProperty innerDataProperty;
    
    public DataPropertyFiltering(DataProperty innerDataProperty, VitroFilters filters){
        this.innerDataProperty = innerDataProperty;
        this.filters = filters;
    }
    
    /**
     * Need to filter DataPropertyStatements and return DataPropertyStatements
     * wrapped in DataPropertyStatementsFiltering. 
     */
    @Override
    public List<DataPropertyStatement> getDataPropertyStatements() {        
        List<DataPropertyStatement> propStmts =  innerDataProperty.getDataPropertyStatements();
        if( propStmts == null ) return null;
        
        List<DataPropertyStatement> filteredStmts = new LinkedList<DataPropertyStatement>();
        Filter.filter(propStmts, filters.getDataPropertyStatementFilter(), filteredStmts);
        
        List<DataPropertyStatement> wrappedStmts = new LinkedList<DataPropertyStatement>();
        for( DataPropertyStatement stmt : filteredStmts){
            wrappedStmts.add( new DataPropertyStatementFiltering(stmt, filters) );
        }
        return wrappedStmts;        
    }
    
           
    /* the rest of the methods are delegated with no filtering */
    @Override
    public int compareTo(DataProperty op) {
        return innerDataProperty.compareTo(op);
    }

    @Override
    public boolean equals(Object obj) {
        return innerDataProperty.equals(obj);
    }

    @Override
    public String getDescription() {
        return innerDataProperty.getDescription();
    }

    @Override
    public int getDisplayLimit() {
        return innerDataProperty.getDisplayLimit();
    }

    @Override
    public int getDisplayTier() {
        return innerDataProperty.getDisplayTier();
    }

    @Override
    public String getDomainClassURI() {
        return innerDataProperty.getDomainClassURI();
    }

    @Override
    public String getPublicName() {
        return innerDataProperty.getPublicName();
    }

    @Override
    public String getLabel() {
        return innerDataProperty.getLabel();
    }

    @Override
    public String getExample() {
        return innerDataProperty.getExample();
    }

    @Override
    public String getGroupURI() {
        return innerDataProperty.getGroupURI();
    }

    @Override
    public RoleLevel getHiddenFromDisplayBelowRoleLevel() {
        return innerDataProperty.getHiddenFromDisplayBelowRoleLevel();
    }
    
    @Override
    public RoleLevel getProhibitedFromUpdateBelowRoleLevel() {
        return innerDataProperty.getProhibitedFromUpdateBelowRoleLevel();
    }

    @Override
    public String getLocalName() {
        return innerDataProperty.getLocalName();
    }
    
    @Override
    public String getLocalNameWithPrefix() {
        return innerDataProperty.getLocalNameWithPrefix();
    }
    
    @Override
    public String getPickListName() {
        return innerDataProperty.getPickListName();
    }

    @Override
    public String getNamespace() {
        return innerDataProperty.getNamespace();
    }

    @Override
    public String getPublicDescription() {
        return innerDataProperty.getPublicDescription();
    }

    @Override
    public String getURI() {
        return innerDataProperty.getURI();
    }

    @Override
    public int hashCode() {
        return innerDataProperty.hashCode();
    }

    @Override
    public boolean isAnonymous() {
        return innerDataProperty.isAnonymous();
    }

    @Override
    public boolean isSubjectSide() {
        return innerDataProperty.isSubjectSide();
    }

    @Override
    public void setDescription(String description) {
        innerDataProperty.setDescription(description);
    }

    @Override
    public void setPublicName(String publicName) {
        innerDataProperty.setPublicName(publicName);
    }

    @Override
    public void setDomainClassURI(String domainClassURI) {
        innerDataProperty.setDomainClassURI(domainClassURI);
    }

    @Override
    public void setLabel(String label) {
        innerDataProperty.setLabel(label);
    }

    @Override
    public void setExample(String example) {
        innerDataProperty.setExample(example);
    }

    @Override
    public void setGroupURI(String in) {
        innerDataProperty.setGroupURI(in);
    }

    @Override
    public void setHiddenFromDisplayBelowRoleLevel(RoleLevel eR) {
        innerDataProperty.setHiddenFromDisplayBelowRoleLevel(eR);
    }
    
    @Override
    public void setHiddenFromDisplayBelowRoleLevelUsingRoleUri(String roleUri) {
        innerDataProperty.setHiddenFromDisplayBelowRoleLevel(BaseResourceBean.RoleLevel.getRoleByUri(roleUri));
    }

    @Override
    public void setProhibitedFromUpdateBelowRoleLevel(RoleLevel eR) {
        innerDataProperty.setProhibitedFromUpdateBelowRoleLevel(eR);
    }
    
    @Override
    public void setProhibitedFromUpdateBelowRoleLevelUsingRoleUri(String roleUri) {
        innerDataProperty.setProhibitedFromUpdateBelowRoleLevel(BaseResourceBean.RoleLevel.getRoleByUri(roleUri));
    }

    @Override
    public void setLocalName(String localName) {
        innerDataProperty.setLocalName(localName);
    }

    @Override
    public void setLocalNameWithPrefix(String localNameWithPrefix) {
        innerDataProperty.setLocalNameWithPrefix(localNameWithPrefix);
    }
    
    @Override
    public void setPickListName(String pickListName) {
        innerDataProperty.setPickListName(pickListName);
    }

    @Override
    public void setNamespace(String namespace) {
        innerDataProperty.setNamespace(namespace);
    }

    @Override
    public void setDataPropertyStatements(
            List<DataPropertyStatement> objectPropertyStatements) {
        innerDataProperty
                .setDataPropertyStatements(objectPropertyStatements);
    }

    @Override
    public void setPublicDescription(String s) {
        innerDataProperty.setPublicDescription(s);
    }

    @Override
    public void setDisplayLimit(int displayLimit) {
        innerDataProperty.setDisplayLimit(displayLimit);
    }

    @Override
    public void setDisplayTier(int displayTier) {
        innerDataProperty.setDisplayTier(displayTier);
    }

    @Override
    public void setURI(String URI) {
        innerDataProperty.setURI(URI);
    }

    @Override
    public String toString() {
        return innerDataProperty.toString();
    }
}
