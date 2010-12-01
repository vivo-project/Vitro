/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public abstract class ObjectPropertyTemplateModel extends PropertyTemplateModel {
    
    private static final String TYPE = "object";

    ObjectPropertyTemplateModel(ObjectProperty property) {
        super(property);
    }
    

    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }
    
    public abstract boolean getIsCollatedBySubtype();

}
