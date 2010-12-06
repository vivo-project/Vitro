/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

/** List of object property statements for an individual, where the objects belong to a single subclass **/

public class SubclassList {
    
    String name = null;
    List<ObjectPropertyStatementTemplateModel> statements = null;

    SubclassList(String name) {
        this.name = name;
        this.statements = new ArrayList<ObjectPropertyStatementTemplateModel>();
        
        // get the obj property statements for this subclass from the db via sparql query
    
    }

    /* Access methods for templates */
    
    public String getName() {
        return name;
    }
    
    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
}
