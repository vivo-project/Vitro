/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.http;

/**
 * Indicates that none of the available types are acceptable to the client.
 */
public class NotAcceptableException extends Exception {
	public NotAcceptableException(String message) {
		super(message);
	}

}
