/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans;

import java.util.List;

/**
 * TODO: this needs to work with Jim's FakeApplicationOntologyService
 * and ShortViewServiceImpl.
 * 
 * @author bdc34
 *
 */
public class ShortView {
    String uri;
    List<String> dataGetterURIs; 
    
    public String getURI(){ return uri; }
    public void setURI( String uri ){ this.uri = uri; }
    
    
    public List<String> getDataGetterURIs() {
        return dataGetterURIs;
    }
    public void setDataGetterURIs(List<String> dataGetterURIs) {
        this.dataGetterURIs = dataGetterURIs;
    }        
}
