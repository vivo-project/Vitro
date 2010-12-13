/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class CollatedObjectPropertyTemplateModel extends ObjectPropertyTemplateModel {

    private static final Log log = LogFactory.getLog(CollatedObjectPropertyTemplateModel.class);  
    
    private Map<String, List<ObjectPropertyStatementTemplateModel>> collatedStatements;
    //private List<SubclassList> subclassList;
    
    CollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, WebappDaoFactory wdf) {
        super(op, subject, wdf);        
        ObjectPropertyStatementDao opDao = wdf.getObjectPropertyStatementDao();
        String subjectUri = subject.getURI();
        String propertyUri = op.getURI();
        List<Map<String, Object>> statementData = opDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, getQueryString());
        collatedStatements = new HashMap<String, List<ObjectPropertyStatementTemplateModel>>(statementData.size());
//        for (Map<String, Object> map : statementData) {
//            statements.add(new ObjectPropertyStatementTemplateModel(subjectUri, propertyUri, map, wdf));
//        }
        
        if (statementData.size() > 0) {
            String collationTarget = getCollationTarget();
            List<VClass> vclasses = getDirectVClasses(collationTarget, statementData);
        }
        
    }
    
    private List<VClass> getDirectVClasses(String key, List<Map<String, Object>> data) {
        return null;
    }

    /* Access methods for templates */
    
    public Map<String, List<ObjectPropertyStatementTemplateModel>> getCollatedStatements() {
        return collatedStatements;
    }
    
    @Override
    public boolean isCollatedBySubclass() {
        return true;
    }
}
