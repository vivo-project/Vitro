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
    
    private SortedMap<String, List<ObjectPropertyStatementTemplateModel>> subclasses;
    
    CollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, VitroRequest vreq) throws Exception {
        super(op, subject, vreq); 

        // RY Temporarily throw an error because collation hasn't been implemented yet.
        boolean error = true;
        if (error) {
            throw new Exception("Collated object property not implemented yet");
        }
        
        /* Change the approach to collation:
         * Custom views can get the subclasses in the query. Must use a term ?subclass - throw error if not.
         * Default view: we may be able to figure out the  class to get subclasses of by inspecting the property.
         * If not, use getDirectClasses etc of the object term.
         * We need a subclassed and nonsubclassed default query for the default view: collated-query and uncollated-query.
         * We can also use these for custom views. Throw error if property is collated but there's no subclass term
         * in the query. (The reverse is okay - uncollated property with a subclass term in the query.     
         */
        
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        ObjectPropertyStatementDao opDao = wdf.getObjectPropertyStatementDao();
        String subjectUri = subject.getURI();
        String propertyUri = op.getURI();
        List<Map<String, String>> statementData = opDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, getQueryString());
        
        Map<String, List<ObjectPropertyStatementTemplateModel>> unsortedSubclasses = hasCustomListView() ?
                collateCustomListView(subjectUri, propertyUri, statementData, vreq) :
                collateDefaultListView(subjectUri, propertyUri, statementData, vreq);  

        /* Sort by subclass name */
        Comparator<String> comparer = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
            }};
        subclasses = new TreeMap<String, List<ObjectPropertyStatementTemplateModel>>(comparer);
        subclasses.putAll(unsortedSubclasses);        
    }
    
    private Map<String, List<ObjectPropertyStatementTemplateModel>> collateCustomListView(String subjectUri, 
            String propertyUri, List<Map<String, String>> statementData, VitroRequest vreq) {
    
        Map<String, List<ObjectPropertyStatementTemplateModel>> unsortedSubclasses = 
            new HashMap<String, List<ObjectPropertyStatementTemplateModel>>();
        String currentSubclassUri = null;
        List<ObjectPropertyStatementTemplateModel> currentList = null;
        for (Map<String, String> map : statementData) {
            String subclassUri = map.get("subclass");
            if (!subclassUri.equals(currentSubclassUri)) {
                currentSubclassUri = subclassUri;
                currentList = new ArrayList<ObjectPropertyStatementTemplateModel>();
                String subclassName = getSubclassName(subclassUri, vreq);
                unsortedSubclasses.put(subclassName, currentList);
            }
            currentList.add(new ObjectPropertyStatementTemplateModel(subjectUri, propertyUri, map));
        }   
        return unsortedSubclasses; 
    }
    
    private Map<String, List<ObjectPropertyStatementTemplateModel>> collateDefaultListView(String subjectUri, 
            String propertyUri, List<Map<String, String>> statementData, VitroRequest vreq) {
        
        Map<String, List<ObjectPropertyStatementTemplateModel>> unsortedSubclasses = 
            new HashMap<String, List<ObjectPropertyStatementTemplateModel>>();
        return unsortedSubclasses;
    }
    
    private String getSubclassName(String subclassUri, VitroRequest vreq) {
        VClassDao vclassDao = vreq.getWebappDaoFactory().getVClassDao();
        VClass vclass = vclassDao.getVClassByURI(subclassUri);
        return vclass.getName();
    }
    
    private String getCollationTargetError() {
        String errorMessage = null;
        String collationTarget = getCollationTarget();
        // Make sure the collation target is not null or empty.
        if (collationTarget == null || collationTarget.trim().isEmpty()) {
            errorMessage = "No collation target specified.";
        } else {
            // Make sure the collation target is one of the select terms in the query.
            String queryString = getQueryString();
            String selectClause = queryString.substring(0, queryString.indexOf("{"));
            Pattern collationTargetPattern = Pattern.compile("\\b\\\\?" + collationTarget + "\\b");
            Matcher matcher = collationTargetPattern.matcher(selectClause);
            if (! matcher.find()) {
                errorMessage = "Invalid collation target.";
            }
        }   
        return errorMessage;
    }
    
    private List<VClass> getDirectVClasses(String key, List<Map<String, Object>> data) {
        return null;
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
