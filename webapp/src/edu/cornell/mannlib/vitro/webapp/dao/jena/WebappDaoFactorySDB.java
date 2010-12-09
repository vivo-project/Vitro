/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.HashSet;

import com.hp.hpl.jena.query.Dataset;

import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;

public class WebappDaoFactorySDB extends WebappDaoFactoryJena {

	private DatasetWrapperFactory dwf;
	
	/**
	 * For use when any database connection associated with the Dataset
	 * is managed externally
	 * @param ontModelSelector
	 * @param dataset
	 */
	public WebappDaoFactorySDB(OntModelSelector ontModelSelector, Dataset dataset) {
		super(ontModelSelector);
		this.dwf = new StaticDatasetFactory(dataset);
	}
	
    /**
     * For use when any database connection associated with the Dataset
     * is managed externally
     * @param ontModelSelector
     * @param dataset
     */
	public WebappDaoFactorySDB(OntModelSelector ontModelSelector, Dataset dataset, String defaultNamespace, HashSet<String> nonuserNamespaces, String[] preferredLanguages) {
		super(ontModelSelector, defaultNamespace, nonuserNamespaces, preferredLanguages);
        this.dwf = new StaticDatasetFactory(dataset);
	}
	
	@Override
    public IndividualDao getIndividualDao() {
        if (entityWebappDao != null)
            return entityWebappDao;
        else
            return entityWebappDao = new IndividualDaoSDB(dwf, this);
    }
	
	@Override
	public DataPropertyStatementDao getDataPropertyStatementDao() {
		if (dataPropertyStatementDao != null) 
			return dataPropertyStatementDao;
		else
			return dataPropertyStatementDao = new DataPropertyStatementDaoSDB(dwf, this);
	}
	
	@Override
	public ObjectPropertyStatementDao getObjectPropertyStatementDao() {
		if (objectPropertyStatementDao != null) 
			return objectPropertyStatementDao;
		else
			return objectPropertyStatementDao = new ObjectPropertyStatementDaoSDB(dwf, this);
	}
	
	@Override
	public VClassDao getVClassDao() {
		if (vClassDao != null) 
			return vClassDao;
		else
			return vClassDao = new VClassDaoSDB(dwf, this);
	}
	
	private class StaticDatasetFactory implements DatasetWrapperFactory {
	 
	    private Dataset dataset;
	    
	    public StaticDatasetFactory (Dataset dataset) {
	        this.dataset = dataset;
	    }
	    
	    public DatasetWrapper getDatasetWrapper() {
	        return new DatasetWrapper(dataset);
	    }
	    
	}
	
}
