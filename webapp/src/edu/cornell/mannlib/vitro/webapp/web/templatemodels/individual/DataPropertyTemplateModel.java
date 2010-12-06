/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;

public class DataPropertyTemplateModel extends PropertyTemplateModel {
    
    private static final String TYPE = "data";

    DataPropertyTemplateModel(DataProperty dp) {
        super(dp);
        
        // get the data property statements from the db via sparql query
    }

    
    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }


    @Override
    public String addLink() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String editLink() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String deleteLink() {
        // TODO Auto-generated method stub
        return null;
    }

}
