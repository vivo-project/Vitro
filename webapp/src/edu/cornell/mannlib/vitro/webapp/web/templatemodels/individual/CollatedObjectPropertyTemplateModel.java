/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final String SUBCLASS_VARIABLE_NAME = "subclass";
    
    private static final Pattern SELECT_SUBCLASS_PATTERN = 
        // SELECT ?subclass
        Pattern.compile("SELECT[^{]*\\?" + SUBCLASS_VARIABLE_NAME + "\\b", Pattern.CASE_INSENSITIVE);
        // ORDER BY ?subclass
        // ORDER BY DESC(?subclass)
    private static final Pattern ORDER_BY_SUBCLASS_PATTERN = 
        Pattern.compile("ORDER\\s+BY\\s+(DESC\\s*\\(\\s*)?\\?" + SUBCLASS_VARIABLE_NAME, Pattern.CASE_INSENSITIVE);
    
    private SortedMap<String, List<ObjectPropertyStatementTemplateModel>> subclasses;
    private WebappDaoFactory wdf;
    
    CollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, 
            VitroRequest vreq, EditingPolicyHelper policyHelper) 
            throws InvalidConfigurationException {
        
        super(op, subject, vreq, policyHelper); 
        
        /* Get the data */
        wdf = vreq.getWebappDaoFactory();
        ObjectPropertyStatementDao opDao = wdf.getObjectPropertyStatementDao();
        String subjectUri = subject.getURI();
        String propertyUri = op.getURI();
        List<Map<String, String>> statementData = 
            opDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, getSelectQuery(), getConstructQueries());

        /* Apply post-processing */
        postprocess(statementData, wdf);
        
        /* Collate the data */
        Map<String, List<ObjectPropertyStatementTemplateModel>> unsortedSubclasses = 
            collate(subjectUri, propertyUri, statementData, vreq, policyHelper);

        /* Sort by subclass name */
        subclasses = new TreeMap<String, List<ObjectPropertyStatementTemplateModel>>();
        subclasses.putAll(unsortedSubclasses); 
        
        for (List<ObjectPropertyStatementTemplateModel> list : subclasses.values()) {
            postprocessStatementList(list);
        }
    }
    
    protected ConfigError checkQuery(String queryString) {
        
        if (StringUtils.isBlank(queryString)) {
            return ConfigError.NO_SELECT_QUERY;
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
    
    @Override
    protected void removeDuplicates(List<Map<String, String>> data) {
       filterSubclasses(data);
    }
    
    /*
     * The query returns subclasses of a specific superclass that the object belongs to; for example,
     * in the case of authorInAuthorship, subclasses of core:InformationResource. Here we remove all but
     * the most specific subclass for the object. 
     */
    private void filterSubclasses(List<Map<String, String>> statementData) {
        String objectVariableName = getObjectKey();
        if (objectVariableName == null) {
            log.error("Cannot remove duplicate statements for property " + getUri() + " because no object found to dedupe.");
            return;
        } 
        
        if (log.isDebugEnabled()) {
            log.debug("Data before subclass filtering");
            logData(statementData);
        }
        
        List<Map<String, String>> filteredList = new ArrayList<Map<String, String>>();  
        Set<String> processedObjects = new HashSet<String>();
        for (Map<String, String> outerMap : statementData) {
            String objectUri = outerMap.get(objectVariableName);
            if (processedObjects.contains(objectUri)) {
                continue;
            }
            processedObjects.add(objectUri);
            List<Map<String, String>> dataForThisObject = new ArrayList<Map<String, String>>();
            for (Map<String, String> innerMap : statementData) {
                if ( innerMap.get(objectVariableName) == objectUri ) {
                    dataForThisObject.add(innerMap);
                }                
            }
            // Sort the data for this object from most to least specific subclass, with nulls at end
            Collections.sort(dataForThisObject, new SubclassComparator(wdf));
            filteredList.add(dataForThisObject.get(0));
        }

        statementData.clear();
        statementData.addAll(filteredList);
        
        if (log.isDebugEnabled()) {
            log.debug("Data after subclass filtering");
            logData(statementData);
        }
    }
    
    private class SubclassComparator implements Comparator<Map<String, String>> {
        
        private VClassDao vclassDao;
        
        SubclassComparator(WebappDaoFactory wdf) {
            this.vclassDao = wdf.getVClassDao();
        }

        @Override
        public int compare(Map<String, String> map1, Map<String, String> map2) {
            
            String subclass1 = map1.get(SUBCLASS_VARIABLE_NAME);
            String subclass2 = map2.get(SUBCLASS_VARIABLE_NAME);
            
            if (subclass1 == null) {
                if (subclass2 == null) {
                    return 0;
                } else {
                    return 1; // nulls rank highest
                }
            }
            
            if (subclass2 == null) {
                return -1; // nulls rank highest
            }
            
            if (subclass1.equals(subclass2)) {
                return 0;
            }
            
            List<String> superclasses = vclassDao.getAllSuperClassURIs(subclass1);
            if (superclasses.contains(subclass2)) {
                return -1;
            }
            
            superclasses = vclassDao.getAllSuperClassURIs(subclass2);
            if  (superclasses.contains(subclass1)) {
                return 1;
            }
            
            return 0;            
        }       
    }
    
    private Map<String, List<ObjectPropertyStatementTemplateModel>> collate(String subjectUri, String propertyUri,
            List<Map<String, String>> statementData, VitroRequest vreq, EditingPolicyHelper policyHelper) {
    
        Map<String, List<ObjectPropertyStatementTemplateModel>> subclassMap = 
            new HashMap<String, List<ObjectPropertyStatementTemplateModel>>();
        String currentSubclassUri = null;
        List<ObjectPropertyStatementTemplateModel> currentList = null;
        String objectKey = getObjectKey();
        for (Map<String, String> map : statementData) {
            String subclassUri = map.get(SUBCLASS_VARIABLE_NAME);
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
