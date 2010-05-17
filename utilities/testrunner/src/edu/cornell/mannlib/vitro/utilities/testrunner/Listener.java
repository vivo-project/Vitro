/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

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
public class Listener {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private final Writer writer;

	// ----------------------------------------------------------------------
	// Listener methods
	// ----------------------------------------------------------------------

	public Listener(PrintStream out) {
		this.writer = new OutputStreamWriter(out);
	}

	public Listener(File logFile) throws IOException {
		this.writer = new FileWriter(logFile);
	}

	// ----------------------------------------------------------------------
	// Listener methods
	// ----------------------------------------------------------------------

	public void runStarted() {
		log("Run started.");
	}

	public void runFailed(Exception e) {
		log("Run failed - fatal error");
		log(e);
	}

	public void runStopped() {
		log("Run stopped.");
	}

	public void webappStopping(String tomcatStopCommand) {
		log("Stopping tomcat: " + tomcatStopCommand);
	}

	public void webappStopFailed(int returnCode) {
		log("Failed to stop tomcat; return code was " + returnCode);
	}

	public void webappWaitingForStop(int tomcatStopDelay) {
		log("Waiting " + tomcatStopDelay + " seconds for tomcat to stop.");
	}

	public void webappStopped() {
		log("Tomcat stopped.");
	}

	public void dropDatabaseStarting(String statement) {
		log("Dropping database: " + statement);
	}

	public void dropDatabaseFailed(int returnCode) {
		log("Failed to drop the database; return code was " + returnCode);
	}

	public void dropDatabaseComplete() {
		log("Dropped database.");
	}

	public void loadDatabaseStarting(String statement) {
		log("Loading the database: " + statement);
	}

	public void loadDatabaseFailed(int returnCode) {
		log("Failed to load the database; return code was " + returnCode);
	}

	public void loadDatabaseComplete() {
		log("Loaded the database.");
	}

	public void webappStarting(String tomcatStartCommand) {
		log("Starting tomcat: " + tomcatStartCommand);
	}

	public void webappStartFailed(int returnCode) {
		log("Failed to start tomcat; return code was " + returnCode);
	}

	public void webappWaitingForStart(int tomcatStartDelay) {
		log("Waiting " + tomcatStartDelay + " seconds for tomcat to start.");
	}

	public void webappStarted() {
		log("Tomcat started.");
	}

	public void subProcessStart(List<String> command) {
		log("Subprocess started: " + command);
	}

	public void subProcessStartInBackground(List<String> command) {
		log("Subprocess started in background: " + command);
	}

	public void subProcessStdout(String string) {
		logRawText(string);
	}

	public void subProcessErrout(String string) {
		logRawText(string);
	}

	public void subProcessStop(List<String> command) {
		log("Subprocess stopped: " + command);
	}

	public void suiteStarted(File suiteDir) {
		log("Suite started: " + suiteDir.getName());
	}

	public void suiteTestingStarted(File suiteDir) {
		log("Suite testing started: " + suiteDir.getName());
	}

	public void suiteFailed(File suiteDir, int returnCode) {
		log("Suite failed: " + suiteDir.getName() + ", returnCode="
				+ returnCode);
	}

	public void suiteFailed(File suiteDir, Exception e) {
		log("Suite failed: " + suiteDir.getName());
		log(e);
	}

	public void suiteTestingStopped(File suiteDir) {
		log("Suite testing stopped: " + suiteDir.getName());
	}

	public void suiteStopped(File suiteDir) {
		log("Suite stopped: " + suiteDir.getName());
	}

	public void cleanUploadStart(File uploadDirectory) {
		log("Upload cleaning started: " + uploadDirectory.getPath());
	}

	public void cleanUploadFailed(File uploadDirectory, IOException e) {
		log("Upload cleaning failed: " + uploadDirectory.getPath());
		log(e);
	}

	public void cleanUploadStop(File uploadDirectory) {
		log("Upload cleaning stopped: " + uploadDirectory.getPath());
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
