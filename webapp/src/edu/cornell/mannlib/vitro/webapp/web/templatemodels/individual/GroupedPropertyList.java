/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** 
 * The entire grouped property list for the subject.
 * If there are no groups defined or populated, use a dummy group so that the 
 * display logic in the templates is the same whether or not there are groups.
 * @author rjy7
 *
 */
public class GroupedPropertyList extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(GroupedPropertyList.class);
    private static final int MAX_GROUP_DISPLAY_RANK = 99;
    
    private List<PropertyGroupTemplateModel> groups;
    private DummyPropertyGroupTemplateModel ungroupedGroup;

    GroupedPropertyList(WebappDaoFactory wdf, PropertyList propertyList) {
        
        // Get the property groups
        PropertyGroupDao pgDao = wdf.getPropertyGroupDao(); 
        List<PropertyGroup> groupList = pgDao.getPublicGroups(false); // may be returned empty but not null

        List<PropertyGroupTemplateModel> groups = new ArrayList<PropertyGroupTemplateModel>(groupList.size());
        for (PropertyGroup g : groupList) {
            groups.add(new PropertyGroupTemplateModel(g));
            // Properties unassigned to any group go in a dummy group with name an empty string. Templates
            // must test for <#if ! group.name?has_content> or <#if group.name == ""> or <#if group.name?length == 0>
            ungroupedGroup = new DummyPropertyGroupTemplateModel("");            
        }
        // If there are no groups, create a dummy group, so that the template display logic is the same
        // in both cases. Name is null. Templates must test for <#if ! group.name??> 
        if (groups.isEmpty()) {
            ungroupedGroup = new DummyPropertyGroupTemplateModel(null);          
        }
        groups.add(ungroupedGroup);
               

//      if (groupedMode) {
//      int groupsCount=0;
//      try {
//          groupsCount = populateGroupListWithProperties(pgDao,groupList,propertyList); //,groupForUngroupedProperties);
//      } catch (Exception ex) {
//          log.error("Exception on trying to populate groups list with properties: "+ex.getMessage());
//          ex.printStackTrace();
//      }
//      try {
//          int removedCount = pgDao.removeUnpopulatedGroups(groupList);
//          if (removedCount == 0) {
//              log.warn("Of "+groupsCount+" groups, none removed by removeUnpopulatedGroups");
//      /*  } else {
//              log.warn("Of "+groupsCount+" groups, "+removedCount+" removed by removeUnpopulatedGroups"); */
//          }
//          groupsCount -= removedCount;
//          //req.setAttribute("groupsCount", new Integer(groupsCount));
//          if (groupsCount > 0) { //still
//              for (PropertyGroup g : groupList) {
//                  int statementCount=0;
//                  if (g.getPropertyList()!=null && g.getPropertyList().size()>0) {
//                      for (Property p : g.getPropertyList()) {
//                          if (p instanceof ObjectProperty) {
//                              ObjectProperty op = (ObjectProperty)p;
//                              List<ObjectPropertyStatement> opStmts = op.getObjectPropertyStatements();
//                              if (op.getObjectPropertyStatements()!=null && opStmts.size()>0) {
//                                  statementCount += opStmts.size();
//                                  
//                                  // If not collated, we need to apply custom sorting now. 
//                                  //applyCustomSortToUncollatedProperty(op, opStmts);
//                              }
//
//
//                          }
//                      }
//                  }
//                  g.setStatementCount(statementCount);
//              }
//          }
//      } catch (Exception ex) {
//          log.error("Exception on trying to prune groups list with properties: "+ex.getMessage());
//      }
//      propertyList.clear();
      
//  } else { // ungrouped mode
//      for (Property p : mergedPropertyList) {
//          if (p instanceof ObjectProperty) {
//              ObjectProperty op = (ObjectProperty)p;
//              applyCustomSortToUncollatedProperty(op, op.getObjectPropertyStatements()); 
//          }
//      }        }

  
        
    }

//    private int populateGroupListWithProperties(PropertyGroupDao pgDao, List<PropertyGroup> groupList, PropertyList propertyList) {//, String unassignedGroupName) {
//        int count = groupList.size();
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
//                     Collections.sort(groupList);
//                 } catch (Exception ex) {
//                     log.error("Exception on sorting groupList in populateGroupListWithProperties()");
//                 }
//        }
//        if (count==0 && unassignedGroupName!=null) {
//            groupList.add(tempGroup);
//        }
//        for (PropertyGroup pg : groupList) {
//             if (pg.getPropertyList().size()>0) {
//                 pg.getPropertyList().clear();
//             }
//             List<PropertyTemplateModel> properties = propertyList.getProperties();
//             for (PropertyTemplateModel ptm : properties) {
//                 
//                 Property p = ptm.getProperty();
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
//            groupList.add(tempGroup);
//        }
//        count = groupList.size();
//        return count;
//    }
    
    /* 
     * Public getters for templates 
     */
    
    public List<PropertyGroupTemplateModel> getGroups() {
        return groups;
    }

}
