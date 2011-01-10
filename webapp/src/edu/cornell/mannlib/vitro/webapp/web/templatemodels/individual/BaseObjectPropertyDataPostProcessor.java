/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public abstract class BaseObjectPropertyDataPostProcessor implements
        ObjectPropertyDataPostProcessor {

    private static final Log log = LogFactory.getLog(BaseObjectPropertyDataPostProcessor.class); 
    
    protected ObjectPropertyTemplateModel objectPropertyTemplateModel;
    protected WebappDaoFactory wdf;
    
    public BaseObjectPropertyDataPostProcessor(ObjectPropertyTemplateModel optm, WebappDaoFactory wdf) {
        this.objectPropertyTemplateModel = optm;
        this.wdf = wdf;
    }
        
    @Override
    public void process(List<Map<String, String>> data) {
        
        if (data.isEmpty()) {
            log.debug("No data to postprocess for property " + objectPropertyTemplateModel.getUri());
            return;
        }
        
        removeDuplicates(data);
        
        for (Map<String, String> map : data) {
            process(map);           
        }
    }
    
    protected abstract void process(Map<String, String> map);
    
    /** The SPARQL query results may contain duplicate rows for a single object, if there are multiple solutions 
     * to the entire query. Remove duplicates here by arbitrarily selecting only the first row returned.
     * Note that in the case of a collated query, the query has filtered out inferred subclasses, but if there
     * are multiple asserted subclasses, all will be returned. This method will arbitrarily remove all but the
     * first one returned.
     * @param List<Map<String, String>> data
     */
    protected void removeDuplicates(List<Map<String, String>> data) {
        String objectVariableName = objectPropertyTemplateModel.getObjectKey();
        if (objectVariableName == null) {
            log.error("Cannot remove duplicate statements for property " + objectPropertyTemplateModel.getName() + " because no object found to dedupe.");
            return;
        }
        List<String> foundObjects = new ArrayList<String>();
        log.debug("Removing duplicates from property: " + objectPropertyTemplateModel.getUri());
        Iterator<Map<String, String>> dataIterator = data.iterator();
        while (dataIterator.hasNext()) {
            Map<String, String> map = dataIterator.next();
            String objectValue = map.get(objectVariableName);
            // We arbitrarily remove all but the first. Not sure what selection criteria could be brought to bear on this.
            if (foundObjects.contains(objectValue)) {
                dataIterator.remove();
            } else {
                foundObjects.add(objectValue);
            }
        }
    }
    

    /* Postprocessor methods callable from any postprocessor */

    protected void addName(Map<String, String> map, String nameKey, String objectKey) {
        String name = map.get(nameKey);
        if (name == null) {
            map.put(nameKey, getIndividual(map.get(objectKey)).getName());
        }
    }
    
    /* This is a temporary measure to handle the fact that the current Individual.getMoniker()
     * method returns the individual's VClass if moniker is null. We want to replicate that
     * behavior here, but in future the moniker property (along with other Vitro namespace
     * properties) will be removed. In addition, this type of logic (display x if it exists, otherwise y)
     * will be moved into the display modules (Editing and Display Configuration Improvements).
     */
    protected void addMoniker(Map<String, String> map, String monikerKey, String objectKey) {
        String moniker = map.get(monikerKey);
        if (moniker == null) {
            map.put(monikerKey, getIndividual(map.get(objectKey)).getMoniker());
        }
    }
    
    protected Individual getIndividual(String uri) {
        return wdf.getIndividualDao().getIndividualByURI(uri);
    }

}
