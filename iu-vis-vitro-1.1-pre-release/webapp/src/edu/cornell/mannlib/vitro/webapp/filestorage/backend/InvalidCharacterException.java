/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.backend;

/**
 * Indicates that an object ID contains an invalid character.
 */
public class InvalidCharacterException extends RuntimeException {
	private final char invalid;
	private final int position;
	private final String context;


	public InvalidCharacterException(char invalid, int position, String context) {
		this.invalid = invalid;
		this.position = position;
		this.context = context;
	}


	@Override
	public String getMessage() {
		return String.format(
				"Invalid character '%1$c'(0x%1$x) at position %2$d in '%3$s'",
				(int)invalid, position, context);
	}
}
