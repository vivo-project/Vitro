/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.http;

/**
 * Indicates an invalid Accept header. Either the basic syntax was flawed, or
 * the value for "q" could not be parsed to a Float.
 */
public class AcceptHeaderParsingException extends Exception {
	public AcceptHeaderParsingException(String message) {
		super(message);
	}

	public AcceptHeaderParsingException(String message, Throwable cause) {
		super(message, cause);
	}

}
