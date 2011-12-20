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
    
    private final List<ObjectPropertyStatementTemplateModel> statements;
    
    UncollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject, 
            VitroRequest vreq, EditingPolicyHelper policyHelper, 
            List<ObjectProperty> populatedObjectPropertyList)
        throws InvalidConfigurationException {
        
        super(op, subject, vreq, policyHelper);
        statements = new ArrayList<ObjectPropertyStatementTemplateModel>();
        
        if (populatedObjectPropertyList.contains(op)) {
            log.debug("Getting data for populated object property " + op.getURI());
            
            /* Get the data */
            List<Map<String, String>> statementData = getStatementData();
            
            /* Apply postprocessing */
            postprocess(statementData);
            
            /* Put into data structure to send to template */            
            String objectKey = getObjectKey();
            for (Map<String, String> map : statementData) {
                statements.add(new ObjectPropertyStatementTemplateModel(subjectUri, 
                        propertyUri, objectKey, map, policyHelper, getTemplateName(), vreq));
            }
            
            postprocessStatementList(statements);
        } else {
            log.debug("Object property " + getUri() + " is unpopulated.");
        }
    }
    
    @Override
    protected boolean isEmpty() {
        return statements.isEmpty();
    }
    
    /* Template properties */

    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    
    @Override
    public boolean isCollatedBySubclass() {
        return false;
    }
    
    /* Template methods */
    
    public ObjectPropertyStatementTemplateModel first() {
        return ( (statements == null || statements.isEmpty()) ) ? null : statements.get(0);
    }
}
