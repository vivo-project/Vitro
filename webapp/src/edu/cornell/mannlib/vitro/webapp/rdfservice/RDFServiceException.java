/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice;

public class RDFServiceException extends Exception {

	public RDFServiceException() {
		super();
	}
	
	public RDFServiceException(Throwable cause) {
	    super(cause);
	}
	
	public RDFServiceException(String message) {
		super(message);
	}
	
    public RDFServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
