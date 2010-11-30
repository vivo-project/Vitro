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
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
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
    
    /** Don't include these properties in the list. */
    // RY This should perhaps be moved to ObjectPropertyTemplateModel
    private static final Collection<String> SUPPRESSED_OBJECT_PROPERTIES = Collections
            .unmodifiableCollection(Arrays
                    .asList(new String[] { VitroVocabulary.IND_MAIN_IMAGE }));
    
    protected Individual subject;
    protected VitroRequest vreq;
   
    PropertyListBuilder(Individual individual, VitroRequest vreq) {
        this.subject = individual;
        this.vreq = vreq;
    }
    
    protected GroupedPropertyList getPropertyList() {
        
        // Determine whether we're editing or not.
        boolean isEditing = getEditingStatus();

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

        return null;        
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
}
