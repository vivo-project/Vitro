/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;

public class NameStatementTemplateModel extends
        DataPropertyStatementTemplateModel {
    
    private static final Log log = LogFactory.getLog(NameStatementTemplateModel.class); 

    /*
     * This method handles the special case where we are creating a DataPropertyStatementTemplateModel outside the GroupedPropertyList.
     * Specifically, it allows rdfs:label to be treated like a data property statement and thus have editing links. 
     */
    NameStatementTemplateModel(String subjectUri, VitroRequest vreq, EditingPolicyHelper policyHelper) {
        super(subjectUri, VitroVocabulary.LABEL, vreq, policyHelper);

        WebappDaoFactory wdf = vreq.getWebappDaoFactory();
        
        // NIHVIVO-2466 Use the same methods to get the label that are used elsewhere in the 
        // application, to guarantee consistent results for individuals with multiple labels
        // across the application. 
        IndividualDao iDao = wdf.getIndividualDao();
        EditLiteral literal = iDao.getLabelEditLiteral(subjectUri);
        
        if (literal != null) {
            value = cleanTextForDisplay( literal.getLexicalForm() );
            setEditUrls(literal, policyHelper, propertyUri);
        } else {
            // If the individual has no rdfs:label, use the local name. It will not be editable. (This replicates previous behavior;
            // perhaps we would want to allow a label to be added. But such individuals do not usually have their profiles viewed or
            // edited directly.)
            URI uri = new URIImpl(subjectUri);
            value = uri.getLocalName();       
        }
    }
    
}
