/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.datamodel;

import edu.cornell.mannlib.vitro.utilities.testrunner.Status;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.OutputDataListener.ProcessOutput;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults.TestResults;

/**
 * What do we know about this suite, both before it runs and after it has run?
 */
public class SuiteData {
	private final String name;
	private final boolean ignored;
	private final SuiteContents contents;
	private final SuiteResults results;
	private final ProcessOutput failureMessages;

	public SuiteData(String name, boolean ignored, SuiteContents contents,
			SuiteResults results, ProcessOutput failureMessages) {
		this.name = name;
		this.ignored = ignored;
		this.contents = contents;
		this.results = results;
		this.failureMessages = failureMessages;
	}

	public String getName() {
		return name;
	}

	public boolean isIgnored() {
		return ignored;
	}

	public SuiteContents getContents() {
		return contents;
	}

	public SuiteResults getResults() {
		return results;
	}

	public Status getSuiteStatus() {
		if (ignored) {
			return Status.WARN;
		}
		if (failureMessages != null) {
			return Status.ERROR;
		}
		if (results == null) {
			return Status.PENDING;
		}

		/*
		 * If we have results and no failure messages, scan the results for the
		 * worst status.
		 */
		Status status = Status.OK;
		for (TestResults t : results.getTests()) {
			status = Status.combine(status, t.getStatus());
		}
		return status;
	}
}
