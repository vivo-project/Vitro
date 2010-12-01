/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;

public class DataPropertyTemplateModel extends PropertyTemplateModel {
    
    private static final String TYPE = "data";

    DataPropertyTemplateModel(DataProperty property) {
        super(property);
    }

    
    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }

}
