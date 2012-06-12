/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans;

import java.util.Collection;

public class ApplicationConfig {
    String URI;
    String publicDescription;
    
    /** list of URIs that this ApplicationConfig is an inheriting configuration for */
    Collection<String> inheritingConfigurationFor;
    
    /** list of URIs that this ApplicationConfig is an non-inheriting configuration for */
    Collection<String> noninheritingConfigurationFor;
    
    public String getURI() {
        return URI;
    }
    public void setURI(String uRI) {
        URI = uRI;
    }
    
    public String getPublicDescription() {
        return publicDescription;
    }
    public void setPublicDescription(String publicDescription) {
        this.publicDescription = publicDescription;
    }
    
    public Collection<String> getInheritingConfigurationFor() {
        return inheritingConfigurationFor;
    }
    public void setInheritingConfigurationFor(
            Collection<String> inheritingConfigurationFor) {
        this.inheritingConfigurationFor = inheritingConfigurationFor;
    }
    
    public Collection<String> getNoninheritingConfigurationFor() {
        return noninheritingConfigurationFor;
    }
    public void setNoninheritingConfigurationFor(Collection<String> configurationFor) {
        this.noninheritingConfigurationFor = configurationFor;
    }
    
}
