/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PropertyDisplayConfig extends DisplayConfig {
    Collection<String> listViewURIs;

    public Collection<String> getListViewURIs() {
        if( listViewURIs == null)
            return Collections.emptyList();
        else
            return listViewURIs;
    }
    public void setListViewURIs(Collection<String> listViewURIs) {
        if( listViewURIs == null )
            this.listViewURIs = new ArrayList<String>();
        else
            this.listViewURIs = listViewURIs;
    }
    
    
}
