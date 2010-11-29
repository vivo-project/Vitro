/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** The entire grouped property list for the subject.
 * If there are no groups defined or populated, use a dummy group so that the 
 * display logic in the templates is the same whether or not there are groups.
 * @author rjy7
 *
 */
public class GroupedPropertyList extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(GroupedPropertyList.class);
    private static final int MAX_GROUP_DISPLAY_RANK = 99;
    
    /** Don't include these properties in the list. */
    private static final Collection<String> SUPPRESSED_OBJECT_PROPERTIES = Collections
            .unmodifiableCollection(Arrays
                    .asList(new String[] { VitroVocabulary.IND_MAIN_IMAGE }));
    
    // RY Do we really want to store subject and vreq as members? Could just pass around.
    private Individual subject;
    private VitroRequest vreq;
    
    private List<PropertyGroupTemplateModel> groups;

    GroupedPropertyList(Individual subject, VitroRequest vreq) {
        // RY Do we really want to store these as members? Could just pass around.
        this.subject = subject;
        this.vreq = vreq;
        
        // Get the property groups
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        PropertyGroupDao pgDao = wdf.getPropertyGroupDao();
        List<PropertyGroup> groupList = pgDao.getPublicGroups(false); // may be returned empty but not null

        List<PropertyGroupTemplateModel> groups = new ArrayList<PropertyGroupTemplateModel>(groupList.size());
        for (PropertyGroup g : groupList) {
            groups.add(new PropertyGroupTemplateModel(g));
            // Properties unassigned to any group go in a dummy group with name an empty string. Templates
            // must test for <#if ! group.name?has_content> or <#if group.name == ""> or <#if group.name?length == 0>
            groups.add(new DummyPropertyGroupTemplateModel(""));
        }
        // If there are no groups, create a dummy group, so that the template display logic is the same
        // in both cases. Name is null. Templates must test for <#if ! group.name??> 
        if (groups.isEmpty()) {
            groups.add(new DummyPropertyGroupTemplateModel(null));
        }
    }

    /** 
     * Return true iff the user is editing. 
     */
    private boolean getEditingStatus() { 
        // These tests may change once self-editing issues are straightened out.
        boolean isSelfEditing = VitroRequestPrep.isSelfEditing(vreq);
        boolean isCurator = LoginStatusBean.getBean(vreq).isLoggedInAtLeast(LoginStatusBean.CURATOR);
        return isSelfEditing || isCurator;
    }
    
    /* 
     * Public getters for templates 
     */
    
    public List<PropertyGroupTemplateModel> getGroups() {
        return groups;
    }
    

}
