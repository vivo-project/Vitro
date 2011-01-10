/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class ObjectPropertyStatementTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyStatementTemplateModel.class);  
    
    // RY WE may want to instead store the ObjectPropertyStatement; get the object from the data
    // the same way we do in BaseObjectPropertyDataPostProcessor.getQueryObjectVariableName, then
    // getting the value from the data.
    private String subjectUri; // we'll use these to make the edit links
    private String propertyUri;
    private Map<String, String> data;

    ObjectPropertyStatementTemplateModel(String subjectUri, String propertyUri, Map<String, String> data) {
        this.subjectUri = subjectUri;
        this.propertyUri = propertyUri;
        this.data = data;
    }

    
    /* Access methods for templates */

    public Object get(String key) {
        return data.get(key);
    }
    
    public String getEditLink() {
        return null;
    }
    
    public String getDeleteLink() {
        return null;
    }
}
