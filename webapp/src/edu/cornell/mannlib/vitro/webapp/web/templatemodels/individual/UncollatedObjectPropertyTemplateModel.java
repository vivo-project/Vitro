/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class UncollatedObjectPropertyTemplateModel extends ObjectPropertyTemplateModel {

    private static final Log log = LogFactory.getLog(UncollatedObjectPropertyTemplateModel.class);  
    
    private List<ObjectPropertyStatementTemplateModel> statements;
    
    UncollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, WebappDaoFactory wdf) {
        super(op, subject, wdf);        
        ObjectPropertyStatementDao opDao = wdf.getObjectPropertyStatementDao();
        List<ObjectPropertyStatement> opStatements = opDao.getObjectPropertyStatementsForIndividualByProperty(subject, op, getQueryString());
        statements = new ArrayList<ObjectPropertyStatementTemplateModel>();
        for (ObjectPropertyStatement ops : opStatements) {
            statements.add(new ObjectPropertyStatementTemplateModel(ops));
        }
    }
    
    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    
//    public List<SubclassList> getStatements() {
//        return subclassList;
//    }
    
    /* Access methods for templates */
    
    @Override
    public boolean isCollatedBySubclass() {
        return false;
    }
}
