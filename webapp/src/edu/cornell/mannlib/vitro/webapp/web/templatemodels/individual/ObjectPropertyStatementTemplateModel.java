/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class ObjectPropertyStatementTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyStatementTemplateModel.class);  
    
    private String subjectUri; // we'll use these to make the edit links
    private String propertyUri;
    private Map<String, Object> data;
    private WebappDaoFactory wdf;

    ObjectPropertyStatementTemplateModel(String subjectUri, String propertyUri, Map<String, Object> data, WebappDaoFactory wdf) {
        this.subjectUri = subjectUri;
        this.propertyUri = propertyUri;
        this.wdf = wdf;
        this.data = new HashMap<String, Object>(data.size());
        // See comments above StatementIndividual class definition on why we don't just set this.data = data.
        for (String key : data.keySet()) {
            Object value = data.get(key);
            if (value instanceof Individual) {
                Individual i = (Individual) value;
                this.data.put(key, new StatementObject(i));
            } else {
                this.data.put(key, value);
            } 
        }
    }

    /* This is a hopefully temporary solution to account for the fact that in the default
     * object property list view we are displaying the object's name and moniker.  These
     * cannot be derived from a simple sparql query. The name is either the label, localName, or id, 
     * because context nodes do not have labels. But in general we do not want to display context nodes
     * in the property list view; we are only displaying them temporarily until custom list views 
     * are implemented. In general any object that we want to display in a custom view should have a label,
     * and we can get that directly from the sparql query. Note that we can get the localName using an ARQ
     * function: PREFIX afn:   <http://jena.hpl.hp.com/ARQ/function#>
     * SELECT ?object (afn:localname(?object) AS ?localName) ...
     * but it is harder (or impossible) to do what the individual.getName() function does in a SPARQL query.
     * 
     * In the case of moniker, the Individual.getMoniker()
     * returns the VClass if moniker is null. But moniker is a vitro namespace property which will be 
     * eliminated in a future version, and the get-vclass-if-no-moniker logic should be moved into the
     * display modules where it belongs. In general any information that we would want to display in the custom
     * list view should be obtained directly in the sparql query. 
     * 
     * We don't want to put an Individual into the template model, because the beans wrapper used in IndividualController
     * has exposure level EXPOSE_SAFE, due to the need to call methods with parameters rather than simple parameterless
     * getters. We don't want to expose the Individual's setters to the template, so we wrap it in an individual that
     * only has getters.
     */
    public class StatementObject {
        
        private Individual individual;
        
        StatementObject(Individual individual) {
            this.individual = individual;
        }
        
        public String getName() {
            return individual.getName();
        }
        
        public String getMoniker() {
            return individual.getMoniker();
        }
        
        public String getUrl() {
            return UrlBuilder.getIndividualProfileUrl(individual, wdf);
        }
    }

    
    /* Access methods for templates */

    public Object get(String key) {
        return data.get(key);
    }
    

//    public IndividualTemplateModel getIndividual(String key) {
//        IndividualDao iDao = vreq.getWebappDaoFactory().getIndividualDao();
//        Individual individual = iDao.getIndividualByURI(data.get(key));
//        return new IndividualTemplateModel(individual, vreq);
//    }
    
    public String getEditLink() {
        return null;
    }
    
    public String getDeleteLink() {
        return null;
    }
}
