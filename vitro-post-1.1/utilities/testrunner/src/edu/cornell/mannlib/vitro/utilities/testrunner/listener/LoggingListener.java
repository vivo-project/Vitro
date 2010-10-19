/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A listener for all events that occur during the run. In this basic
 * implementation, each event is simply formatted and written to a log file or
 * {@link PrintStream}.
 */
public class LoggingListener implements Listener {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private final Writer writer;

	// ----------------------------------------------------------------------
	// Constructors
	// ----------------------------------------------------------------------

	public LoggingListener(PrintStream out) {
		this.writer = new OutputStreamWriter(out);
	}

	public LoggingListener(File logFile) throws IOException {
		this.writer = new FileWriter(logFile);
	}

	// ----------------------------------------------------------------------
	// Listener methods
	// ----------------------------------------------------------------------

	@Override
	public void suiteIgnored(File suite) {
		log("Suite '" + suite.getName() + "' ignored.");
	}

	@Override
	public void suiteAdded(File suite) {
		log("Suite '" + suite.getName() + "' added.");
	}

	@Override
	public void runStarted() {
		log("Run started.");
	}

	@Override
	public void runFailed(Exception e) {
		log("Run failed - fatal error");
		log(e);
	}

	@Override
	public void runEndTime() {
		log("Testing complete.");
	}

	@Override
	public void runStopped() {
		log("Run stopped.");
	}

	@Override
	public void cleanOutputStart(File outputDirectory) {
		log("Output area cleaning started: " + outputDirectory.getPath());
	}

	@Override
	public void cleanOutputFailed(File outputDirectory, IOException e) {
		log("Output area cleaning failed: " + outputDirectory.getPath());
		log(e);
	}

	@Override
	public void cleanOutputStop(File outputDirectory) {
		log("Output area cleaning stopped: " + outputDirectory.getPath());
	}

	@Override
	public void webappCheckingReady(String command) {
		log("Checking if Tomcat is ready: " + command);
	}

	@Override
	public void webappCheckReadyFailed(int returnCode) {
		log("Tomcat is not ready: " + returnCode);
	}

	@Override
	public void webappCheckedReady() {
		log("Checked that Tomcat is ready.");
	}

	@Override
	public void webappStopping(String command) {
		log("Stopping tomcat: " + command);
	}

	@Override
	public void webappStopFailed(int returnCode) {
		log("Failed to stop tomcat; return code was " + returnCode);
	}

	@Override
	public void webappStopped() {
		log("Tomcat stopped.");
	}

	@Override
	public void dropDatabaseStarting(String statement) {
		log("Dropping database: " + statement);
	}

	@Override
	public void dropDatabaseFailed(int returnCode) {
		log("Failed to drop the database; return code was " + returnCode);
	}

	@Override
	public void dropDatabaseComplete() {
		log("Dropped database.");
	}

	@Override
	public void loadDatabaseStarting(String statement) {
		log("Loading the database: " + statement);
	}

	@Override
	public void loadDatabaseFailed(int returnCode) {
		log("Failed to load the database; return code was " + returnCode);
	}

	@Override
	public void loadDatabaseComplete() {
		log("Loaded the database.");
	}

	@Override
	public void webappStarting(String tomcatStartCommand) {
		log("Starting tomcat: " + tomcatStartCommand);
	}

	@Override
	public void webappStartFailed(int returnCode) {
		log("Failed to start tomcat; return code was " + returnCode);
	}

	@Override
	public void webappStarted() {
		log("Tomcat started.");
	}

	@Override
	public void subProcessStart(List<String> command) {
		log("Subprocess started: " + command);
	}

	@Override
	public void subProcessStartInBackground(List<String> command) {
		log("Subprocess started in background: " + command);
	}

	@Override
	public void subProcessStdout(String string) {
		logRawText(string);
	}

	@Override
	public void subProcessErrout(String string) {
		logRawText(string);
	}

	@Override
	public void subProcessStop(List<String> command) {
		log("Subprocess stopped: " + command);
	}

	@Override
	public void suiteStarted(File suiteDir) {
		log("Suite started: " + suiteDir.getName());
	}

	@Override
	public void suiteTestingStarted(File suiteDir) {
		log("Suite testing started: " + suiteDir.getName());
	}

	@Override
	public void suiteFailed(File suiteDir, int returnCode) {
		log("Suite failed: " + suiteDir.getName() + ", returnCode="
				+ returnCode);
	}

	@Override
	public void suiteFailed(File suiteDir, Exception e) {
		log("Suite failed: " + suiteDir.getName());
		log(e);
	}

	@Override
	public void suiteTestingStopped(File suiteDir) {
		log("Suite testing stopped: " + suiteDir.getName());
	}

	@Override
	public void suiteStopped(File suiteDir) {
		log("Suite stopped: " + suiteDir.getName());
	}

	@Override
	public void cleanUploadStart(File uploadDirectory) {
		log("Upload cleaning started: " + uploadDirectory.getPath());
	}

	@Override
	public void cleanUploadFailed(File uploadDirectory, IOException e) {
		log("Upload cleaning failed: " + uploadDirectory.getPath());
		log(e);
	}

	@Override
	public void cleanUploadStop(File uploadDirectory) {
		log("Upload cleaning stopped: " + uploadDirectory.getPath());
	}

	@Override
	public void logWarning(String message) {
		log("WARNING: " + message);
	}

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void logRawText(String rawText) {
		try {
			writer.write(rawText);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void log(String message) {
		try {
			writer.write(timeStamp() + " " + message + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void log(Throwable t) {
		try {
			t.printStackTrace(new PrintWriter(writer));
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Convert the current date and time to a string for the log.
	 */
	private String timeStamp() {
		return DATE_FORMAT.format(new Date());
	}

}
