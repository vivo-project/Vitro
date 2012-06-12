/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.appConfig.PropertyEditConfigDao;

public class PropertyEditConfigDaoJena  extends JenaAppConfigBase implements PropertyEditConfigDao{
	
	  public PropertyEditConfigDaoJena(OntModel appModel,
	            String localAppNamespace) {
	        super(appModel, localAppNamespace);
	    }

}
