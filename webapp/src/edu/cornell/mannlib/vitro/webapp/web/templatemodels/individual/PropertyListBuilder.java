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
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

public class PropertyListBuilder {

    private static final Log log = LogFactory.getLog(PropertyListBuilder.class);
    private static final int MAX_GROUP_DISPLAY_RANK = 99;
    
    // Don't include these properties in the list. 
    private static final Collection<String> SUPPRESSED_OBJECT_PROPERTIES = Collections
            .unmodifiableCollection(Arrays
                    .asList(new String[] { VitroVocabulary.IND_MAIN_IMAGE }));
    
    protected Individual subject;
    protected VitroRequest vreq;
   
    PropertyListBuilder(Individual individual, VitroRequest vreq) {
        this.subject = individual;
        this.vreq = vreq;
    }
    
    protected List<Object> getPropertyList() {
        
        // Determine whether we're editing or not. 
        // These tests may change once self-editing issues are straightened out.
        boolean isSelfEditing = VitroRequestPrep.isSelfEditing(vreq);
        boolean isCurator = LoginStatusBean.getBean(vreq).isLoggedInAtLeast(LoginStatusBean.CURATOR);
        boolean isEditing = isSelfEditing || isCurator;
        
        // Determine whether to return a grouped or ungrouped property list.
        // If the call specified ungrouped, use ungrouped.
        // If the call specified grouped:

            WebappDaoFactory wdf = vreq.getWebappDaoFactory();
            PropertyGroupDao pgDao = wdf.getPropertyGroupDao();
            List <PropertyGroup> groupsList = pgDao.getPublicGroups(false); // may be returned empty but not null
            // Use ungrouped if no property groups are defined

            // If < 2 property groups are populated, we also want ungrouped, but we won't know that until we
            // get the property list.         


        // Assemble the property list
        List<Property> mergedPropertyList = new ArrayList<Property>();
        // First get the properties this entity actually has, presumably populated with statements 
        List<ObjectProperty> objectPropertyList = subject.getObjectPropertyList();  

        for (ObjectProperty op : objectPropertyList) {
            if (!SUPPRESSED_OBJECT_PROPERTIES.contains(op)) {
                op.setEditLabel(op.getDomainPublic());
                mergedPropertyList.add(op);
            }else{
                log.debug("suppressed " + op.getURI());
            }
        }
        
        // If < 2 property groups populated, we want ungrouped. 
        // This can just be handled by not including a group, or using an "empty" group with no name.
        
        return null;        
    }
}
