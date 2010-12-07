/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class ObjectPropertyStatementTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyStatementTemplateModel.class);  
    
    private ObjectPropertyStatement statement;

    ObjectPropertyStatementTemplateModel(ObjectPropertyStatement statement) {
        this.statement = statement;
    }
    
    /* Access methods for templates */
    
    public Individual getObject() {
        return statement.getObject();
    }
    
    public String getEditLink() {
        return null;
    }
    
    public String getDeleteLink() {
        return null;
    }
}
