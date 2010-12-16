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
            applySpecificPreprocessing(map);            
        }
    }
    
    /* Standard preprocessing that applies to all views. */
    protected void applyStandardPreprocessing(Map<String, String> map) {
        addLinkForTarget(map);   
    }
    
    protected void applySpecificPreprocessing(Map<String, String> map) { 
        /* Base class method is empty because this method is defined 
         * to apply subclass preprocessing. 
         */        
    }
    
    private void addLinkForTarget(Map<String, String> map) {
        String linkTarget = objectPropertyTemplateModel.getLinkTarget();
        String targetUri = map.get(linkTarget);
        if (targetUri != null) {
            String targetUrl = getLink(targetUri);
            map.put(linkTarget + "Url", targetUrl);
        } 
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
