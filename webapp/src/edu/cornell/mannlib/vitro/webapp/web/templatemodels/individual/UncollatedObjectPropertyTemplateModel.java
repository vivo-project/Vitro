/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public class UncollatedObjectPropertyTemplateModel extends ObjectPropertyTemplateModel {

    private List<ObjectPropertyStatementTemplateModel> statements;
    
    UncollatedObjectPropertyTemplateModel(ObjectProperty property) {
        super(property);
        statements = new ArrayList<ObjectPropertyStatementTemplateModel>();
        
        // get the statements from the db via sparql query
    }
    
    @Override
    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    

    /* Access methods for templates */
    
    @Override
    public boolean isCollatedBySubclass() {
        return false;
    }
}
