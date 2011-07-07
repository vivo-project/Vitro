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
    
    protected final ObjectPropertyTemplateModel objectPropertyTemplateModel;
    protected final WebappDaoFactory wdf;
    
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
        
        processList(data);

        for (Map<String, String> map : data) {
            process(map);           
        }
    }
    
    /** Postprocessing that applies to the list as a whole - reordering, removing duplicates, etc. */
    protected void processList(List<Map<String, String>> data) {
        objectPropertyTemplateModel.removeDuplicates(data);
    }
    
    /** Postprocessing that applies to individual list items */
    protected abstract void process(Map<String, String> map);
    

    /* Postprocessor methods callable from any postprocessor */

    protected void addName(Map<String, String> map, String nameKey, String objectKey) {
        String name = map.get(nameKey);
        if (name == null) {
            // getIndividual() could return null
            Individual ind = getIndividual(map.get(objectKey));
            if (ind != null) {
                map.put(nameKey, ind.getName());
            }
        }
    }
    
    protected Individual getIndividual(String uri) {
        return wdf.getIndividualDao().getIndividualByURI(uri);
    }

}
