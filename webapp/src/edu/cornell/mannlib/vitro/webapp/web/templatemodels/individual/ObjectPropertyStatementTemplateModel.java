/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class ObjectPropertyStatementTemplateModel extends PropertyStatementTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyStatementTemplateModel.class); 
    
    private static final String EDIT_PATH = "edit/editRequestDispatch.jsp";

    private Map<String, String> data;
    
    // Used for editing
    private String objectUri = null;
    private String templateName = null;
    private VitroRequest vitroRequest = null;
    //Updating to include Vitro Request
    ObjectPropertyStatementTemplateModel(String subjectUri, String propertyUri, String objectKey, 
            Map<String, String> data, EditingPolicyHelper policyHelper, String templateName, VitroRequest vreq) {
        super(subjectUri, propertyUri, policyHelper);
        
        this.data = data;
        this.objectUri = data.get(objectKey);
        this.templateName = templateName;
        this.vitroRequest = vreq;
        setEditAccess(policyHelper);
    }

    /** 
     * This method handles the special case where we are creating a DataPropertyStatementTemplateModel 
     * outside the GroupedPropertyList. Specifically, it allows vitro:primaryLink and vitro:additionalLink 
     * to be treated like object property statements and thus have editing links. (In a future version, 
     * these properties will be replaced by vivo core ontology properties.) It could potentially be used 
     * for other properties outside the property list as well.
     */
    ObjectPropertyStatementTemplateModel(String subjectUri, String propertyUri, 
            VitroRequest vreq, EditingPolicyHelper policyHelper) {
        super(subjectUri, propertyUri, policyHelper); 
        vitroRequest = vreq;
    }

    private void setEditAccess(EditingPolicyHelper policyHelper) {
        // If the policyHelper is non-null, we are in edit mode, so create the list of editing permissions.
        // We do this now rather than in getEditUrl() and getDeleteUrl(), because getEditUrl() also needs to know
        // whether a delete is allowed.
        if (policyHelper != null) {
            ObjectPropertyStatement objectPropertyStatement = new ObjectPropertyStatementImpl(subjectUri, propertyUri, objectUri);
            
            // Determine whether the statement can be edited
            RequestedAction action =  new EditObjPropStmt(objectPropertyStatement);
            if (policyHelper.isAuthorizedAction(action)) {
                markEditable();
            }
            
            // Determine whether the statement can be deleted
            action = new DropObjectPropStmt(subjectUri, propertyUri, objectUri);
            if (policyHelper.isAuthorizedAction(action)) {    
                markDeletable();
            }
        }        
    }
    
    /* Access methods for templates */

    public Object get(String key) {
        return data.get(key);
    }
    
    public String getEditUrl() {
        String editUrl = "";
        if (isEditable()) {
            if (propertyUri.equals(VitroVocabulary.IND_MAIN_IMAGE)) {
                return ObjectPropertyTemplateModel.getImageUploadUrl(subjectUri, "edit");
            } 
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "objectUri", objectUri);
            if (! isDeletable()) {
                params.put("deleteProhibited", "prohibited");
            }
            //Check if special parameters being sent
            HashMap<String, String> specialParams = UrlBuilder.getSpecialParams(vitroRequest);
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
            if (propertyUri.equals(VitroVocabulary.IND_MAIN_IMAGE)) {
                return ObjectPropertyTemplateModel.getImageUploadUrl(subjectUri, "delete");
            } 
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "objectUri", objectUri,
                    "cmd", "delete");
            for ( String key : data.keySet() ) {
                String value = data.get(key);
                // Remove an entry with a null value instead of letting it get passed
                // as a param with an empty value, in order to align with behavior on
                // profile page. E.g., if statement.moniker is null, a test for 
                // statement.moniker?? will yield different results if null on the 
                // profile page but an empty string on the deletion page.
                if (value != null) {
                    params.put("statement_" + key, data.get(key));
                }
            }
            params.put("templateName", templateName);
            //Check if special parameters being sent
            HashMap<String, String> specialParams = UrlBuilder.getSpecialParams(vitroRequest);
            if(specialParams.size() > 0) {
            	params.putAll(specialParams);
            }
            deleteUrl = UrlBuilder.getUrl(EDIT_PATH, params);

        }
        return deleteUrl;
    }
}
