/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

/**
 * Indicates a problem with the attempt to run a command in a sub-process.
 */
public class CommandRunnerException extends Exception {

	public CommandRunnerException() {
		super();
	}

	public CommandRunnerException(String message) {
		super(message);
	}

	public CommandRunnerException(Throwable cause) {
		super(cause);
	}

	public CommandRunnerException(String message, Throwable cause) {
		super(message, cause);
	}

}
