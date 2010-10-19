/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

/**
 * Status for each test, each suite, and the entire run.
 */
public enum Status {
	/** All tests passed, and there were no warnings or errors. */
	OK("good"),

	/**
	 * One or more tests have not been run yet.
	 */
	PENDING("pending"),

	/**
	 * Will not run because it is ignored, or has run and failed but the failure
	 * is ignored.
	 */
	IGNORED("fair"),

	/**
	 * A test failed and could not be ignored, or an error message was
	 * generated.
	 */
	ERROR("bad");

	private final String htmlClass;

	private Status(String htmlClass) {
		this.htmlClass = htmlClass;
	}

	public String getHtmlClass() {
		return this.htmlClass;
	}

	/** When combined, the more severe status (defined later) takes precedence. */
	public static Status combine(Status s1, Status s2) {
		if (s1.compareTo(s2) > 0) {
			return s1;
		} else {
			return s2;
		}
	}

	/** Anything except ERROR is considered to be a success. */
	public static boolean isSuccess(Status status) {
		return status != Status.ERROR;
	}
}
