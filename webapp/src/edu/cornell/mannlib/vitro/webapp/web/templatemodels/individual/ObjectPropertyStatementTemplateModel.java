/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class ObjectPropertyStatementTemplateModel extends BaseTemplateModel {
    
    private static final Log log = LogFactory.getLog(ObjectPropertyStatementTemplateModel.class); 
    
    private static final String EDIT_PATH = "edit/editRequestDispatch.jsp";
    
    // RY WE may want to instead store the ObjectPropertyStatement
    private String subjectUri; // we'll use these to make the edit links
    private String propertyUri;
    private String objectUri;
    private Map<String, String> data;
    private VitroRequest vreq;
    private ObjectPropertyStatement objectPropertyStatement;
    private EditLinkHelper editLinkHelper;

    ObjectPropertyStatementTemplateModel(String subjectUri, String propertyUri, 
            String objectKey, Map<String, String> data, VitroRequest vreq) {
        this.subjectUri = subjectUri;
        this.propertyUri = propertyUri;
        this.objectUri = data.get(objectKey);
        this.data = data;
        this.vreq = vreq;
        // Don't set these until needed (when edit links are requested)
        this.objectPropertyStatement = null;
        this.editLinkHelper = null;
    }
    
    private void doEditingLinkPrep() {
        // Assign the objectPropertyStatement and editLinkHelper to instance variables, so we don't
        // have to do it twice, once for edit link and once for delete link.
        if (objectPropertyStatement == null) {
            objectPropertyStatement = new ObjectPropertyStatementImpl(subjectUri, propertyUri, objectUri);
        }
        if (editLinkHelper == null) {
            editLinkHelper = new EditLinkHelper(vreq);
        }
    }
    
    /* Access methods for templates */

    public Object get(String key) {
        return data.get(key);
    }
    
    public String getEditUrl() {
        String editUrl = "";
        doEditingLinkPrep();
        RequestedAction action = new EditObjPropStmt(objectPropertyStatement);
        PolicyDecision decision = editLinkHelper.getPolicy().isAuthorized(editLinkHelper.getIds(), action);
        if (decision != null && decision.getAuthorized() == Authorization.AUTHORIZED) {
            ParamMap params = new ParamMap(
                    "subjectUri", subjectUri,
                    "predicateUri", propertyUri,
                    "objectUri", objectUri);
            editUrl = UrlBuilder.getUrl(EDIT_PATH, params);
        }
        
        return editUrl;
    }
    
    public String getDeleteUrl() {
        String deleteUrl = "";
        doEditingLinkPrep();
        return deleteUrl;
    }
}
