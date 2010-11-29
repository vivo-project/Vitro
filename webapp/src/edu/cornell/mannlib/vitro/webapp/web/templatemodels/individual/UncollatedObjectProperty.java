/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

public class UncollatedObjectProperty extends ObjectPropertyTemplateModel {

    private List<ObjectPropertyStatementTemplateModel> statements = null;
    
    UncollatedObjectProperty(String predicateUri) {
        super(predicateUri);
        // TODO Auto-generated constructor stub
    }
    
    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }

}
