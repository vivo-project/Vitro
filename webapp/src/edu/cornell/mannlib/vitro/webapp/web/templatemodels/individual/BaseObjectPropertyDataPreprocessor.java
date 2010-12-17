/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public abstract class BaseObjectPropertyDataPreprocessor implements
        ObjectPropertyDataPreprocessor {

    protected ObjectPropertyTemplateModel objectPropertyTemplateModel;
    protected WebappDaoFactory wdf;
    
    public BaseObjectPropertyDataPreprocessor(ObjectPropertyTemplateModel optm, WebappDaoFactory wdf) {
        this.objectPropertyTemplateModel = optm;
        this.wdf = wdf;
    }
    
    
    @Override
    public void process(List<Map<String, String>> data) {
        for (Map<String, String> map : data) {
            process(map);           
        }
    }
    
    protected abstract void process(Map<String, String> map);
     
    /* Preprocessor helper methods callable from any preprocessor */
    
    protected String getMoniker(String uri) {
        return getIndividual(uri).getMoniker();
    }
    
    protected String getName(String uri) {
        return getIndividual(uri).getName();
    }

    protected Individual getIndividual(String uri) {
        return wdf.getIndividualDao().getIndividualByURI(uri);
    }
}
