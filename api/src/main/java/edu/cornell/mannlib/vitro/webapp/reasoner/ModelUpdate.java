/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import com.hp.hpl.jena.rdf.model.Statement;


public class ModelUpdate {

	public static enum Operation {ADD, RETRACT};
	
	private Operation operation;
	private Statement statement;
	private String modelURI;
	//JenaDataSourceSetupBase.JENA_DB_MODEL;
	//JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL;
	
	
	public ModelUpdate() {

    }

	public ModelUpdate(Statement statement,
			           Operation operation,
	                   String modelURI) {
		
		this.operation = operation;
		this.statement = statement;
		this.modelURI = modelURI;
    }

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	public String getModelURI() {
		return modelURI;
	}

	public void setModelURI(String modelURI) {
		this.modelURI = modelURI;
	}
	
	@Override public String toString() {
		String ret = "operation = " + this.operation + ",";
		ret += " model = " + this.modelURI + ",";
		ret += " statement = " + SimpleReasoner.stmtString(statement);
		
		
		return ret;
	}
}
