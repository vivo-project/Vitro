/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;

import edu.cornell.mannlib.vitro.webapp.dao.appConfig.context.ConfigContext;

public class ConfigContextJena implements ConfigContext {
    String contextFor;
    String qualifiedBy;
    
    @Override
    public ConfigContext configContextFor(String uri) {
        contextFor = uri;
        return this;
    }

    @Override
    public String getConfigContextFor() {
        return contextFor;
    }

    @Override
    public ConfigContext qualifiedBy(String uri) {
        qualifiedBy = uri;
        return this;
    }

    @Override
    public String getQuifiedBy() {
        return qualifiedBy;
    }

}
