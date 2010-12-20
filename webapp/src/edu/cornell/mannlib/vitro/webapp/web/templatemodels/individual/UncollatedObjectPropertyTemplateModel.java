/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class UncollatedObjectPropertyTemplateModel extends ObjectPropertyTemplateModel {

    private static final Log log = LogFactory.getLog(UncollatedObjectPropertyTemplateModel.class);  
    
    private List<ObjectPropertyStatementTemplateModel> statements;
    
    UncollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, WebappDaoFactory wdf) {
        super(op, subject, wdf);        
        ObjectPropertyStatementDao opDao = wdf.getObjectPropertyStatementDao();
        String subjectUri = subject.getURI();
        String propertyUri = op.getURI();
        List<Map<String, String>> statementData = opDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, getQueryString());
        postprocess(statementData, wdf);
        statements = new ArrayList<ObjectPropertyStatementTemplateModel>(statementData.size());
        for (Map<String, String> map : statementData) {
            statements.add(new ObjectPropertyStatementTemplateModel(subjectUri, propertyUri, map));
        }
    }
    
    /* Access methods for templates */

    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    
    @Override
    public boolean isCollatedBySubclass() {
        return false;
    }
}
