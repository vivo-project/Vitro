/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class ObjectPropertyStatementTemplateModel extends PropertyStatementTemplateModel {
    private static final Log log = LogFactory.getLog(ObjectPropertyStatementTemplateModel.class); 
    
    private final Map<String, String> data;
    
    private final String objectUri;
    private final String templateName;
    private final String objectKey;
    private final String editUrl;
    private final String deleteUrl;
    
    public ObjectPropertyStatementTemplateModel(String subjectUri, ObjectProperty property, String objectKey, 
            Map<String, String> data, String templateName, VitroRequest vreq) {
        super(subjectUri, property, vreq);

        this.data = Collections.unmodifiableMap(new HashMap<String, String>(data));
        this.objectUri = data.get(objectKey);        
        this.templateName = templateName;
        //to keep track of later
        this.objectKey = objectKey;
        
        // Do delete url first, since it is used in building edit url
        this.deleteUrl = makeDeleteUrl();
        this.editUrl = makeEditUrl();
    }

	private String makeDeleteUrl() {
    	// Is the delete link suppressed for this property?
    	if (property.isDeleteLinkSuppressed()) {
    		return "";
    	}
        
        // Determine whether the statement can be deleted
		RequestedAction action = new DropObjectPropertyStatement(
				vreq.getJenaOntModel(), subjectUri, property, objectUri);
        if ( ! PolicyHelper.isAuthorizedForActions(vreq, action) ) {
            return "";
        }
        
        if (VitroVocabulary.IND_MAIN_IMAGE.equals(property.getURI())) {
            return ObjectPropertyTemplateModel.getImageUploadUrl(subjectUri, "delete");
        } 
        
        ParamMap params = new ParamMap(
                "subjectUri", subjectUri,
                "predicateUri", property.getURI(),
                "objectUri", objectUri,
                "cmd", "delete",
                "objectKey", objectKey);
            
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
        
        if (property!= null && property.getDomainVClassURI() != null) {
            params.put("domainUri", property.getDomainVClassURI());
        }
        if (property!= null && property.getRangeVClassURI() != null) {
            params.put("rangeUri", property.getRangeVClassURI());
        }
        
        params.put("templateName", templateName);
        params.putAll(UrlBuilder.getModelParams(vreq));
        
        return UrlBuilder.getUrl(EDIT_PATH, params);
	}

	private String makeEditUrl() {
    	// Is the edit link suppressed for this property?
    	if (property.isEditLinkSuppressed()) {
    		return "";
    	}
        
       // Determine whether the statement can be edited
        RequestedAction action =  new EditObjectPropertyStatement(vreq.getJenaOntModel(), subjectUri, property, objectUri);
        if ( ! PolicyHelper.isAuthorizedForActions(vreq, action) ) {
            return "";
        }
        
        if (VitroVocabulary.IND_MAIN_IMAGE.equals(property.getURI())) {
            return ObjectPropertyTemplateModel.getImageUploadUrl(subjectUri, "edit");
        } 

        ParamMap params = new ParamMap(
                "subjectUri", subjectUri,
                "predicateUri", property.getURI(),
                "objectUri", objectUri);
        
        if ( deleteUrl.isEmpty() ) {
            params.put("deleteProhibited", "prohibited");
        }
        
        if (property!= null && property.getDomainVClassURI() != null) {
            params.put("domainUri", property.getDomainVClassURI());
        }
        if (property!= null && property.getRangeVClassURI() != null) {
            params.put("rangeUri", property.getRangeVClassURI());
        }
        
        params.putAll(UrlBuilder.getModelParams(vreq));
        
        return UrlBuilder.getUrl(EDIT_PATH, params);
	}
    
    /* Template methods */

    public Object get(String key) {
        return cleanTextForDisplay( data.get(key) );
    }
  
    public String uri(String key) {
    	return cleanURIForDisplay(data.get(key));
    }
    
    //Adding this method to enable retrieval of the entire data map
    public Map<String, String> getAllData() {
    	return data;
    }

	@Override
	public String getDeleteUrl() {
		return deleteUrl;
	}
  
	@Override
	public String getEditUrl() {
		return editUrl;
	}

}
