/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.modules.searchEngine;

/**
 * Indicates that a request to the SearchEngine has timed out, or given some
 * other indication that no response will be coming.
 */
public class SearchEngineNotRespondingException extends SearchEngineException {

	public SearchEngineNotRespondingException() {
		super();
	}

	public SearchEngineNotRespondingException(String message) {
		super(message);
	}

	public SearchEngineNotRespondingException(Throwable cause) {
		super(cause);
	}

	public SearchEngineNotRespondingException(String message, Throwable cause) {
		super(message, cause);
	}

}
