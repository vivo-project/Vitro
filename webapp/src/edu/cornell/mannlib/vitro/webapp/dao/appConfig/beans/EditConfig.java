/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans;

public class EditConfig extends ApplicationConfig {
    Integer entryLimit;

    public int getEntryLimit() {
        if( entryLimit == null )
            return -1;
        else
            return entryLimit;
    }
    public Integer getEntryLimitInteger() {
        return entryLimit;
    }
    public void setEntryLimit(Integer entryLimit) {
        this.entryLimit = entryLimit;
    }            
    
}
