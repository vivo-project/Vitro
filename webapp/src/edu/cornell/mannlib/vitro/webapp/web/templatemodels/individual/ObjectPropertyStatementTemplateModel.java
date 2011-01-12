/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class ObjectPropertyStatementTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyStatementTemplateModel.class); 
    
    private static final String EDIT_PATH = "edit/editRequestDispatch.jsp";
    
    private static enum EditAccess {
        EDIT, DELETE;
    }

    private Map<String, String> data;
    
    // Used for editing
    private String subjectUri = null; 
    private String propertyUri = null;
    private String objectUri = null;
    private List<EditAccess> editAccessList;

    ObjectPropertyStatementTemplateModel(String subjectUri, String propertyUri, 
            String objectKey, Map<String, String> data, EditingPolicyHelper policyHelper) {

        this.data = data;
        
        // If the policyHelper is non-null, we are in edit mode, so create the list of editing permissions.
        // We do this now rather than in getEditUrl() and getDeleteUrl(), because getEditUrl() also needs to know
        // whether a delete is allowed.
        if (policyHelper != null) {
            this.subjectUri = subjectUri;
            this.propertyUri = propertyUri;
            objectUri = data.get(objectKey);
            editAccessList = new ArrayList<EditAccess>();
            ObjectPropertyStatement objectPropertyStatement = new ObjectPropertyStatementImpl(subjectUri, propertyUri, objectUri);
            
            // Determine whether the statement can be edited
            RequestedAction action =  new EditObjPropStmt(objectPropertyStatement);
            if (policyHelper.isAuthorizedAction(action)) {
                editAccessList.add(EditAccess.EDIT);
            }
            
            // Determine whether the statement can be deleted
            action = new DropObjectPropStmt(subjectUri, propertyUri, objectUri);
            if (policyHelper.isAuthorizedAction(action)) {    
                editAccessList.add(EditAccess.DELETE);
            }
        }
    }

    
    /* Access methods for templates */

    public Object get(String key) {
        return data.get(key);
    }
    
    public String getEditUrl() {
        String editUrl = "";
        if (editAccessList.contains(EditAccess.EDIT)) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "objectUri", objectUri);
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
                    "objectUri", objectUri,
                    "cmd", "delete");
            deleteUrl = UrlBuilder.getUrl(EDIT_PATH, params);
        }
        return deleteUrl;
    }
}
