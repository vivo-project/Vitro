/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class CollatedObjectPropertyTemplateModel extends ObjectPropertyTemplateModel {

    private static final Log log = LogFactory.getLog(CollatedObjectPropertyTemplateModel.class);  
    private static final Pattern SELECT_SUBCLASS_PATTERN = 
        // SELECT ?subclass
        Pattern.compile("SELECT[^{]*\\?subclass\\b", Pattern.CASE_INSENSITIVE);
        // ORDER BY ?subclass
        // ORDER BY DESC(?subclass)
    private static final Pattern ORDER_BY_SUBCLASS_PATTERN = 
        Pattern.compile("ORDER\\s+BY\\s+(DESC\\s*\\(\\s*)?\\?subclass", Pattern.CASE_INSENSITIVE);

    private static enum ConfigError {
        NO_QUERY("Missing query specification"),
        NO_SUBCLASS_SELECT("Query does not select a subclass variable"),
        NO_SUBCLASS_ORDER_BY("Query does not sort first by subclass variable");
        
        String message;
        
        ConfigError(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String toString() {
            return getMessage();
        }
    }
    
    private SortedMap<String, List<ObjectPropertyStatementTemplateModel>> subclasses;
    
    CollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, 
            VitroRequest vreq, EditingPolicyHelper policyHelper) 
        throws InvalidCollatedPropertyConfigurationException {
        
        super(op, subject, vreq, policyHelper); 
        
        // RY It would be more efficient to check for these errors in the super constructor, so that we don't
        // go through the rest of that constructor before throwing an error. In that case, the subclasses
        // could each have their own checkConfiguration() method.
        ConfigError configError = checkConfiguration();
        if ( configError != null ) {
            throw new InvalidCollatedPropertyConfigurationException("Invalid configuration for collated property " + 
                    op.getURI() + ":" + configError + ". Creating uncollated display instead."); 
        }

        /* Get the data */
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        ObjectPropertyStatementDao opDao = wdf.getObjectPropertyStatementDao();
        String subjectUri = subject.getURI();
        String propertyUri = op.getURI();
        List<Map<String, String>> statementData = 
            opDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, getQueryString());

        /* Apply post-processing */
        postprocess(statementData, wdf);
        
        /* Collate the data */
        Map<String, List<ObjectPropertyStatementTemplateModel>> unsortedSubclasses = 
            collate(subjectUri, propertyUri, statementData, vreq, policyHelper);

        /* Sort by subclass name */
        Comparator<String> comparer = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
            }};
        subclasses = new TreeMap<String, List<ObjectPropertyStatementTemplateModel>>(comparer);
        subclasses.putAll(unsortedSubclasses); 
        
        for (List<ObjectPropertyStatementTemplateModel> list : subclasses.values()) {
            postprocessStatementList(list);
        }
    }
    
    private ConfigError checkConfiguration() {

        String queryString = getQueryString();
        
        if (StringUtils.isBlank(queryString)) {
            return ConfigError.NO_QUERY;
        }
        
        Matcher m;
        m = SELECT_SUBCLASS_PATTERN.matcher(queryString); 
        if ( ! m.find() ) { 
            return ConfigError.NO_SUBCLASS_SELECT;
        } 
        
        m = ORDER_BY_SUBCLASS_PATTERN.matcher(queryString);
        if ( ! m.find() ) {
            return ConfigError.NO_SUBCLASS_ORDER_BY;
        }
        
        return null;
    }
    
    private Map<String, List<ObjectPropertyStatementTemplateModel>> collate(String subjectUri, String propertyUri,
            List<Map<String, String>> statementData, VitroRequest vreq, EditingPolicyHelper policyHelper) {
    
        Map<String, List<ObjectPropertyStatementTemplateModel>> subclassMap = 
            new HashMap<String, List<ObjectPropertyStatementTemplateModel>>();
        String currentSubclassUri = null;
        List<ObjectPropertyStatementTemplateModel> currentList = null;
        String objectKey = getObjectKey();
        for (Map<String, String> map : statementData) {
            String subclassUri = map.get("subclass");
            // Rows with no subclass are put into a subclass map with an empty name.
            if (subclassUri == null) {
                subclassUri = "";
            }
            if (!subclassUri.equals(currentSubclassUri)) {
                currentSubclassUri = subclassUri;
                currentList = new ArrayList<ObjectPropertyStatementTemplateModel>();
                String subclassName = getSubclassName(subclassUri, vreq);
                subclassMap.put(subclassName, currentList);
            }
            currentList.add(new ObjectPropertyStatementTemplateModel(subjectUri, 
                    propertyUri, objectKey, map, policyHelper));
        }   
        return subclassMap; 
    }
    
    private String getSubclassName(String subclassUri, VitroRequest vreq) {
        String subclassName = null;
        if (subclassUri.isEmpty()) {
            subclassName = "";
        } else {
            VClassDao vclassDao = vreq.getWebappDaoFactory().getVClassDao();
            VClass vclass = vclassDao.getVClassByURI(subclassUri);
            subclassName = vclass.getName();
        }
        return subclassName;
    }
    
    /* Access methods for templates */
    
    public Map<String, List<ObjectPropertyStatementTemplateModel>> getSubclasses() {
        return subclasses;
    }
    
    @Override
    public boolean isCollatedBySubclass() {
        return true;
    }
}
