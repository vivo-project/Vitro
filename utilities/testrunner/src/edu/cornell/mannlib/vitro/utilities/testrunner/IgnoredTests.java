/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A list of tests to be ignored - they are expected to fail, and their failure
 * is logged with a warning, not an error.
 */
public class IgnoredTests {
	public static final IgnoredTests EMPTY_LIST = new IgnoredTests();

	private final String filePath;
	private final List<IgnoredTestInfo> tests;

	/**
	 * Create an empty instance.
	 */
	private IgnoredTests() {
		this.filePath = "NO FILE";
		this.tests = Collections.emptyList();
	}

	/**
	 * <p>
	 * Parse the file of ignored tests.
	 * </p>
	 * <p>
	 * Ignore any blank line, or any line starting with '#' or '!'
	 * </p>
	 * <p>
	 * Each other line describes an ignored test. The line contains the suite
	 * name, a comma (with optional space), the test name (with optional space)
	 * and optionally a comment, starting with a '#'.
	 * </p>
	 * If the test name is an asterisk '*', then the entire suite will be
	 * ignored.
	 * <p>
	 * </p>
	 */
	public IgnoredTests(File file) {
		this.filePath = file.getAbsolutePath();

		List<IgnoredTestInfo> tests = new ArrayList<IgnoredTestInfo>();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while (null != (line = reader.readLine())) {
				line = line.trim();
				if ((line.length() == 0) || (line.charAt(0) == '#')
						|| (line.charAt(0) == '!')) {
					continue;
				}
				Pattern p = Pattern.compile("([^,#]+),([^,#]+)(#(.*))?");
				Matcher m = p.matcher(line);
				if (m.matches()) {
					tests.add(new IgnoredTestInfo(m.group(1), m.group(2), m
							.group(4)));
				} else {
					throw new FatalException(
							"Bad format on ignored test description: '" + line
									+ "', should be "
									+ "<suite name>, <test name> [# comment]");
				}
			}
		} catch (IOException e) {
			throw new FatalException(
					"Failed to parse the list of ignored tests: '"
							+ file.getPath() + "'", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		this.tests = Collections.unmodifiableList(tests);
	}

	/**
	 * Get a copy of the whole list.
	 */
	public List<IgnoredTestInfo> getList() {
		return new ArrayList<IgnoredTestInfo>(tests);
	}

	/**
	 * Is this test ignored or not?
	 */
	public boolean isIgnored(String suiteName, String testName) {
		for (IgnoredTestInfo test : tests) {
			if (test.matches(suiteName, testName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Is this entire suite ignored?
	 */
	public boolean isIgnored(String suiteName) {
		for (IgnoredTestInfo test : tests) {
			if (test.matchesEntireSuite(suiteName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If this test is ignored, what is the reason? If not, return an empty
	 * string.
	 */
	public String getReasonForIgnoring(String suiteName, String testName) {
		for (IgnoredTestInfo test : tests) {
			if (test.matches(suiteName, testName)) {
				return test.comment;
			}
		}
		return "";
	}

	/**
	 * If this suite is ignored, what is the reason? If not, return an empty
	 * string.
	 */
	public String getReasonForIgnoring(String suiteName) {
		for (IgnoredTestInfo test : tests) {
			if (test.matchesEntireSuite(suiteName)) {
				return test.comment;
			}
		}
		return "";
	}

	public String toString() {
		String s = "  ignored tests from " + this.filePath + "\n";
		for (IgnoredTestInfo test : tests) {
			s += "      " + test.suiteName + ", " + test.testName + "\n";
		}
		return s;
	}

	/**
	 * Encapsulates a line from the file with suite name, test name, and
	 * comment.
	 */
	public static class IgnoredTestInfo {
		public final String suiteName;
		public final String testName;
		public final String comment;

		public IgnoredTestInfo(String suiteName, String testName, String comment) {
			this.suiteName = suiteName.trim();
			this.testName = testName.trim();
			this.comment = (comment == null) ? "" : comment.trim();
		}

		public boolean matches(String suiteName, String testName) {
			return this.suiteName.equals(suiteName)
					&& (this.testName.equals(testName) || this.testName
							.equals("*"));
		}

		public boolean matchesEntireSuite(String suiteName) {
			return this.suiteName.equals(suiteName)
					&& this.testName.equals("*");
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!obj.getClass().equals(this.getClass())) {
				return false;
			}
			IgnoredTestInfo that = (IgnoredTestInfo) obj;
			return this.suiteName.equals(that.suiteName)
					&& this.testName.equals(that.testName)
					&& this.comment.equals(that.comment);
		}

		@Override
		public int hashCode() {
			return suiteName.hashCode() ^ testName.hashCode()
					^ comment.hashCode();
		}

		@Override
		public String toString() {
			return "IgnoredTestInfo['" + suiteName + "', '" + testName + "', '"
					+ comment + "']";
		}

	}

}
