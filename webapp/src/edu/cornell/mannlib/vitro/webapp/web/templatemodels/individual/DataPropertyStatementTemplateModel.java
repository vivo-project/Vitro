/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.HashMap;
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
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;

public class DataPropertyStatementTemplateModel extends PropertyStatementTemplateModel {
    
    private static final Log log = LogFactory.getLog(DataPropertyStatementTemplateModel.class); 
    private static final String EDIT_PATH = "edit/editDatapropStmtRequestDispatch.jsp";  
    
    protected String value;
    
    // Used for editing
    protected String dataPropHash;
   
    //Extended to include vitro request to check for special parameters
    DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, 
            Literal literal, EditingPolicyHelper policyHelper, VitroRequest vreq) {
        super(subjectUri, propertyUri, policyHelper, vreq);
        
        //attempt to strip any odd HTML
        this.value = cleanTextForDisplay( literal.getLexicalForm() );
        
        setEditAccess(literal, policyHelper, propertyUri);
       
    }
    
    /*
     * This method handles the special case where we are creating a DataPropertyStatementTemplateModel outside the GroupedPropertyList.
     * Specifically, it allows rdfs:label to be treated like a data property statement and thus have editing links. It is not possible
     * to handle rdfs:label like vitro links and vitroPublic image, because it is not possible to construct a DataProperty from
     * rdfs:label.
     */
    DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, VitroRequest vreq, EditingPolicyHelper policyHelper) {
        super(subjectUri, propertyUri, policyHelper, vreq);
        DataPropertyStatementDao dpsDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
        List<Literal> literals = dpsDao.getDataPropertyValuesForIndividualByProperty(subjectUri, propertyUri);
        
        // Make sure the subject has a value for this property 
        if (literals.size() > 0) {
            Literal literal = literals.get(0);
            value = literal.getLexicalForm();
            setEditAccess(literal, policyHelper, propertyUri);
        } 
    }
    
    protected void setValue(String value) {
        this.value = value;
    }
    
    protected void setEditAccess(Literal value, EditingPolicyHelper policyHelper, String propertyUri) {
        
        if (policyHelper != null) { // we're editing         
            
            DataPropertyStatement dps = new DataPropertyStatementImpl(subjectUri, propertyUri, value.getLexicalForm());
            // Language and datatype are needed to get the correct hash value
            dps.setLanguage(value.getLanguage());
            dps.setDatatypeURI(value.getDatatypeURI());
            this.dataPropHash = String.valueOf(RdfLiteralHash.makeRdfLiteralHash(dps));
            
            // Determine whether the statement can be edited
            RequestedAction action = new EditDataPropStmt(dps);
            // vitro:moniker is deprecated. We display existing data values so editors can move them to other properties
            // and delete, but don't allow editing.
            if ( ( ! propertyUri.equals(VitroVocabulary.MONIKER) ) && policyHelper.isAuthorizedAction(action)) {
                markEditable();
            }      
            
            // Determine whether the statement can be deleted
            // Hack for rdfs:label - the policy doesn't prevent deletion.
            if ( ! propertyUri.equals(VitroVocabulary.LABEL) ) {
                action = new DropDataPropStmt(dps);
                if (policyHelper.isAuthorizedAction(action)) {
                    markDeletable();
                } 
            }
        }        
    }
    
    
    /* Access methods for templates */
    
    public String getValue() {
        return value;
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
            
            //Check if special parameters being sent
            HashMap<String, String> specialParams = UrlBuilder.getSpecialParams(vreq);
            if(specialParams.size() > 0) {
            	params.putAll(specialParams);
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
            
            //Check if special parameters being sent
            HashMap<String, String> specialParams = UrlBuilder.getSpecialParams(vreq);
            if(specialParams.size() > 0) {
            	params.putAll(specialParams);
            }
            
            deleteUrl = UrlBuilder.getUrl(EDIT_PATH, params);
        }
        return deleteUrl;
    }

}
