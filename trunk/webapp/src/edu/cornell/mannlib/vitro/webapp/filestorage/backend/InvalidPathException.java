/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

/**
 * Indicates a PairTree path ("ppath" or "relative path") that is not correctly
 * formed, and cannot be converted to an object ID.
 */
public class InvalidPathException extends RuntimeException {

	public InvalidPathException() {
		super();
	}

	public InvalidPathException(String message) {
		super(message);
	}

	public InvalidPathException(Throwable cause) {
		super(cause);
	}

	public InvalidPathException(String message, Throwable cause) {
		super(message, cause);
	}

}
