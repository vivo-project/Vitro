/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract any summary information from an HTML output file, produced by a test
 * suite.
 */
public class SuiteStats {
	/**
	 * If the file doesn't contain a line that includes this pattern, it is not
	 * a suite output file.
	 */
	private static final Pattern TITLE_LINE_PATTERN = Pattern
			.compile("<title>Test suite results</title>");

	/**
	 * A test line looks something like this example:
	 */
	public static final String EXAMPLE_TEST_LINE = ""
			+ "<pre><tr class=\"  status_passed\"><td><a href=\"#testresult0\">MyTest</a></td></tr></pre>";

	/**
	 * So here is the pattern to match it:
	 */
	private static final Pattern TEST_LINE_PATTERN = Pattern
			.compile("<tr class=\"\\s*(\\w+)\"><td><a href=\"(#\\w+)\">([^<]*)</a></td></tr>");

	/**
	 * Parse the fields from this file and attempt to produce a
	 * {@link SuiteStats} object. If this is not an appropriate file, just
	 * return null.
	 */
	public static SuiteStats parse(SeleniumRunnerParameters parms,
			File outputFile) {
		IgnoredTests ignoredTests = parms.getIgnoredTests();

		boolean isSuiteOutputFile = false;
		Status status = Status.ERROR;

		List<TestInfo> tests = new ArrayList<TestInfo>();
		String suiteName = FileHelper.baseName(outputFile);
		String outputLink = outputFile.getName();

		BufferedReader reader = null;
		String line;
		try {
			reader = new BufferedReader(new FileReader(outputFile));
			while (null != (line = reader.readLine())) {
				if (TITLE_LINE_PATTERN.matcher(line).find()) {
					isSuiteOutputFile = true;
				}

				Matcher m;
				m = TEST_LINE_PATTERN.matcher(line);
				if (m.matches()) {
					String testName = m.group(3);
					String testLink = outputLink + m.group(2);

					Status testStatus;
					String reasonForIgnoring;
					if ("status_passed".equals(m.group(1))) {
						testStatus = Status.OK;
						reasonForIgnoring = "";
					} else if (ignoredTests.isIgnored(suiteName, testName)) {
						testStatus = Status.WARN;
						reasonForIgnoring = ignoredTests.getReasonForIgnoring(
								suiteName, testName);
					} else {
						testStatus = Status.ERROR;
						reasonForIgnoring = "";
					}

					tests.add(new TestInfo(testName, suiteName, testLink,
							testStatus, reasonForIgnoring));
				}
			}

			status = Status.OK;
			for (TestInfo t : tests) {
				if (t.status == Status.ERROR) {
					status = Status.ERROR;
				} else if ((t.status == Status.WARN) && (status == Status.OK)) {
					status = Status.WARN;
				}
			}

			if (isSuiteOutputFile) {
				return new SuiteStats(suiteName, outputLink, tests, status);
			} else {
				return null;
			}

		} catch (IOException e) {
			// Can't give up - I need to create as much output as I can.
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final String suiteName;
	private final String outputLink;
	private final List<TestInfo> tests;
	private final Status status;

	public SuiteStats(String suiteName, String outputLink,
			List<TestInfo> tests, Status status) {
		this.suiteName = suiteName;
		this.outputLink = outputLink;
		this.tests = tests;
		this.status = status;
	}

	public String getName() {
		return suiteName;
	}

	public Status getStatus() {
		return status;
	}

	public String getOutputLink() {
		return outputLink;
	}

	public Collection<TestInfo> getTests() {
		return Collections.unmodifiableCollection(tests);
	}

	public static class TestInfo {
		private final String name;
		private final String suite;
		private final String outputLink;
		private final Status status;
		private final String reasonForIgnoring;

		public TestInfo(String name, String suite, String outputLink,
				Status status, String reasonForIgnoring) {
			this.name = name;
			this.suite = suite;
			this.outputLink = outputLink;
			this.status = status;
			this.reasonForIgnoring = reasonForIgnoring;
		}

		public Status getStatus() {
			return status;
		}

		public String getSuiteName() {
			return suite;
		}

		public String getTestName() {
			return name;
		}

		public String getOutputLink() {
			return outputLink;
		}

		public String getReasonForIgnoring() {
			return reasonForIgnoring;
		}
	}

}
