/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

public class CollatedObjectProperty extends ObjectPropertyTemplateModel {

    private List<SubclassList> subclassList = null;
    
    CollatedObjectProperty(String predicateUri) {
        super(predicateUri);
        // TODO Auto-generated constructor stub
    }
    
    public List<SubclassList> getSubclassList() {
        return subclassList;
    }
    
    public List<SubclassList> getStatements() {
        return subclassList;
    }
}
