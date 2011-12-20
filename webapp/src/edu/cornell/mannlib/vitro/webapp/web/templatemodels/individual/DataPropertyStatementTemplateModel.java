/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
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
    private static final String EDIT_PATH = "editRequestDispatch";  
    
    protected String value;
   
    //Extended to include vitro request to check for special parameters
    public DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, 
            Literal literal, EditingPolicyHelper policyHelper, VitroRequest vreq) {
        super(subjectUri, propertyUri, policyHelper, vreq);
        
        //attempt to strip any odd HTML
        this.value = cleanTextForDisplay( literal.getLexicalForm() );

        setEditUrls(literal, policyHelper, propertyUri);      
    }
    
    /*
     * This method handles the special case where we are creating a DataPropertyStatementTemplateModel outside the GroupedPropertyList.
     * Specifically, it allows rdfs:label to be treated like a data property statement and thus have editing links. It is not possible
     * to handle rdfs:label like vitro links and vitroPublic image, because it is not possible to construct a DataProperty from
     * rdfs:label.
     */
    DataPropertyStatementTemplateModel(String subjectUri, String propertyUri, VitroRequest vreq, EditingPolicyHelper policyHelper) {
        super(subjectUri, propertyUri, policyHelper, vreq);
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    protected void setEditUrls(Literal value, EditingPolicyHelper policyHelper, String propertyUri) {

        if (policyHelper == null) {
            return;
        }     
            
        DataPropertyStatement dps = new DataPropertyStatementImpl(subjectUri, propertyUri, value.getLexicalForm());
        // Language and datatype are needed to get the correct hash value
        dps.setLanguage(value.getLanguage());
        dps.setDatatypeURI(value.getDatatypeURI());
        String dataPropHash = String.valueOf(RdfLiteralHash.makeRdfLiteralHash(dps));
            
        // Do delete url first, since used in building edit url
        setDeleteUrl(policyHelper, propertyUri, dps, dataPropHash);            
        setEditUrl(policyHelper, propertyUri, dps, dataPropHash);       
    }
    
    
    protected void setDeleteUrl(EditingPolicyHelper policyHelper, String propertyUri, DataPropertyStatement dps, String dataPropHash) {

        // Hack for rdfs:label - the policy doesn't prevent deletion.
        if (propertyUri.equals(VitroVocabulary.LABEL)) {
            return;
        }      

        // Determine whether the statement can be deleted
        RequestedAction action = new DropDataPropStmt(dps);
        if ( ! policyHelper.isAuthorizedAction(action) ) {
            return;
        }
        
        ParamMap params = new ParamMap(
                "subjectUri", subjectUri,
                "predicateUri", propertyUri,
                "datapropKey", dataPropHash,
                "cmd", "delete");
        
        params.putAll(UrlBuilder.getModelParams(vreq));
        
        deleteUrl = UrlBuilder.getUrl(EDIT_PATH, params);
    } 

    protected void setEditUrl(EditingPolicyHelper policyHelper, String propertyUri, DataPropertyStatement dps, String dataPropHash) {

        // vitro:moniker is deprecated. We display existing data values so editors can 
        // move them to other properties and delete, but don't allow editing.
        if ( propertyUri.equals(VitroVocabulary.MONIKER) ) {
            return;           
        }
        
        // Determine whether the statement can be edited
        RequestedAction action = new EditDataPropStmt(dps);
        if ( ! policyHelper.isAuthorizedAction(action) ) {
            return;
        }
        
        ParamMap params = new ParamMap(
                "subjectUri", subjectUri,
                "predicateUri", propertyUri,
                "datapropKey", dataPropHash);
        
        if ( deleteUrl.isEmpty() ) {
            params.put("deleteProhibited", "prohibited");
        }
        
        params.putAll(UrlBuilder.getModelParams(vreq));
        
        editUrl = UrlBuilder.getUrl(EDIT_PATH, params);             
    }
        
        
    /* Template properties */
    
    public String getValue() {
        return value;
    }

}
