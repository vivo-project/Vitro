/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public abstract class PropertyStatementTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyStatementTemplateModel.class);
    
    protected Individual subject = null; // not sure whether we want subject, or subject uri
    protected String subjectUri = null;
    

    public String getEditLink() {
        return null;
    }
    
    public String getDeleteLink() {
        return null;
    }
    
}
