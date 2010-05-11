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
 * TODO
 */
public class Logger {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private final Writer writer;

	public Logger(PrintStream out) {
		this.writer = new OutputStreamWriter(out);
	}

	public Logger(File logFile) throws IOException {
		this.writer = new FileWriter(logFile);
	}

	public void runStarted() {
		log("Run started.");
	}

	public void runStopped() {
		log("Run stopped.");
	}

	public void subProcessStart(List<String> command) {
		log("Subprocess started: " + command);
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

	public void suiteFailed(File suiteDir, IOException e) {
		log("Suite failed: " + suiteDir.getName());
		log(e);
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
