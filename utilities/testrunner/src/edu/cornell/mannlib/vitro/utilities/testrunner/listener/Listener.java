/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.listener;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A listener for all events that occur during the run.
 */
public interface Listener {

	void suiteIgnored(File suite);

	void suiteAdded(File suite);

	void runStarted();

	void runFailed(Exception e);

	void runEndTime();

	void runStopped();

	void cleanOutputStart(File outputDirectory);

	void cleanOutputFailed(File outputDirectory, IOException e);

	void cleanOutputStop(File outputDirectory);

	void webappStopping(String tomcatStopCommand);

	void webappStopFailed(int returnCode);

	void webappStopped();

	void dropDatabaseStarting(String statement);

	void dropDatabaseFailed(int returnCode);

	void dropDatabaseComplete();

	void loadDatabaseStarting(String statement);

	void loadDatabaseFailed(int returnCode);

	void loadDatabaseComplete();

	void webappCheckingReady(String command);

	void webappCheckReadyFailed(int returnCode);

	void webappCheckedReady();

	void webappStarting(String command);

	void webappStartFailed(int returnCode);

	void webappStarted();

	void subProcessStart(List<String> command);

	void subProcessStartInBackground(List<String> command);

	void subProcessStdout(String string);

	void subProcessErrout(String string);

	void subProcessStop(List<String> command);

	void suiteStarted(File suiteDir);

	void suiteTestingStarted(File suiteDir);

	void suiteFailed(File suiteDir, int returnCode);

	void suiteFailed(File suiteDir, Exception e);

	void suiteTestingStopped(File suiteDir);

	void suiteStopped(File suiteDir);

	void cleanUploadStart(File uploadDirectory);

	void cleanUploadFailed(File uploadDirectory, IOException e);

	void cleanUploadStop(File uploadDirectory);

	void logWarning(String message);

}
