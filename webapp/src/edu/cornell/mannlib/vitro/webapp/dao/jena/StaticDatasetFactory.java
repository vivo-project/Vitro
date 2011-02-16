/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.query.Dataset;

public class StaticDatasetFactory implements DatasetWrapperFactory {
    
    private Dataset _dataset;
    
    public StaticDatasetFactory (Dataset dataset) {
        _dataset = dataset;
    }
    
    public DatasetWrapper getDatasetWrapper() {
        return new DatasetWrapper(_dataset);
    }
    
}