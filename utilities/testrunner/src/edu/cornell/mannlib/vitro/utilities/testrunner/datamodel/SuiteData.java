/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.datamodel;

import java.util.LinkedHashMap;
import java.util.Map;

import edu.cornell.mannlib.vitro.utilities.testrunner.Status;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.OutputDataListener.ProcessOutput;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults.TestResults;

/**
 * What do we know about this suite, both before it runs and after it has run?
 */
public class SuiteData {
	/**
	 * If this suite has failure messages, the output link is to an anchor on
	 * the same page.
	 */
	public static String failureMessageAnchor(SuiteData s) {
		return "suiteFailure_" + s.getName();
	}

	private final String name;
	private final Status status;
	private final String outputLink;
	private final ProcessOutput failureMessages;

	/**
	 * This map iterates according to the order that the tests were specified in
	 * the suite file.
	 */
	private final Map<String, TestData> testMap;

	public SuiteData(String name, boolean ignored, SuiteContents contents,
			SuiteResults results, ProcessOutput failureMessages) {
		this.name = name;
		this.failureMessages = failureMessages;

		if (ignored) {
			this.status = Status.IGNORED;
			this.outputLink = null;
			this.testMap = buildTestMap(contents, results);
		} else if (failureMessages != null) {
			this.status = Status.ERROR;
			this.outputLink = "#" + failureMessageAnchor(this);
			this.testMap = buildTestMap(contents, results);
		} else if (results != null) {
			this.testMap = buildTestMap(contents, results);
			this.status = buildStatusFromTestMap();
			this.outputLink = results.getOutputLink();
		} else {
			this.status = Status.PENDING;
			this.outputLink = null;
			this.testMap = buildTestMap(contents, results);
		}
	}

	/**
	 * Build the test map. Do we have test results, or only the advance list of
	 * tests?
	 */
	private Map<String, TestData> buildTestMap(SuiteContents contents,
			SuiteResults results) {
		if (results == null) {
			return buildTestMapFromContents(contents);
		} else {
			return buildTestMapFromResults(contents, results);
		}
	}

	/**
	 * All we have to build from is the contents of the Suite HTML file.
	 */
	private Map<String, TestData> buildTestMapFromContents(
			SuiteContents contents) {
		Map<String, TestData> map = new LinkedHashMap<String, TestData>();
		for (String testName : contents.getTestNames()) {
			map.put(testName, new TestData(testName, this.name, this.status,
					null));
		}
		return map;
	}

	/**
	 * We can build from both the contents of the Suite HTML file and from the
	 * test results output file.
	 */
	private Map<String, TestData> buildTestMapFromResults(
			SuiteContents contents, SuiteResults results) {
		Map<String, TestData> map = new LinkedHashMap<String, TestData>();
		for (String testName : contents.getTestNames()) {
			TestResults testResult = results.getTest(testName);
			if (testResult == null) {
				// This shouldn't happen. How do we show it?
				map.put(testName, new TestData(testName, this.name,
						Status.PENDING, null));
			} else {
				map.put(testName,
						new TestData(testName, this.name, testResult
								.getStatus(), testResult.getOutputLink()));
			}
		}
		return map;
	}

	/**
	 * The suite ran to completion, so its status is the worst of the individual
	 * test statuses.
	 */
	private Status buildStatusFromTestMap() {
		Status status = Status.OK;
		for (TestData t : this.testMap.values()) {
			status = Status.combine(status, t.getStatus());
		}
		return status;
	}

	public String getName() {
		return name;
	}

	public Status getStatus() {
		return status;
	}

	public String getOutputLink() {
		return outputLink;
	}

	public ProcessOutput getFailureMessages() {
		return failureMessages;
	}

	public Map<String, TestData> getTestMap() {
		return testMap;
	}

	/**
	 * What do we know about this test, both before it runs and after it has
	 * run?
	 */
	public static class TestData {
		private final String testName;
		private final String suiteName;
		private final Status status;
		private final String outputLink;

		public TestData(String testName, String suiteName, Status status,
				String outputLink) {
			this.testName = testName;
			this.suiteName = suiteName;
			this.status = status;
			this.outputLink = outputLink;
		}

		public String getTestName() {
			return testName;
		}

		public String getSuiteName() {
			return suiteName;
		}

		public Status getStatus() {
			return status;
		}

		public String getOutputLink() {
			return outputLink;
		}

		@Override
		public String toString() {
			return "TestData[testName=" + testName + ", suiteName=" + suiteName
					+ ", status=" + status + ", outputLink=" + outputLink + "]";
		}

	}
}
