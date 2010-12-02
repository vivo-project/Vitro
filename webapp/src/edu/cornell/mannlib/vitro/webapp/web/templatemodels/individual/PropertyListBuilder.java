/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

/**
 * Build the list of ontology properties for display on an individual profile page.
 * @author rjy7
 *
 */

// RY We may not need this class. Logic for building the list can be moved to GroupedPropertyList.java.
// Wait and see how much code remains here - if little, just put in IndividualTemplateModel.
public class PropertyListBuilder {

    private static final Log log = LogFactory.getLog(PropertyListBuilder.class);
    private static final int MAX_GROUP_DISPLAY_RANK = 99;
    
    protected Individual subject;
    protected VitroRequest vreq;
    protected WebappDaoFactory wdf;
   
    PropertyListBuilder(Individual individual, VitroRequest vreq) {
        this.subject = individual;
        this.vreq = vreq;
        this.wdf = vreq.getWebappDaoFactory();
    }
    
    // RY Create the list here first to get it working. Then consider moving to GroupedPropertyList constructor.
    protected List<PropertyGroup> getPropertyList() {
        
        // Determine whether we're editing or not.
        boolean userCanEditThisProfile = getEditingStatus();

        // Create the property list for the subject. The properties will be put into groups later.
        //PropertyList propertyList = new PropertyList();
        List<Property> propertyList = new ArrayList<Property>();
        
        // First get all the object properties that occur in statements in the db with this subject as subject.
        // This may include properties that are not defined as "possible properties" for a subject of this class,
        // so we cannot just rely on getting that list.
        List<ObjectProperty> objectPropertyList = subject.getPopulatedObjectPropertyList();
        // If we're going to create a new template model object property with a name,
        // don't need to set editLabel, can just do this:
        //propertyList.addAll(objectPropertyList); 
        for (ObjectProperty op : objectPropertyList) {
            op.setEditLabel(op.getDomainPublic());
            propertyList.add(op);
        }
                
        // If editing this page, merge in object properties applicable to the individual that are currently
        // unpopulated, so the properties are displayed to allow statements to be added to these properties.
        // RY In future, we should limit this to properties that the user CAN add properties to.
        if (userCanEditThisProfile) {
            mergeAllPossibleObjectProperties(objectPropertyList, propertyList);
        }
        
        // Now do much the same with data properties: get the list of populated data properties, then add in placeholders for missing ones 
        // If we're going to create a new template model object property with a name,
        // don't need to set editLabel, can just do this:
        //propertyList.addAll(subject.getPopulatedDataPropertyList()); 
        List<DataProperty> dataPropertyList = subject.getPopulatedDataPropertyList();
        for (DataProperty dp : dataPropertyList) {
            dp.setEditLabel(dp.getPublicName());
            propertyList.add(dp);
        }

        if (userCanEditThisProfile) {
            mergeAllPossibleDataProperties(propertyList);           
        }

        sort(propertyList); //*** Does this do data and obj props, or just obj props??
        
        // *** ADD collation and statements here *** 
        // Don't  include custom sorting, since that will be handled from custom short views
        // We'll populate each item in the property list with its statements or subclass lists

        // Put the list into groups        
        //return new GroupedPropertyList(wdf, propertyList);
        List<PropertyGroup> groups = addPropertiesToGroups(propertyList);
        return groups;
    }
    
    /** 
     * Return true iff the user is editing. 
     * These tests may change once self-editing issues are straightened out. What we really need to know
     * is whether the user can edit this profile, not whether in general they are an editor.
     */
    private boolean getEditingStatus() { 
        boolean isSelfEditing = VitroRequestPrep.isSelfEditing(vreq);
        boolean isCurator = LoginStatusBean.getBean(vreq).isLoggedInAtLeast(LoginStatusBean.CURATOR);
        return isSelfEditing || isCurator;
    }

    @SuppressWarnings("unchecked")
    protected void sort(List<Property> propertyList) {            
        try {
            Collections.sort(propertyList, new PropertyRanker(vreq));
        } catch (Exception ex) {
            log.error("Exception sorting merged property list: " + ex.getMessage());
        }
    }
    
    private boolean alreadyOnObjectPropertyList(List<ObjectProperty> opList, PropertyInstance pi) {
        if (pi.getPropertyURI() == null) {
            return false;
        }
        for (ObjectProperty op : opList) {
            if (op.getURI() != null && op.getURI().equals(pi.getPropertyURI())) {
                return op.isSubjectSide() == pi.getSubjectSide();
            }
        }
        return false;
    }
    
