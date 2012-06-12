/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.context;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ApplicationConfig;

public interface AppConfigContextService {
        
    List<ApplicationConfig> getConfigsForContext(ConfigContext configContext, String configurationForURI )
    throws Exception;
}
