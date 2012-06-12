/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans;

public class ObjectPropertyDisplayConfig extends PropertyDisplayConfig{
    Boolean collateBySubclass=null;

    public boolean getCollateBySubclass() {
        if( collateBySubclass != null)
            return collateBySubclass;
        else
            return false;
    }    
    public Boolean getCollateBySubclassBoolean() {
        return collateBySubclass;
    }
    
    public void setCollateBySubclass(Boolean collateBySubclass) {
        this.collateBySubclass = collateBySubclass;
    }
    
}
