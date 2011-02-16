/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class PropertyStatementTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyStatementTemplateModel.class); 

    private static enum EditAccess {
        EDIT, DELETE;
    }
    
    // Used for editing
    protected String subjectUri = null;
    protected String propertyUri = null;
    private List<EditAccess> editAccessList = null;
    
    PropertyStatementTemplateModel(String subjectUri, String propertyUri, EditingPolicyHelper policyHelper) {
        
        if (policyHelper != null) { // we're editing
            this.subjectUri = subjectUri;
            this.propertyUri = propertyUri;  
            editAccessList = new ArrayList<EditAccess>(); 
        }
    }
    
    protected void markEditable() {
        editAccessList.add(EditAccess.EDIT);
    }
    
    protected void markDeletable() {
        editAccessList.add(EditAccess.DELETE);
    }
    
    protected boolean isEditable() {
        return editAccessList.contains(EditAccess.EDIT);
    }
    
    protected boolean isDeletable() {
        return editAccessList.contains(EditAccess.DELETE);
    }
}
