package edu.cornell.mannlib.vitro.utilities.testrunner.output;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

public class OutputDataModel implements Listener {
	private boolean runCompleted;
	private long startTime;
	private long endTime;

	// ----------------------------------------------------------------------
	// Listener methods that affect the data model
	// ----------------------------------------------------------------------

	@Override
	public void runStarted() {
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

	// ----------------------------------------------------------------------
	// Accessor methods
	// ----------------------------------------------------------------------

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

	// ----------------------------------------------------------------------
	// Listener methods that don't affect the data model
	// ----------------------------------------------------------------------

	@Override
	public void suiteIgnored(File suite) {
	}

	@Override
	public void suiteAdded(File suite) {
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
	public void webappStopping(String tomcatStopCommand) {
	}

	@Override
	public void webappStopFailed(int returnCode) {
	}

	@Override
	public void webappWaitingForStop(int tomcatStopDelay) {
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
	public void webappWaitingForStart(int tomcatStartDelay) {
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
	public void subProcessStdout(String string) {
	}

	@Override
	public void subProcessErrout(String string) {
	}

	@Override
	public void subProcessStop(List<String> command) {
	}

	@Override
	public void suiteStarted(File suiteDir) {
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
	public void suiteStopped(File suiteDir) {
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
