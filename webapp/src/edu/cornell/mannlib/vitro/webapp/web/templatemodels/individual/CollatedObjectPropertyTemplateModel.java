/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public class CollatedObjectPropertyTemplateModel extends ObjectPropertyTemplateModel {

    private List<SubclassList> subclassList;
    
    CollatedObjectPropertyTemplateModel(ObjectProperty property) {
        super(property);
        subclassList = new ArrayList<SubclassList>();
    }
    
    public List<SubclassList> getSubclassList() {
        return subclassList;
    }
    
//    public List<SubclassList> getStatements() {
//        return subclassList;
//    }
    
    /* Access methods for templates */
    
    @Override
    public boolean isCollatedBySubclass() {
        return true;
    }
}
