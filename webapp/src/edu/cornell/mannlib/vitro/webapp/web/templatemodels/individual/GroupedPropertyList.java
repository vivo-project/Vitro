/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** The entire grouped property list for the subject.
 * If there are no groups defined or populated, use a dummy group so that the 
 * display logic in the templates is the same whether or not there are groups.
 * @author rjy7
 *
 */
public class GroupedPropertyList extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(GroupedPropertyList.class);
    
    private List<PropertyGroupTemplateModel> groups;

    GroupedPropertyList(WebappDaoFactory wdf, PropertyList propertyList) {
        
        // Get the property groups
        PropertyGroupDao pgDao = wdf.getPropertyGroupDao(); 
        List<PropertyGroup> groupList = pgDao.getPublicGroups(false); // may be returned empty but not null
//
//        List<PropertyGroupTemplateModel> groups = new ArrayList<PropertyGroupTemplateModel>(groupList.size());
//        for (PropertyGroup g : groupList) {
//            groups.add(new PropertyGroupTemplateModel(g));
//            // Properties unassigned to any group go in a dummy group with name an empty string. Templates
//            // must test for <#if ! group.name?has_content> or <#if group.name == ""> or <#if group.name?length == 0>
//            groups.add(new DummyPropertyGroupTemplateModel(""));
//        }
//        // If there are no groups, create a dummy group, so that the template display logic is the same
//        // in both cases. Name is null. Templates must test for <#if ! group.name??> 
//        if (groups.isEmpty()) {
//            groups.add(new DummyPropertyGroupTemplateModel(null));
//        }

    }

    
    /* 
     * Public getters for templates 
     */
    
    public List<PropertyGroupTemplateModel> getGroups() {
        return groups;
    }
    

}
