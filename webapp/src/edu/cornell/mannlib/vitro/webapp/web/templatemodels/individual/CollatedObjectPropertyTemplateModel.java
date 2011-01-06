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
    private static final String DEFAULT_CONFIG_FILE = "listViewConfig-default-collated.xml";
    private static final Pattern QUERY_PATTERN = Pattern.compile("SELECT[^{]*\\?subclass\\b");
    
    private SortedMap<String, List<ObjectPropertyStatementTemplateModel>> subclasses;
    
    CollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, VitroRequest vreq) 
        throws InvalidConfigurationException {
        
        super(op, subject, vreq); 
        
        if ( ! validConfigurationForCollatedProperty() ) {
            throw new InvalidConfigurationException("Invalid configuration for property " + op.getURI() + 
                                                    ": Query does not select a subclass variable."); 
        }

        /* Get the data */
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        ObjectPropertyStatementDao opDao = wdf.getObjectPropertyStatementDao();
        String subjectUri = subject.getURI();
        String propertyUri = op.getURI();
        List<Map<String, String>> statementData = 
            opDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, getQueryString());

        /* Apply postprocessing */
        postprocess(statementData, wdf);
        
        /* Collate the data */
        Map<String, List<ObjectPropertyStatementTemplateModel>> unsortedSubclasses = 
            collate(subjectUri, propertyUri, statementData, vreq);

        /* Sort by subclass name */
        Comparator<String> comparer = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
            }};
        subclasses = new TreeMap<String, List<ObjectPropertyStatementTemplateModel>>(comparer);
        subclasses.putAll(unsortedSubclasses);        
    }
    
    private boolean validConfigurationForCollatedProperty() {
        boolean validConfig = true;
        
        // Make sure the query selects a ?subclass variable. 
        String queryString = getQueryString();
        Matcher m = QUERY_PATTERN.matcher(queryString);
        if ( ! m.find() ) { 
            validConfig = false;
        }    
        
        return validConfig;
    }
    
    private Map<String, List<ObjectPropertyStatementTemplateModel>> collate(String subjectUri, 
            String propertyUri, List<Map<String, String>> statementData, VitroRequest vreq) {
    
        Map<String, List<ObjectPropertyStatementTemplateModel>> subclassMap = 
            new HashMap<String, List<ObjectPropertyStatementTemplateModel>>();
        String currentSubclassUri = null;
        List<ObjectPropertyStatementTemplateModel> currentList = null;
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
            currentList.add(new ObjectPropertyStatementTemplateModel(subjectUri, propertyUri, map));
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

    @Override
    protected String getDefaultConfigFileName() {
        return DEFAULT_CONFIG_FILE;
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
