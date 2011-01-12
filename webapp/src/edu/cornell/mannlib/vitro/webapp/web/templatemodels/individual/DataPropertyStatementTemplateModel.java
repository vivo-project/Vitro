/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.RdfLiteralHash;

public class DataPropertyStatementTemplateModel extends PropertyStatementTemplateModel {
    
    private static final Log log = LogFactory.getLog(DataPropertyStatementTemplateModel.class); 
    private static final String EDIT_PATH = "edit/editDatapropStmtRequestDispatch.jsp";  
    
    private Literal value;
    
    // Used for editing
    private String dataPropHash = null;

    DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, 
            Literal value, EditingPolicyHelper policyHelper) {
        super(subjectUri, propertyUri, policyHelper);
        
        this.value = value;
        setEditAccess(value, policyHelper);

    }
    
    DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, VitroRequest vreq, EditingPolicyHelper policyHelper) {
        super(subjectUri, propertyUri, policyHelper);
        
        DataPropertyStatementDao dpsDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
        List<Literal> values = dpsDao.getDataPropertyValuesForIndividualByProperty(subjectUri, propertyUri);
        
        value = values.get(0);
        setEditAccess(value, policyHelper);
        
    }
    
    private void setEditAccess(Literal value, EditingPolicyHelper policyHelper) {
        
        if (policyHelper != null) { // we're editing         
            DataPropertyStatement dps = new DataPropertyStatementImpl(subjectUri, propertyUri, value.getLexicalForm());
            // Language and datatype are needed to get the correct hash value
            dps.setLanguage(value.getLanguage());
            dps.setDatatypeURI(value.getDatatypeURI());
            this.dataPropHash = String.valueOf(RdfLiteralHash.makeRdfLiteralHash(dps));
            
            // Determine whether the statement can be edited
            RequestedAction action = new EditDataPropStmt(dps);
            if (policyHelper.isAuthorizedAction(action)) {
                markEditable();
            }      
            
            // Determine whether the statement can be deleted
            if ( ! propertyUri.equals(VitroVocabulary.LABEL)) {
                action = new DropDataPropStmt(dps);
                if (policyHelper.isAuthorizedAction(action)) {
                    markDeletable();
                } 
            }
        }        
    }
    
    
    /* Access methods for templates */
    
    public String getValue() {
        return value.getLexicalForm();
    }
    
    public String getEditUrl() {
        String editUrl = "";
        if (isEditable()) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "datapropKey", dataPropHash);
            if (! isDeletable()) {
                params.put("deleteProhibited", "prohibited");
            }
            //
            if (propertyUri.equals(VitroVocabulary.LABEL)) {
                params.put("vitroNsProp", "true");
            }
            editUrl = UrlBuilder.getUrl(EDIT_PATH, params);    
        }
        return editUrl;
    }
    
    public String getDeleteUrl() {
        String deleteUrl = "";
        if (isDeletable()) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "datapropKey", dataPropHash,
                    "cmd", "delete");
            deleteUrl = UrlBuilder.getUrl(EDIT_PATH, params);
        }
        return deleteUrl;
    }

}
