/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataPropertyStatementTemplateModel extends PropertyStatementTemplateModel {
    
    private static final Log log = LogFactory.getLog(DataPropertyStatementTemplateModel.class);  
    
    // not sure whether we want the objects or the uris here
    //protected DataProperty property = null;
    protected String predicateUri = null;
    protected String data = null;

    DataPropertyStatementTemplateModel(String subjectUri, String predicateUri, String data) {
        this.subjectUri = subjectUri;
        this.predicateUri = predicateUri;
        this.data = data;
    }
}
