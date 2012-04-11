/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class PropertyStatementTemplateModel extends BaseTemplateModel {
	protected static final String EDIT_PATH = "editRequestDispatch";  

    protected final VitroRequest vreq;
    protected final String subjectUri;
    protected final String propertyUri;
    
    PropertyStatementTemplateModel(String subjectUri, String propertyUri, VitroRequest vreq) {
        this.vreq = vreq;        
        this.subjectUri = subjectUri;
        this.propertyUri = propertyUri;              
    }
    
    /* Template properties */
    
    public abstract String getEditUrl();
    public abstract String getDeleteUrl();
    public boolean isEditable() {
        return ! getEditUrl().isEmpty();
    }

}
