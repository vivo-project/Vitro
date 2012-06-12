/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDaoUtils;

public class JenaAppConfigBase extends JenaBaseDaoUtils{
    OntModel appModel;
    String localAppNamespace;
    
    public JenaAppConfigBase(OntModel appModel, String localAppNamespace) {
        super();
        this.appModel = appModel;
        this.localAppNamespace = localAppNamespace;
    }            
}
