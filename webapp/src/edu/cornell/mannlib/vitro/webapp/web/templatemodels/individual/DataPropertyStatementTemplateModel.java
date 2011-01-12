/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class DataPropertyStatementTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(DataPropertyStatementTemplateModel.class); 
    private static final String EDIT_PATH = "edit/editDatapropStmtRequestDispatch.jsp";  
    
    private static enum EditAccess {
        EDIT, DELETE;
    }    
    
    private Literal value;
    
    // Used for editing
    private String subjectUri = null; 
    private String propertyUri = null;
    private List<EditAccess> editAccessList = null;
    private String dataPropHash = null;

    DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, 
            Literal value2, EditingPolicyHelper policyHelper) {
        
        this.value = value2;
        
        if (policyHelper != null) {
            this.subjectUri = subjectUri;
            this.propertyUri = propertyUri;
            
            DataPropertyStatement dps = new DataPropertyStatementImpl(subjectUri, propertyUri, value.getLexicalForm());
            // Language and datatype are needed to get the correct hash value
            dps.setLanguage(value.getLanguage());
            dps.setDatatypeURI(value.getDatatypeURI());
            this.dataPropHash = String.valueOf(RdfLiteralHash.makeRdfLiteralHash(dps));
            
            editAccessList = new ArrayList<EditAccess>(); 
            
            // Determine whether the statement can be edited
            RequestedAction action = new EditDataPropStmt(dps);
            if (policyHelper.isAuthorizedAction(action)) {
                editAccessList.add(EditAccess.EDIT);
            }      
            
            // Determine whether the statement can be deleted
            action = new DropDataPropStmt(dps);
            if (policyHelper.isAuthorizedAction(action)) {
                editAccessList.add(EditAccess.DELETE);
            } 
        }
    }
    
    /* Access methods for templates */
    
    public String getValue() {
        return value.getLexicalForm();
    }
    
    public String getEditUrl() {
        String editUrl = "";
        if (editAccessList.contains(EditAccess.EDIT)) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "datapropKey", dataPropHash);
            if (! editAccessList.contains(EditAccess.DELETE)) {
                params.put("deleteProhibited", "prohibited");
            }
            editUrl = UrlBuilder.getUrl(EDIT_PATH, params);    
        }
        return editUrl;
    }
    
    public String getDeleteUrl() {
        String deleteUrl = "";
        if (editAccessList.contains(EditAccess.DELETE)) {
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