    private boolean alreadyOnPropertyList(List<Property> propsList, Property p) {
        if (p.getURI() == null) {
            log.error("Property p has no propertyURI in alreadyOnPropertyList()");
            return true; // don't add to list
        }
        for (Property ptest : propsList) {
            if (ptest.getURI() != null && ptest.getURI().equals(p.getURI())) {
                return true;
            }
        }
        return false;
    }
    
    private void mergeAllPossibleObjectProperties(List<ObjectProperty> objectPropertyList, List<Property> propertyList) {
        PropertyInstanceDao piDao = wdf.getPropertyInstanceDao();
        // RY *** Does this exclude properties in the excluded namespaces already? If not, need same test as above
        Collection<PropertyInstance> allPropInstColl = piDao.getAllPossiblePropInstForIndividual(subject.getURI());
        if (allPropInstColl != null) {
            for (PropertyInstance pi : allPropInstColl) {
                if (pi != null) {
                    // RY Do we need to check this before checking if it's on the property list??
                    if (! alreadyOnObjectPropertyList(objectPropertyList, pi)) {
                        ObjectPropertyDao opDao = wdf.getObjectPropertyDao();
                        ObjectProperty op = opDao.getObjectPropertyByURI(pi.getPropertyURI());
                        if (op == null) {
                            log.error("ObjectProperty op returned null from opDao.getObjectPropertyByURI()");
                        } else if (op.getURI() == null) {
                            log.error("ObjectProperty op returned with null propertyURI from opDao.getObjectPropertyByURI()");
                        } else  if (! alreadyOnPropertyList(propertyList, op)) {
                            propertyList.add(op);
                        }
                    }
                } else {
                    log.error("a property instance in the Collection created by PropertyInstanceDao.getAllPossiblePropInstForIndividual() is unexpectedly null");
                }
            }
        } else {
            log.error("a null Collection is returned from PropertyInstanceDao.getAllPossiblePropInstForIndividual()");
        }                    
    }    

    protected void mergeAllPossibleDataProperties(List<Property> propertyList) {
        DataPropertyDao dpDao = wdf.getDataPropertyDao();
        // RY *** Does this exclude properties in the excluded namespaces already? If not, need same test as above
        Collection <DataProperty> allDatapropColl = dpDao.getAllPossibleDatapropsForIndividual(subject.getURI());
        if (allDatapropColl != null) {
            for (DataProperty dp : allDatapropColl ) {
                if (dp!=null) {
                    if (dp.getURI() == null) {
                        log.error("DataProperty dp returned with null propertyURI from dpDao.getAllPossibleDatapropsForIndividual()");
                    } else if (! alreadyOnPropertyList(propertyList, dp)) {
                        propertyList.add(dp);
                    }
                } else {
                    log.error("a data property in the Collection created in DataPropertyDao.getAllPossibleDatapropsForIndividual() is unexpectedly null)");
                }
            }
        } else {
            log.error("a null Collection is returned from DataPropertyDao.getAllPossibleDatapropsForIndividual())");
        }        
    }
    
    private List<PropertyGroup> addPropertiesToGroups(List<Property> propertyList) {
        
        // Get the property groups
        PropertyGroupDao pgDao = wdf.getPropertyGroupDao(); 
        List<PropertyGroup> groupList = pgDao.getPublicGroups(false); // may be returned empty but not null
        
        int groupsCount=0;
        try {
            groupsCount = populateGroupsListWithProperties(pgDao, groupList, propertyList);
        } catch (Exception ex) {
            log.error("Exception on trying to populate groups list with properties: "+ex.getMessage());
            ex.printStackTrace();
        }
        try {
            int removedCount = pgDao.removeUnpopulatedGroups(groupList);
            if (removedCount == 0) {
                log.warn("Of "+groupsCount+" groups, none removed by removeUnpopulatedGroups");
        /*  } else {
                log.warn("Of "+groupsCount+" groups, "+removedCount+" removed by removeUnpopulatedGroups"); */
            }
            groupsCount -= removedCount;      
        } catch (Exception ex) {
            log.error("Exception on trying to prune groups list with properties: "+ex.getMessage());
        }
        return null;
    }

