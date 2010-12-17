/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class BaseObjectPropertyDataPreprocessor implements
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
            applyStandardPreprocessing(map);
            applyPropertySpecificPreprocessing(map);            
        }
    }
    
    /* Standard preprocessing that applies to all views. */
    protected void applyStandardPreprocessing(Map<String, String> map) {
        /* none identified yet */
    }
    
    protected void applyPropertySpecificPreprocessing(Map<String, String> map) { 
        /* Base class method is empty because this method is defined 
         * to apply subclass preprocessing. 
         */        
    }
     
    /* Preprocessor helper methods callable from any preprocessor */
    
    protected String getLink(String uri) {
        return UrlBuilder.getIndividualProfileUrl(uri, wdf);
    }
    
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
