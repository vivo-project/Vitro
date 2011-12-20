/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class PropertyStatementTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyStatementTemplateModel.class); 
    
    protected final VitroRequest vreq;
    // Used for editing
    protected final String subjectUri;
    protected final String propertyUri;
    protected String editUrl;
    protected String deleteUrl;
 
    
    PropertyStatementTemplateModel(String subjectUri, String propertyUri, EditingPolicyHelper policyHelper, VitroRequest vreq) {
        this.vreq = vreq;        
        this.subjectUri = subjectUri;
        this.propertyUri = propertyUri;              
        editUrl = "";
        deleteUrl = "";
    }
    
    
    
    /* Template properties */
    
    public String getEditUrl() {
        return editUrl;
    }
    
    public String getDeleteUrl() {
        return deleteUrl;
    }
    
    public boolean isEditable() {
        return ! editUrl.isEmpty();
    }

}
