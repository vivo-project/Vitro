/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

public class BlankNodeStatementListener extends StatementListener {

	private static final Log log = LogFactory.getLog(BlankNodeStatementListener.class);
	private Model bnodeModel;          
	
	public BlankNodeStatementListener(Model bnodeModel) { 
		this.bnodeModel = bnodeModel;
	}
		
	@Override
	public void addedStatement(Statement stmt) {
        
		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
			bnodeModel.add(stmt);
        } 
	}
	
	@Override
	public void removedStatement(Statement stmt) {

		if (stmt.getSubject().isAnon() || stmt.getObject().isAnon()) {
			bnodeModel.remove(stmt);
        } 
	}
}
