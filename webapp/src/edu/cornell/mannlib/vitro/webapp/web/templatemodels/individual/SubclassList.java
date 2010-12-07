/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/** List of object property statements for an individual, where the objects belong to a single subclass **/

public class SubclassList extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(SubclassList.class); 
    
    String name;
    List<ObjectPropertyStatementTemplateModel> statements;

    SubclassList(String name) {
        this.name = name;
        this.statements = new ArrayList<ObjectPropertyStatementTemplateModel>();
        
        // get the obj property statements for this subclass from the db via  a sparql query
    
    }

    /* Access methods for templates */
    
    public String getName() {
        return name;
    }
    
    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
}
