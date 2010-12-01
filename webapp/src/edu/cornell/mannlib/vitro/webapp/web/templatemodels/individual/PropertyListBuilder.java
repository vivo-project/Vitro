/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
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
   
    PropertyListBuilder(Individual individual, VitroRequest vreq) {
        this.subject = individual;
        this.vreq = vreq;
    }
    
    // RY Create the list here first to get it working. Then consider moving to GroupedPropertyList constructor.
    protected GroupedPropertyList getPropertyList() {
        
        // Determine whether we're editing or not.
        boolean userCanEditThisProfile = getEditingStatus();

    

        
        // Create the property list for the subject. The properties will be put into groups later.
        PropertyList propertyList = new PropertyList();
        
        // First get all the object properties that occur in statements in the db with this subject as subject.
        // This may include properties that are not defined as "possible properties" for a subject of this class,
        // so we cannot just rely on getting that list.
        List<ObjectProperty> objectPropertyList = subject.getPopulatedObjectPropertyList();
        propertyList.addObjectProperties(objectPropertyList);

        WebappDaoFactory wdf = vreq.getWebappDaoFactory();           
        
        // If editing this page, merge in object properties applicable to the individual that are currently
        // unpopulated, so the properties are displayed to allow statements to be added to these properties.
        // RY In future, we should limit this to properties that the user CAN add properties to.
        if (userCanEditThisProfile) {
            propertyList.mergeAllPossibleObjectProperties(wdf, subject, objectPropertyList);
        }
        
        // Now do much the same with data properties: get the list of populated data properties, then add in placeholders for missing ones
        List<DataProperty> dataPropertyList = subject.getPopulatedDataPropertyList();
        propertyList.addDataProperties(dataPropertyList);

        if (userCanEditThisProfile) {
            propertyList.mergeAllPossibleDataProperties(wdf, subject);           
        }

        propertyList.sort(vreq);

        // *** ADD collation here *** 
        // Don't  include custom sorting, since that will be handled from custom short views

        // Put the list into groups
        

//        if (groupedMode) {
//            int groupsCount=0;
//            try {
//                groupsCount = populateGroupsListWithProperties(pgDao,groupList,propertyList); //,groupForUngroupedProperties);
//            } catch (Exception ex) {
//                log.error("Exception on trying to populate groups list with properties: "+ex.getMessage());
//                ex.printStackTrace();
//            }
//            try {
//                int removedCount = pgDao.removeUnpopulatedGroups(groupList);
//                if (removedCount == 0) {
//                    log.warn("Of "+groupsCount+" groups, none removed by removeUnpopulatedGroups");
//            /*  } else {
//                    log.warn("Of "+groupsCount+" groups, "+removedCount+" removed by removeUnpopulatedGroups"); */
//                }
//                groupsCount -= removedCount;
//                //req.setAttribute("groupsCount", new Integer(groupsCount));
//                if (groupsCount > 0) { //still
//                    for (PropertyGroup g : groupList) {
//                        int statementCount=0;
//                        if (g.getPropertyList()!=null && g.getPropertyList().size()>0) {
//                            for (Property p : g.getPropertyList()) {
//                                if (p instanceof ObjectProperty) {
//                                    ObjectProperty op = (ObjectProperty)p;
//                                    List<ObjectPropertyStatement> opStmts = op.getObjectPropertyStatements();
//                                    if (op.getObjectPropertyStatements()!=null && opStmts.size()>0) {
//                                        statementCount += opStmts.size();
//                                        
//                                        // If not collated, we need to apply custom sorting now. 
//                                        //applyCustomSortToUncollatedProperty(op, opStmts);
//                                    }
//
//
//                                }
//                            }
//                        }
//                        g.setStatementCount(statementCount);
//                    }
//                }
//            } catch (Exception ex) {
//                log.error("Exception on trying to prune groups list with properties: "+ex.getMessage());
//            }
//            propertyList.clear();
            
//        } else { // ungrouped mode
//            for (Property p : mergedPropertyList) {
//                if (p instanceof ObjectProperty) {
//                    ObjectProperty op = (ObjectProperty)p;
//                    applyCustomSortToUncollatedProperty(op, op.getObjectPropertyStatements()); 
//                }
//            }        }

        
        return new GroupedPropertyList(wdf, propertyList);
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

//    private int populateGroupsListWithProperties(PropertyGroupDao pgDao, List<PropertyGroup> groupsList, List<Property> mergedPropertyList) {//, String unassignedGroupName) {
//        int count = groupsList.size();
//        PropertyGroup tempGroup = null;
//        String unassignedGroupName = ""; //temp, for compilation
//        if (unassignedGroupName!=null) {
//            tempGroup = pgDao.createTempPropertyGroup(unassignedGroupName,MAX_GROUP_DISPLAY_RANK);
//            log.debug("creating temp property group "+unassignedGroupName+" for any unassigned properties");
//        }
//        switch (count) {
//        case 0: log.warn("groupsList has no groups on entering populateGroupsListWithProperties(); will create a new group \"other\"");
//                break;
//        case 1: break;
//        default: try {
//                     Collections.sort(groupsList);
//                 } catch (Exception ex) {
//                     log.error("Exception on sorting groupsList in populateGroupsListWithProperties()");
//                 }
//        }
//        if (count==0 && unassignedGroupName!=null) {
//            groupsList.add(tempGroup);
//        }
//        for (PropertyGroup pg : groupsList) {
//             if (pg.getPropertyList().size()>0) {
//                 pg.getPropertyList().clear();
//             }
//             for (Property p : mergedPropertyList) {
//                 if (p.getURI() == null) {
//                     log.error("Property p has null URI in populateGroupsListWithProperties()");
//                 } else if (p.getGroupURI()==null) {
//                     if (tempGroup!=null) { // not assigned any group yet and are creating a group for unassigned properties
//                         if (!alreadyOnPropertyList(tempGroup.getPropertyList(),p)) {
//                             tempGroup.getPropertyList().add(p);
//                             log.debug("adding property "+p.getEditLabel()+" to members of temp group "+unassignedGroupName);
//                         }
//                     } // otherwise don't put that property on the list
//                 } else if (p.getGroupURI().equals(pg.getURI())) {
//                     if (!alreadyOnPropertyList(pg.getPropertyList(),p)) {
//                         pg.getPropertyList().add(p);
//                     }
//                 }
//             }
//             if (pg.getPropertyList().size()>1) {
//                 try {
//                     Collections.sort(pg.getPropertyList(),new Property.DisplayComparatorIgnoringPropertyGroup());
//                 } catch (Exception ex) {
//                     log.error("Exception sorting property group "+pg.getName()+" property list: "+ex.getMessage());
//                 }
//             }
//        }
//        if (count>0 && tempGroup!=null && tempGroup.getPropertyList().size()>0) {
//            groupsList.add(tempGroup);
//        }
//        count = groupsList.size();
//        return count;
//    }
}
