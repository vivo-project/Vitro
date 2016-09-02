/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.DropDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt.EditDataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.RdfLiteralHash;


public class DataPropertyStatementTemplateModel extends PropertyStatementTemplateModel {
    private static final Log log = LogFactory.getLog(DataPropertyStatementTemplateModel.class); 
    
    private final Literal literalValue;
    private final String deleteUrl;
    private final String editUrl;
    private final String templateName;

    //Extended to include vitro request to check for special parameters
    public DataPropertyStatementTemplateModel(String subjectUri, Property property, Literal literal,
            String templateName, VitroRequest vreq) {
        
        super(subjectUri, property, vreq);
        
        this.literalValue = literal;
        this.templateName = templateName;

        // Do delete url first, since used in building edit url
        this.deleteUrl = makeDeleteUrl();            
        this.editUrl = makeEditUrl();       
    }
    
	private String makeDeleteUrl() {
        // Determine whether the statement can be deleted
		DataPropertyStatement dps = makeStatement();
        RequestedAction action = new DropDataPropertyStatement(vreq.getJenaOntModel(), dps);
        if ( ! PolicyHelper.isAuthorizedForActions(vreq, action) ) {
            return "";
        }
        
        ParamMap params = new ParamMap(
                "subjectUri", subjectUri,
                "predicateUri", property.getURI(),
                "datapropKey", makeHash(dps),
                "cmd", "delete");
        
        params.put("templateName", templateName);
        params.putAll(UrlBuilder.getModelParams(vreq));
        
        return UrlBuilder.getUrl(EDIT_PATH, params);
	}

	private String makeEditUrl() {
        // vitro:moniker is deprecated. We display existing data values so editors can 
        // move them to other properties and delete, but don't allow editing.
        if ( VitroVocabulary.MONIKER.equals(property.getURI()) ) {
            return "";           
        }
        
        // Determine whether the statement can be edited
		DataPropertyStatement dps = makeStatement();
        RequestedAction action = new EditDataPropertyStatement(vreq.getJenaOntModel(), dps);
        if ( ! PolicyHelper.isAuthorizedForActions(vreq, action) ) {
            return "";
        }
        
        ParamMap params = new ParamMap(
                "subjectUri", subjectUri,
                "predicateUri", property.getURI(),
                "datapropKey", makeHash(dps));
        
        if ( deleteUrl.isEmpty() ) {
            params.put("deleteProhibited", "prohibited");
        }
        
        params.putAll(UrlBuilder.getModelParams(vreq));
        
        return UrlBuilder.getUrl(EDIT_PATH, params);             
	}
        
	private DataPropertyStatement makeStatement() {
		DataPropertyStatement dps = new DataPropertyStatementImpl(subjectUri, property.getURI(), literalValue.getLexicalForm());
		// Language and datatype are needed to get the correct hash value
		dps.setLanguage(literalValue.getLanguage());
		dps.setDatatypeURI(literalValue.getDatatypeURI());
		return dps;
	}

	private String makeHash(DataPropertyStatement dps) {
        // Language and datatype are needed to get the correct hash value
        return String.valueOf(RdfLiteralHash.makeRdfLiteralHash(dps));
	}

    /* Template properties */
    
    public String getValue() {
        //attempt to strip any odd HTML
        return cleanTextForDisplay( literalValue.getLexicalForm() );
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
