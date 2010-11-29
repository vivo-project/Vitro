/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ObjectPropertyStatementTemplateModel extends PropertyStatementTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyStatementTemplateModel.class);  
    
    // not sure whether we want the objects or the uris here
    //protected ObjectProperty property = null;
    protected String predicateUri = null;
    //protected Individual object = null;
    protected String objectUri = null;

    ObjectPropertyStatementTemplateModel(String subjectUri, String predicateUri, String objectUri) {
        this.subjectUri = subjectUri;
        this.predicateUri = predicateUri;
        this.objectUri = objectUri;
    }
}
