/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.output;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cornell.mannlib.vitro.utilities.testrunner.FileHelper;
import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

public class OutputDataListener implements Listener {
	private boolean runCompleted;
	private long startTime;
	private long endTime;

	private ProcessOutput currentSuiteOutput = new ProcessOutput("");
	private Map<String, ProcessOutput> failureMessages = new HashMap<String, ProcessOutput>();

	// ----------------------------------------------------------------------
	// Listener methods that affect the data model
	// ----------------------------------------------------------------------

	@Override
	public void runStarted() {
		System.out.println("run started");
		startTime = new Date().getTime();
	}

	@Override
	public void runEndTime() {
		endTime = new Date().getTime();
	}

	@Override
	public void runStopped() {
		runCompleted = true;
	}

	@Override
	public void suiteStarted(File suiteDir) {
		currentSuiteOutput = new ProcessOutput(FileHelper.baseName(suiteDir));
	}

	@Override
	public void suiteStopped(File suiteDir) {
		if (currentSuiteOutput.isSuiteFailure()) {
			failureMessages.put(currentSuiteOutput.suiteName,
					currentSuiteOutput);
		}
		currentSuiteOutput = new ProcessOutput("");
	}

	@Override
	public void subProcessStdout(String string) {
		currentSuiteOutput.addStdout(string);
	}

	@Override
	public void subProcessErrout(String string) {
		currentSuiteOutput.addErrout(string);
	}

	// ----------------------------------------------------------------------
	// A class that holds a snapshot of the data.
	// ----------------------------------------------------------------------

	/**
	 * A snapshot of the data that the listener has accumulated so far.
	 */
	public static class Info {
		public static Info EMPTY_INFO = new Info();

		private final boolean runCompleted;
		private final long startTime;
		private final long endTime;
		private final Map<String, ProcessOutput> failureMessages;

		Info() {
			this.runCompleted = false;
			this.startTime = 0;
			this.endTime = 0;
			this.failureMessages = Collections.emptyMap();
		}

		Info(OutputDataListener parent) {
			this.runCompleted = parent.runCompleted;
			this.startTime = parent.startTime;
			this.endTime = parent.endTime;
			this.failureMessages = Collections
					.unmodifiableMap(new HashMap<String, ProcessOutput>(
							parent.failureMessages));
		}

		public boolean isRunCompleted() {
			return runCompleted;
		}

		public long getStartTime() {
			return startTime;
		}

		public long getEndTime() {
			return endTime;
		}

		public long getElapsedTime() {
			if ((startTime == 0) || (endTime == 0)) {
				return 0;
			} else {
				return endTime - startTime;
			}
		}

		public Map<String, ProcessOutput> getFailureMessages() {
			return failureMessages;
		}

	}

	/**
	 * Get a snapshot of the data.
	 */
	public Info getInfo() {
		return new Info(this);
	}

	// ----------------------------------------------------------------------
	// A class that summarized the sub-process output from a test suite.
	// ----------------------------------------------------------------------

	/**
	 * The output from a subprocess that runs a test suite. It's only
	 * interesting if it indicates a suite failure.
	 */
	public static class ProcessOutput {
		private static final String SUITE_FAILURE_PATTERN = "exception|error(?i)";
		private final String suiteName;
		private final StringBuilder stdout = new StringBuilder();
		private final StringBuilder errout = new StringBuilder();

		public ProcessOutput(String suiteName) {
			this.suiteName = suiteName;
		}

		public void addStdout(String string) {
			stdout.append(string);
		}

		public void addErrout(String string) {
			errout.append(string);
		}

		public String getSuiteName() {
			return suiteName;
		}

		public String getStdout() {
			return stdout.toString();
		}

		public String getErrout() {
			return errout.toString();
		}

		public boolean isSuiteFailure() {
			Pattern p = Pattern.compile(SUITE_FAILURE_PATTERN);
			Matcher m = p.matcher(errout);
			return m.find();
		}

	}

	// ----------------------------------------------------------------------
	// Listener methods that don't affect the data model
	// ----------------------------------------------------------------------

	@Override
	public void suiteAdded(File suite) {
	}

	@Override
	public void suiteIgnored(File suite) {
	}

	@Override
	public void runFailed(Exception e) {
	}

	@Override
	public void cleanOutputStart(File outputDirectory) {
	}

	@Override
	public void cleanOutputFailed(File outputDirectory, IOException e) {
	}

	@Override
	public void cleanOutputStop(File outputDirectory) {
	}

	@Override
	public void webappCheckingReady(String command) {
	}

	@Override
	public void webappCheckReadyFailed(int returnCode) {
	}

	@Override
	public void webappCheckedReady() {
	}

	@Override
	public void webappStopping(String tomcatStopCommand) {
	}

	@Override
	public void webappStopFailed(int returnCode) {
	}

	@Override
	public void webappStopped() {
	}

	@Override
	public void dropDatabaseStarting(String statement) {
	}

	@Override
	public void dropDatabaseFailed(int returnCode) {
	}

	@Override
	public void dropDatabaseComplete() {
	}

	@Override
	public void loadDatabaseStarting(String statement) {
	}

	@Override
	public void loadDatabaseFailed(int returnCode) {
	}

	@Override
	public void loadDatabaseComplete() {
	}

	@Override
	public void webappStarting(String tomcatStartCommand) {
	}

	@Override
	public void webappStartFailed(int returnCode) {
	}

	@Override
	public void webappStarted() {
	}

	@Override
	public void subProcessStart(List<String> command) {
	}

	@Override
	public void subProcessStartInBackground(List<String> command) {
	}

	@Override
	public void subProcessStop(List<String> command) {
	}

	@Override
	public void suiteTestingStarted(File suiteDir) {
	}

	@Override
	public void suiteFailed(File suiteDir, int returnCode) {
	}

	@Override
	public void suiteFailed(File suiteDir, Exception e) {
	}

	@Override
	public void suiteTestingStopped(File suiteDir) {
	}

	@Override
	public void cleanUploadStart(File uploadDirectory) {
	}

	@Override
	public void cleanUploadFailed(File uploadDirectory, IOException e) {
	}

	@Override
	public void cleanUploadStop(File uploadDirectory) {
	}

	@Override
	public void logWarning(String message) {
	}

}
