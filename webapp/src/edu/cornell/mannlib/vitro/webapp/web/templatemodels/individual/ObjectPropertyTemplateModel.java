/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;

public class ObjectPropertyTemplateModel extends PropertyTemplateModel {
    
    private static final String TYPE = "object";
    private List<ObjectPropertyStatementTemplateModel> statements;

    ObjectPropertyTemplateModel(ObjectProperty property) {
        super(property);
        statements = new ArrayList<ObjectPropertyStatementTemplateModel>();
        
        // get the statements from the db via sparql query
    }
    
    protected static ObjectPropertyTemplateModel getObjectPropertyTemplateModel(ObjectProperty op) {
        return op.getCollateBySubclass() ? new CollatedObjectPropertyTemplateModel(op) 
                                         : new ObjectPropertyTemplateModel(op);
    }

    /* Access methods for templates */
    
    public String getType() {
        return TYPE;
    }
    
    public boolean isCollatedBySubclass() {
        return false;
    }

    @Override
    public String getAddLink() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getEditLink() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getDeleteLink() {
        // TODO Auto-generated method stub
        return null;
    }

    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        // TODO Auto-generated method stub
        return null;
    }
}
