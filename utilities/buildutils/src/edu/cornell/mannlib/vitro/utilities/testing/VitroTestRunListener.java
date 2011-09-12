/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testing;

import static edu.cornell.mannlib.vitro.utilities.testing.VitroTestRunner.ReportLevel.BRIEF;
import static edu.cornell.mannlib.vitro.utilities.testing.VitroTestRunner.ReportLevel.FULL;
import static edu.cornell.mannlib.vitro.utilities.testing.VitroTestRunner.ReportLevel.MORE;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import edu.cornell.mannlib.vitro.utilities.testing.VitroTestRunner.ReportLevel;

/**
 * Listen to events as they come from the JUnit test runner. The events from the
 * lifecycle methods are broken down into semantic chunks and executed. Three
 * levels of output are available.
 * 
 * On the surface, JUnit treats "failures" (failed assertions) the same as
 * "errors" (unexpected exceptions). We're going to distinguish between them.
 * 
 * @author jeb228
 */
public class VitroTestRunListener extends RunListener {
	private final ReportLevel reportLevel;

	private int classCount;
	private int testsTotal;
	private int errorsTotal;
	private int failuresTotal;
	private int ignoresTotal;
	private long overallStartTime;

	private Class<?> currentClass;
	private int testsCurrentClass;
	private int errorsCurrentClass;
	private int failuresCurrentClass;
	private int ignoresCurrentClass;
	private long classStartTime;

	private String currentTest;
	private boolean testHadError;
	private boolean testFailed;
	private boolean testIgnored;
	private long testStartTime;

	public VitroTestRunListener(ReportLevel reportLevel) {
		this.reportLevel = reportLevel;
	}

	/** Did any of the tests fail or have errors? */
	public boolean didEverythingPass() {
		return (failuresTotal == 0) && (errorsTotal == 0);
	}

	// -------------------------------------------------------------------------
	// Life-cycle methods that will be called by the test runner.
	// -------------------------------------------------------------------------

	@Override
	public void testRunStarted(Description description) throws Exception {
		openTestRun();
		reportTestRunStart();
	}

	@Override
	public void testStarted(Description description) throws Exception {
		if (currentClass != description.getTestClass()) {
			if (currentClass != null) {
				closeCurrentClass();
				reportCurrentClass();
			}

			openCurrentClass(description);
			reportCurrentClassStart();
		}

		openCurrentTest(description);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		if (isError(failure)) {
			testHadError = true;
			reportError(failure);
		} else {
			testFailed = true;
			reportFailure(failure);
		}
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		if (isError(failure)) {
			testHadError = true;
			reportError(failure);
		} else {
			testFailed = true;
			reportFailure(failure);
		}
	}

	@Override
	public void testFinished(Description description) throws Exception {
		closeCurrentTest();
		reportCurrentTest();
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		testStarted(description);
		testIgnored = true;
		testFinished(description);
	}

	@Override
	public void testRunFinished(Result result) throws Exception {
		if (currentClass != null) {
			closeCurrentClass();
			reportCurrentClass();
		}
		closeTestRun();
		reportTestRun();
		System.out.println();
	}

	// -------------------------------------------------------------------------
	// Handling the logical events.
	// -------------------------------------------------------------------------

	private void openTestRun() {
		overallStartTime = System.currentTimeMillis();
	}

	private void closeTestRun() {
		// Nothing to close.
	}

	private void reportTestRunStart() {
		if (reportLevel == FULL) {
			System.out
					.println("Starting test run at " + time(overallStartTime));
			System.out.println();
		}

		if (reportLevel == MORE) {
			System.out
					.println("Starting test run at " + time(overallStartTime));
			System.out.println();
			System.out.println("Tests Pass Error Fail Ignore Seconds");
		}
	}

	private void reportTestRun() {
		int successes = testsTotal - errorsTotal - failuresTotal - ignoresTotal;

		if (reportLevel != BRIEF) {
			System.out.println();
		}

		System.out.format(
				"Tests Pass Error Fail Ignore Seconds  TOTAL (%d classes)\n",
				classCount);
		System.out.format(" %4d %4d  %4d %4d  %4d  %6s\n", testsTotal,
				successes, errorsTotal, failuresTotal, ignoresTotal,
				elapsed(overallStartTime));

		if (reportLevel != BRIEF) {
			System.out.println("Ending test run at "
					+ time(System.currentTimeMillis()));
		}
	}

	private void openCurrentClass(Description description) {
		currentClass = description.getTestClass();
		classStartTime = System.currentTimeMillis();
		testsCurrentClass = 0;
		errorsCurrentClass = 0;
		failuresCurrentClass = 0;
		ignoresCurrentClass = 0;
	}

	private void closeCurrentClass() {
		classCount++;
		testsTotal += testsCurrentClass;
		errorsTotal += errorsCurrentClass;
		failuresTotal += failuresCurrentClass;
		ignoresTotal += ignoresCurrentClass;
	}

