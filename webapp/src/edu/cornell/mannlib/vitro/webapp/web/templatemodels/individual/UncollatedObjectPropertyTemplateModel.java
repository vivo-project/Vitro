/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class UncollatedObjectPropertyTemplateModel extends ObjectPropertyTemplateModel {

    private static final Log log = LogFactory.getLog(UncollatedObjectPropertyTemplateModel.class);  
    private static final String DEFAULT_CONFIG_FILE = "listViewConfig-default-uncollated.xml";
    
    private List<ObjectPropertyStatementTemplateModel> statements;
    
    UncollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, VitroRequest vreq, EditingHelper editLinkHelper) {
        super(op, subject, vreq, editLinkHelper);
        
        /* Get the data */
        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        ObjectPropertyStatementDao opDao = wdf.getObjectPropertyStatementDao();
        String subjectUri = subject.getURI();
        String propertyUri = op.getURI();
        List<Map<String, String>> statementData = 
            opDao.getObjectPropertyStatementsForIndividualByProperty(subjectUri, propertyUri, getQueryString());
        
        /* Apply postprocessing */
        postprocess(statementData, wdf);
        
        /* Put into data structure to send to template */
        statements = new ArrayList<ObjectPropertyStatementTemplateModel>(statementData.size());
        String objectKey = getObjectKey();
        for (Map<String, String> map : statementData) {
            statements.add(new ObjectPropertyStatementTemplateModel(subjectUri, 
                    propertyUri, objectKey, map, editLinkHelper));
        }
        
        postprocessStatementList(statements);
    }
    
    @Override
    protected String getDefaultConfigFileName() {
        return DEFAULT_CONFIG_FILE;
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
