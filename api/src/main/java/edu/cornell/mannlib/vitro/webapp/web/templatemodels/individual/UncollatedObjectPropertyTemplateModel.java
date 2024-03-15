/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.customlistview.InvalidConfigurationException;

public class UncollatedObjectPropertyTemplateModel extends ObjectPropertyTemplateModel {

    private static final Log log = LogFactory.getLog(UncollatedObjectPropertyTemplateModel.class);

    private final List<ObjectPropertyStatementTemplateModel> statements;

    UncollatedObjectPropertyTemplateModel(ObjectProperty op, Individual subject,
            VitroRequest vreq, boolean editing,
            List<ObjectProperty> populatedObjectPropertyList)
        throws InvalidConfigurationException {

        super(op, subject, vreq, editing);
        statements = new ArrayList<ObjectPropertyStatementTemplateModel>();

        if (populatedObjectPropertyList.contains(op)) {
            log.debug("Getting data for populated object property " + op.getURI());

            /* Get the data */
            List<Map<String, String>> statementData;
            if (op instanceof FauxPropertyWrapper) {
            	statementData = getUnfilteredStatementData();
            } else {
            	statementData = getStatementData();	
            }

            /* Apply postprocessing */
            postprocess(statementData);

            /* Put into data structure to send to template */
            String objectKey = getObjectKey();
            for (Map<String, String> map : statementData) {
                String objectUri = map.get(objectKey);
                if (isAuthorizedToDisplay(vreq, objectUri)) {
                    statements.add(new ObjectPropertyStatementTemplateModel(subjectUri,
                            op, objectKey, map, getTemplateName(), vreq));    
                }
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
