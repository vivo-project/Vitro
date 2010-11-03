/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashSet;

import com.hp.hpl.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;

public class WebappDaoFactorySDB extends WebappDaoFactoryJena {

	private Dataset dataset;
	
	public WebappDaoFactorySDB(OntModelSelector ontModelSelector, Dataset dataset) {
		super(ontModelSelector);
		this.dataset = dataset;
	}
	
	public WebappDaoFactorySDB(OntModelSelector ontModelSelector, Dataset dataset, String defaultNamespace, HashSet<String> nonuserNamespaces, String[] preferredLanguages) {
		super(ontModelSelector, defaultNamespace, nonuserNamespaces, preferredLanguages);
        this.dataset = dataset;
	}
	
	@Override
    public IndividualDao getIndividualDao() {
        if (entityWebappDao != null)
            return entityWebappDao;
        else
            return entityWebappDao = new IndividualDaoSDB(dataset, this);
    }
	
}
