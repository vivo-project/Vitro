package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class InsertException extends Exception {

	public InsertException() {
		super();
	}
	
	public InsertException(String message) {
		super(message);
	}
	
	public InsertException(Throwable cause) {
		super(cause);
	}
	
	public InsertException(String message, Throwable cause) {
		super(message,cause);
	}

}
