/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cornell.mannlib.vitro.utilities.testrunner.IgnoredTests.IgnoredTestInfo;

/**
 * Check that the ignored_tests.txt file is being parsed correctly.
 */
public class IgnoredTestsTest {
	private static final String TEST_WITH_REASON = "test with reason";
	private static final String TEST_NO_REASON = "test with no reason";
	private static final String TEST_NOT_IGNORED = "test not ignored";
	private static final String TEST_FROM_IGNORED_SUITE = "test from ignored suite";
	private static final String SUITE_WITH_TESTS = "suite with tests";
	private static final String SUITE_WITH_REASON = "entire suite with reason";
	private static final String SUITE_NO_REASON = "entire suite with no reason";
	private static final String SUITE_NOT_IGNORED = "entire suite not ignored";
	private static final String REASON_FOR_TEST = "the reason for the test";
	private static final String REASON_FOR_SUITE = "the reason for the suite";
	private static final String NO_REASON = "";

	private static final String FILE_CONTENTS = "# This line is a comment \n"
			+ "! This line is also, and so is the blank one\n"
			+ "\n"
			+ (SUITE_WITH_TESTS + ", " + TEST_NO_REASON + "\n")
			+ (SUITE_WITH_TESTS + ", " + TEST_WITH_REASON + " # "
					+ REASON_FOR_TEST + "\n") + (SUITE_NO_REASON + ", *\n")
			+ (SUITE_WITH_REASON + ", * # " + REASON_FOR_SUITE + "\n");

	private static final String BAD_CONTENTS = "suite but no test # doesn't match.";

	/*
	 * Ignore any blank line, or any line starting with '#' or '!' </p> <p> Each
	 * other line describes an ignored test. The line contains the suite name, a
	 * comma (with optional space), the test name (with optional space) and
	 * optionally a comment, starting with a '#'. </p> If the test name is an
	 * asterisk '*', then the entire suite will be ignored. <p>
	 */

	private static File ignoreFile;
	private static File badFile;

	@BeforeClass
	public static void initializeTheFile() throws IOException {
		ignoreFile = File.createTempFile("IgnoredTestsTest", "");

		Writer writer = new FileWriter(ignoreFile);
		try {
			writer.write(FILE_CONTENTS);
		} finally {
			writer.close();
		}
	}

	@BeforeClass
	public static void initializeBadFile() throws IOException {
		badFile = File.createTempFile("IgnoredTestsTest", "");

		Writer writer = new FileWriter(badFile);
		try {
			writer.write(BAD_CONTENTS);
		} finally {
			writer.close();
		}
	}

	@AfterClass
	public static void cleanup() throws IOException {
		ignoreFile.delete();
		badFile.delete();
	}

	private IgnoredTests ignored;

	@Before
	public void readTheFile() {
		ignored = new IgnoredTests(ignoreFile);
	}

	@Test(expected = FatalException.class)
	public void readBadFile() {
		new IgnoredTests(badFile);
	}

	@Test
	public void getList() {
		Set<IgnoredTestInfo> expected = new HashSet<IgnoredTestInfo>();
		expected.add(new IgnoredTestInfo(SUITE_WITH_TESTS, TEST_NO_REASON,
				NO_REASON));
		expected.add(new IgnoredTestInfo(SUITE_WITH_TESTS, TEST_WITH_REASON,
				REASON_FOR_TEST));
		expected.add(new IgnoredTestInfo(SUITE_NO_REASON, "*", NO_REASON));
		expected.add(new IgnoredTestInfo(SUITE_WITH_REASON, "*",
				REASON_FOR_SUITE));
		Set<IgnoredTestInfo> actual = new HashSet<IgnoredTestInfo>(
				ignored.getList());
		assertEquals("list of tests", expected, actual);
	}

	@Test
	public void isIgnoredTestYes() {
		assertTrue("ignored test",
				ignored.isIgnored(SUITE_WITH_TESTS, TEST_NO_REASON));
	}

	@Test
	public void isIgnoredTestNo() {
		assertFalse("not ignored test",
				ignored.isIgnored(SUITE_WITH_TESTS, TEST_NOT_IGNORED));
	}

	@Test
	public void isIgnoredTestFromSuite() {
		assertTrue("test from ignored suite",
				ignored.isIgnored(SUITE_WITH_REASON, TEST_FROM_IGNORED_SUITE));
	}

	@Test
	public void getReasonTestYes() {
		assertEquals(
				"test with reason",
				REASON_FOR_TEST,
				ignored.getReasonForIgnoring(SUITE_WITH_TESTS, TEST_WITH_REASON));
	}

	@Test
	public void getReasonTestNo() {
		assertEquals(
				"test not ignored",
				NO_REASON,
				ignored.getReasonForIgnoring(SUITE_WITH_TESTS, TEST_NOT_IGNORED));
	}

	@Test
	public void getReasonTestNoReason() {
		assertEquals("test with no reason", NO_REASON,
				ignored.getReasonForIgnoring(SUITE_WITH_TESTS, TEST_NO_REASON));
	}

	@Test
	public void getReasonTestFromSuite() {
		assertEquals("test from ignored suite", REASON_FOR_SUITE,
				ignored.getReasonForIgnoring(SUITE_WITH_REASON,
						TEST_FROM_IGNORED_SUITE));
	}

	@Test
	public void isIgnoredSuiteYes() {
		assertTrue("ignored suite", ignored.isIgnored(SUITE_WITH_REASON));
	}

	@Test
	public void isIgnoredSuiteNo() {
		assertFalse("not ignored suite", ignored.isIgnored(SUITE_NOT_IGNORED));
	}

	@Test
	public void getReasonSuiteYes() {
		assertEquals("suite with reason", REASON_FOR_SUITE,
				ignored.getReasonForIgnoring(SUITE_WITH_REASON));
	}

	@Test
	public void getReasonSuiteNo() {
		assertEquals("suite not ignored", NO_REASON,
				ignored.getReasonForIgnoring(SUITE_NOT_IGNORED));
	}

	@Test
	public void getReasonSuiteNoReason() {
		assertEquals("suite with no reason", NO_REASON,
				ignored.getReasonForIgnoring(SUITE_NO_REASON));
	}
}