	private void reportCurrentClassStart() {
		if (reportLevel == FULL) {
			System.out.format("Tests Pass Error Fail Ignore Seconds  %s\n",
					currentClass.getName());
		}
	}

	private void reportCurrentClass() {
		int successes = testsCurrentClass - errorsCurrentClass
				- failuresCurrentClass - ignoresCurrentClass;
		if (reportLevel == MORE) {
			System.out.format(" %4d %4d  %4d %4d  %4d  %6s %s\n",
					testsCurrentClass, successes, errorsCurrentClass,
					failuresCurrentClass, ignoresCurrentClass,
					elapsed(classStartTime), currentClass.getSimpleName());
		}
		if (reportLevel == FULL) {
			System.out.println("-----------------------------------");
			System.out.format(" %4d %4d  %4d %4d  %4d  %6s\n",
					testsCurrentClass, successes, errorsCurrentClass,
					failuresCurrentClass, ignoresCurrentClass,
					elapsed(classStartTime));
			System.out.println();
		}
	}

	private void openCurrentTest(Description description) {
		currentTest = description.getMethodName();
		testHadError = false;
		testFailed = false;
		testIgnored = false;
		testStartTime = System.currentTimeMillis();
	}

	private void closeCurrentTest() {
		if (testHadError) {
			errorsCurrentClass++;
		}
		if (testFailed) {
			failuresCurrentClass++;
		}
		if (testIgnored) {
			ignoresCurrentClass++;
		}
		testsCurrentClass++;
	}

	private boolean isError(Failure failure) {
		Throwable throwable = failure.getException();
		return (throwable != null) && !(throwable instanceof AssertionError);
	}

	private void reportError(Failure error) {
		Description description = error.getDescription();
		String methodName = description.getMethodName();
		String className = description.getTestClass().getName();
		String message = error.getMessage();
		System.out.format("EXCEPTION:   test %s() in %s: %s\n",
				methodName, className, message);
		System.out.println(formatStackTrace(error.getException()));
	}

	private void reportFailure(Failure failure) {
		Description description = failure.getDescription();
		String methodName = description.getMethodName();
		String className = description.getTestClass().getName();
		String message = failure.getMessage();
		System.out.format("TEST FAILED: test %s() in %s: %s\n", methodName,
				className, message);
	}

	private void reportCurrentTest() {
		if (reportLevel == FULL) {
			char passFlag = (testIgnored | testFailed | testHadError) ? ' '
					: '1';
			char errorFlag = testHadError ? '1' : ' ';
			char failFlag = testFailed ? '1' : ' ';
			char ignoreFlag = testIgnored ? '1' : ' ';
			System.out.format(
					"         %c     %c    %c      %c %6s        %s()\n",
					passFlag, errorFlag, failFlag, ignoreFlag,
					elapsed(testStartTime), currentTest);
		}
	}

	// -------------------------------------------------------------------------
	// Formatting methods.
	// -------------------------------------------------------------------------

	private final SimpleDateFormat formatter = new SimpleDateFormat(
			"HH:mm:ss 'on' MMM dd, yyyy");

	private String time(long time) {
		return formatter.format(new Date(time));
	}

	/** Show elapsed time in 6 columns. */
	private String elapsed(long start) {
		long interval = System.currentTimeMillis() - start;
		return String.format("%6.2f", ((float) interval) / 1000.0);
	}

	/**
	 * Trim the stack trace: don't show the line saying "23 more", and don't
	 * show the lines about org.junit or java.lang.reflect or sun.reflect.
	 * 
	 * Once we hit some "client code", we won't trim any futher lines even if
	 * they belong to org.junit, or the others.
	 * 
	 * If we have nested exceptions, the process repeats for each "Caused by"
	 * section.
	 */
	private String formatStackTrace(Throwable throwable) {
		StringWriter w = new StringWriter();
		throwable.printStackTrace(new PrintWriter(w));
		String[] lineArray = w.toString().split("\\n");
		List<String> lines = new ArrayList<String>(Arrays.asList(lineArray));

		boolean removing = true;
		for (int i = lines.size() - 1; i > 0; i--) {
			String line = lines.get(i);
			if (removing) {
				if (line.matches("\\s*[\\.\\s\\d]+more\\s*")
						|| line.contains("at "
								+ VitroTestRunner.class.getName())
						|| line.contains("at org.junit.")
						|| line.contains("at java.lang.reflect.")
						|| line.contains("at sun.reflect.")) {
					lines.remove(line);
				} else {
					removing = false;
				}
			} else {
				if (line.contains("Caused by: ")) {
					removing = true;
				}
			}
		}

		StringBuilder result = new StringBuilder();
		for (String line : lines) {
			result.append(line).append('\n');
		}
		return result.toString().trim();
	}
}
