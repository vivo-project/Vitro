/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

/**
 * Indicates a problem with a request to the SearchEngine.
 */
public class SearchEngineException extends Exception {

	public SearchEngineException() {
		super();
	}

	public SearchEngineException(String message) {
		super(message);
	}
	
	public SearchEngineException(Throwable cause) {
		super(cause);
	}
	
	public SearchEngineException(String message, Throwable cause) {
		super(message, cause);
	}

}