    private int populateGroupsListWithProperties(PropertyGroupDao pgDao, List<PropertyGroup> groupList, List<Property> propertyList) {
        int count = groupList.size();
        PropertyGroup tempGroup = null;
        String unassignedGroupName = ""; // temp, for compilation
        if (unassignedGroupName!=null) {
            tempGroup = pgDao.createTempPropertyGroup(unassignedGroupName,MAX_GROUP_DISPLAY_RANK);
            log.debug("creating temp property group "+unassignedGroupName+" for any unassigned properties");
        }
        switch (count) {
        case 0: log.warn("groupsList has no groups on entering populateGroupsListWithProperties(); will create a new group \"other\"");
                break;
        case 1: break;
        default: try {
                     Collections.sort(groupList);
                 } catch (Exception ex) {
                     log.error("Exception on sorting groupsList in populateGroupsListWithProperties()");
                 }
        }
        if (count==0 && unassignedGroupName!=null) {
            groupList.add(tempGroup);
        }
        for (PropertyGroup pg : groupList) {
             if (pg.getPropertyList().size()>0) {
                 pg.getPropertyList().clear();
             }
             for (Property p : propertyList) {
                 if (p.getURI() == null) {
                     log.error("Property p has null URI in populateGroupsListWithProperties()");
                 } else if (p.getGroupURI()==null) {
                     if (tempGroup!=null) { // not assigned any group yet and are creating a group for unassigned properties
                         if (!alreadyOnPropertyList(tempGroup.getPropertyList(),p)) {
                          
                             tempGroup.getPropertyList().add(p);
                             log.debug("adding property "+p.getEditLabel()+" to members of temp group "+unassignedGroupName);
                         }
                     } // otherwise don't put that property on the list
                 } else if (p.getGroupURI().equals(pg.getURI())) {
                     if (!alreadyOnPropertyList(pg.getPropertyList(),p)) {
                         pg.getPropertyList().add(p);
                     }
                 }
             }
             if (pg.getPropertyList().size()>1) {
                 try {
                     Collections.sort(pg.getPropertyList(),new Property.DisplayComparatorIgnoringPropertyGroup());
                 } catch (Exception ex) {
                     log.error("Exception sorting property group "+pg.getName()+" property list: "+ex.getMessage());
                 }
             }
        }
        if (count>0 && tempGroup!=null && tempGroup.getPropertyList().size()>0) {
            groupList.add(tempGroup);
        }
        count = groupList.size();
        return count;
    }
    
    private class PropertyRanker implements Comparator {

        WebappDaoFactory wdf;
        PropertyGroupDao pgDao;

        private PropertyRanker(VitroRequest vreq) {
            this.wdf = vreq.getWebappDaoFactory();
            this.pgDao = wdf.getPropertyGroupDao();
        }
        
        public int compare (Object o1, Object o2) {
            Property p1 = (Property) o1;
            Property p2 = (Property) o2;
            
            // sort first by property group rank; if the same, then sort by property rank
            final int MAX_GROUP_RANK=99;
            
            int p1GroupRank=MAX_GROUP_RANK;
            try {
                if (p1.getGroupURI()!=null) {
                    PropertyGroup pg1 = pgDao.getGroupByURI(p1.getGroupURI());
                    if (pg1!=null) {
                        p1GroupRank=pg1.getDisplayRank();
                    }
                }
            } catch (Exception ex) {
                log.error("Cannot retrieve p1GroupRank for group "+p1.getEditLabel());
            }
            
            int p2GroupRank=MAX_GROUP_RANK;
            try {
                if (p2.getGroupURI()!=null) {
                    PropertyGroup pg2 = pgDao.getGroupByURI(p2.getGroupURI());
                    if (pg2!=null) {
                        p2GroupRank=pg2.getDisplayRank();
                    }
                }
            } catch (Exception ex) {
                log.error("Cannot retrieve p2GroupRank for group "+p2.getEditLabel());
            }
            
            // int diff = pgDao.getGroupByURI(p1.getGroupURI()).getDisplayRank() - pgDao.getGroupByURI(p2.getGroupURI()).getDisplayRank();
            int diff=p1GroupRank - p2GroupRank;
            if (diff==0) {
                diff = determineDisplayRank(p1) - determineDisplayRank(p2);
                if (diff==0) {
                    return p1.getEditLabel().compareTo(p2.getEditLabel());
                } else {
                    return diff;
                }
            }
            return diff;
        }
        
        private int determineDisplayRank(Property p) {
            if (p instanceof DataProperty) {
                DataProperty dp = (DataProperty)p;
                return dp.getDisplayTier();
            } else if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty)p;
                String tierStr = op.getDomainDisplayTier(); // no longer used: p.isSubjectSide() ? op.getDomainDisplayTier() : op.getRangeDisplayTier();
                try {
                    return Integer.parseInt(tierStr);
                } catch (NumberFormatException ex) {
                    log.error("Cannot decode object property display tier value "+tierStr+" as an integer");
                }
            } else {
                log.error("Property is of unknown class in PropertyRanker()");  
            }
            return 0;
        }
    }
}
