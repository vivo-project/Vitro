/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

/** List of object property statements for an individual, where the objects belong to a single subclass **/

public class SubclassList {
    
    String name = null;
    List<ObjectPropertyStatementTemplateModel> statements = null;

    SubclassList(String name, List<ObjectPropertyStatementTemplateModel> statements) {
        this.name = name;
        this.statements = statements;
    }
    
    public String getName() {
        return name;
    }
    
    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
}
