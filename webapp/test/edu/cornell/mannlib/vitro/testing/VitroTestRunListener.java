package edu.cornell.mannlib.vitro.testing;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import static edu.cornell.mannlib.vitro.testing.VitroTestRunner.ReportLevel.BRIEF;
import static edu.cornell.mannlib.vitro.testing.VitroTestRunner.ReportLevel.FULL;
import static edu.cornell.mannlib.vitro.testing.VitroTestRunner.ReportLevel.MORE;

import java.util.Date;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import com.ibm.icu.text.SimpleDateFormat;

import edu.cornell.mannlib.vitro.testing.VitroTestRunner.ReportLevel;

/**
 * Listen to events as they come from the JUnit test runner. The events from the
 * lifecycle methods are broken down into semantic chunks and executed. Three
 * levels of output are available.
 * 
 * @author jeb228
 */
public class VitroTestRunListener extends RunListener {
	private final ReportLevel reportLevel;

	private int classCount;
	private int testsTotal;
	private int failuresTotal;
	private int ignoresTotal;
	private long overallStartTime;

	private Class<?> currentClass;
	private int testsCurrentClass;
	private int failuresCurrentClass;
	private int ignoresCurrentClass;
	private long classStartTime;

	private String currentTest;
	private boolean testFailed;
	private boolean testIgnored;
	private long testStartTime;

	public VitroTestRunListener(ReportLevel reportLevel) {
		this.reportLevel = reportLevel;
	}

	/** Did any of the tests fail? */
	public boolean isFailure() {
		return failuresTotal > 0;
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
		testFailed = true;
		reportFailure(failure);
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		testFailed = true;
		reportFailure(failure);
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
			System.out.println("Tests Pass Fail Ignore Seconds");
		}
	}

	private void reportTestRun() {
		int successes = testsTotal - failuresTotal - ignoresTotal;

		if (reportLevel != BRIEF) {
			System.out.println();
		}

		System.out.format(
				"Tests Pass Fail Ignore Seconds  TOTAL (%d classes)\n",
				classCount);
		System.out.format(" %4d %4d %4d  %4d  %6s\n", testsTotal, successes,
				failuresTotal, ignoresTotal, elapsed(overallStartTime));

		if (reportLevel != BRIEF) {
			System.out.println("Ending test run at "
					+ time(System.currentTimeMillis()));
		}
	}

	private void openCurrentClass(Description description) {
		currentClass = description.getTestClass();
		classStartTime = System.currentTimeMillis();
		testsCurrentClass = 0;
		failuresCurrentClass = 0;
		ignoresCurrentClass = 0;
	}

	private void closeCurrentClass() {
		classCount++;
		testsTotal += testsCurrentClass;
		failuresTotal += failuresCurrentClass;
		ignoresTotal += ignoresCurrentClass;
	}

	private void reportCurrentClassStart() {
		if (reportLevel == FULL) {
			System.out.format("Tests Pass Fail Ignore Seconds  %s\n",
					currentClass.getName());
		}
	}

	private void reportCurrentClass() {
		int successes = testsCurrentClass - failuresCurrentClass
				- ignoresCurrentClass;
		if (reportLevel == MORE) {
			System.out.format(" %4d %4d %4d  %4d  %6s %s\n", testsCurrentClass,
					successes, failuresCurrentClass, ignoresCurrentClass,
					elapsed(classStartTime), currentClass.getSimpleName());
		}
		if (reportLevel == FULL) {
			System.out.println("-----------------------------");
			System.out.format(" %4d %4d %4d  %4d  %6s\n", testsCurrentClass,
					successes, failuresCurrentClass, ignoresCurrentClass,
					elapsed(classStartTime));
			System.out.println();
		}
	}

	private void openCurrentTest(Description description) {
		currentTest = description.getMethodName();
		testFailed = false;
		testIgnored = false;
		testStartTime = System.currentTimeMillis();
	}

	private void closeCurrentTest() {
		if (testFailed) {
			failuresCurrentClass++;
		}
		if (testIgnored) {
			ignoresCurrentClass++;
		}
		testsCurrentClass++;
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
			char passFlag = (testIgnored | testFailed) ? ' ' : '1';
			char failFlag = testFailed ? '1' : ' ';
			char ignoreFlag = testIgnored ? '1' : ' ';
			System.out.format("         %c    %c      %c %6s        %s()\n",
					passFlag, failFlag, ignoreFlag, elapsed(testStartTime),
					currentTest);
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

}
