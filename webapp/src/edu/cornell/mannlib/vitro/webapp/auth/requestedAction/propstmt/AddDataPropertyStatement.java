/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import com.hp.hpl.jena.rdf.model.Literal;

/** Should we allow the user to add this DataPropertyStatement? */
public class AddDataPropertyStatement extends AbstractDataPropertyStatementAction {
    protected String data;
    protected String dataType;
    protected String lang;
    
    public AddDataPropertyStatement(String subjectUri, String predicateUri, String value, String dataType, String lang) {
        super(subjectUri, predicateUri);
        this.data= value;
        this.dataType = dataType;
        this.lang = lang;
    }

    public AddDataPropertyStatement(String subjectUri, String predicateUri, Literal literal) {
    	super(subjectUri, predicateUri);
    	this.data= literal.getValue().toString();
    	this.dataType = literal.getDatatypeURI();
    	this.lang = literal.getLanguage();
    }
    
    public String getData() {
        return data;
    }

    public String getDataType() {
        return dataType;
    }

    public String getLang() {
        return lang;
    }
}
