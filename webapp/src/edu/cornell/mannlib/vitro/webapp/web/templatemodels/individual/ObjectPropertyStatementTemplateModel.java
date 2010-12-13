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
        // See comments above the StatementObject class definition on why we don't just set this.data = data.
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
     * object property list view we display the object's name and moniker, and these
     * cannot be derived from a simple sparql query. 
     * 
     * The name is either the label, localName, or id, because context nodes do not have labels
     * (and blank nodes do not have local names?). But in general we do not want to display context nodes
     * in the property list view; we are only displaying them temporarily until custom list views 
     * are implemented. In general any object that we want to display in a custom view should have a label,
     * and we can get that directly from the sparql query. Note that we can get the localName using an ARQ
     * function: PREFIX afn:   <http://jena.hpl.hp.com/ARQ/function#>
     * SELECT ?object (afn:localname(?object) AS ?localName) ...
     * but it is harder (or impossible) to do what the individual.getName() function does in a SPARQL query.
     * 
     * In the case of moniker, the Individual.getMoniker() returns the VClass if moniker is null.
     * But moniker is a vitro namespace property which will be eliminated in a future version, 
     * and the get-vclass-if-no-moniker logic should be moved into the display modules where it belongs. 
     * In general any information that we would want to display in the custom list view should be obtained 
     * directly in the sparql query, and that should be generally true for custom queries and views.
     * 
     * We could instead make these methods of the outer class that take a uri parameter, but then the template
     * semantics is less intuitive: we would have ${statement.moniker(object)} rather than
     * ${statement.object.name}, but the moniker is not a property of the statement.
     * 
     * We don't want to put an Individual into the template model, because the beans wrapper used in IndividualController
     * has exposure level EXPOSE_SAFE, due to the need to call methods with parameters rather than simple parameterless
     * getters. We don't want to expose the Individual's setters to the template, so we wrap it in an individual that
     * only has getters.
     * 
     * RY *** Consider doing this only for the default query. The custom query can just store the data values as strings
     * (uri, label, etc.). There should be no issues with label and moniker in a custom query (but is that true, or do
     * some custom queries display the moniker?), and to handle url we can create a directive <@profileUrl individual=object />
     * where object is the object uri. This will get the WebappDaoFactory from the request and call
     * UrlBuilder.getIndividualProfileUrl(String individualUri, WebappDaoFactory wdf). Equivalently, have a method of this
     * object getProfileUrl(String uri), so in the template we call ${statement.profileUrl(object)} (but still the semantics
     * is a bit weird, since the profile url doesn't belong to the statement).
     */
    public class StatementObject {
        
        private Individual individual;
        
        StatementObject(Individual individual) {
            this.individual = individual;
        }
        
        /* Access methods for templates */
        
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
    
    public String getEditLink() {
        return null;
    }
    
    public String getDeleteLink() {
        return null;
    }
}
