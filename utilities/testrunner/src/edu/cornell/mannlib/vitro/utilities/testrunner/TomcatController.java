/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

/**
 * Start and stop the webapp, so we can clean the database.
 */
public class TomcatController {
	private final SeleniumRunnerParameters parms;
	private final ModelCleanerProperties properties;
	private final Listener listener;

	public TomcatController(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.properties = parms.getModelCleanerProperties();
		this.listener = parms.getListener();

		try {
			checkThatTomcatIsReady();
		} catch (CommandRunnerException e) {
			throw new FatalException(e);
		}
	}

	/**
	 * Insure that Tomcat is ready and that we can start and stop VIVO.
	 */
	private void checkThatTomcatIsReady() throws CommandRunnerException {
		String tomcatCheckReadyCommand = properties
				.getTomcatCheckReadyCommand();

		CommandRunner runner = new CommandRunner(parms);

		listener.webappCheckingReady(tomcatCheckReadyCommand);
		runner.run(parseCommandLine(tomcatCheckReadyCommand));

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.webappCheckReadyFailed(returnCode);
			throw new CommandRunnerException("Tomcat is not ready: code="
					+ returnCode);
		}

		listener.webappCheckedReady();
	}

	public void stopTheWebapp() throws CommandRunnerException {
		String tomcatStopCommand = properties.getTomcatStopCommand();

		CommandRunner runner = new CommandRunner(parms);

		listener.webappStopping(tomcatStopCommand);
		runner.run(parseCommandLine(tomcatStopCommand));

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.webappStopFailed(returnCode);
			throw new CommandRunnerException("Failed to stop Tomcat: code="
					+ returnCode);
		}

		listener.webappStopped();
	}

	/**
	 * Start Tomcat and wait for it to initialize.
	 */
	public void startTheWebapp() throws CommandRunnerException {
		String tomcatStartCommand = properties.getTomcatStartCommand();

		CommandRunner runner = new CommandRunner(parms);

		listener.webappStarting(tomcatStartCommand);
		try {
			// Stupid Windows won't allow us to start Tomcat as an independent
			// process (except if its installed as a service).
			runner.run(parseCommandLine(tomcatStartCommand));
		} catch (CommandRunnerException e) {
			throw new FatalException(e);
		}

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.webappStartFailed(returnCode);
			throw new CommandRunnerException("Failed to start Tomcat: code="
					+ returnCode);
		}

		listener.webappStarted();
	}

	/**
	 * A command line must be broken into separate arguments, where arguments
	 * are delimited by blanks unless the blank (and the argument) is enclosed
	 * in quotes.
	 */
	static List<String> parseCommandLine(String commandLine) {
		List<String> pieces = new ArrayList<String>();
		StringBuilder piece = null;
		boolean inDelimiter = true;
		boolean inQuotes = false;
		for (int i = 0; i < commandLine.length(); i++) {
			char thisChar = commandLine.charAt(i);
			if ((thisChar == ' ') && !inQuotes) {
				if (inDelimiter) {
					// No effect.
				} else {
					inDelimiter = true;
					pieces.add(piece.toString());
				}
			} else if (thisChar == '"') {
				// Quotes are not carried into the parsed strings.
				inQuotes = !inQuotes;
			} else { // Not a blank or a quote.
				if (inDelimiter) {
					inDelimiter = false;
					piece = new StringBuilder();
				}
				piece.append(thisChar);
			}
		}

		// There is an implied delimiter at the end of the command line.
		if (!inDelimiter) {
			pieces.add(piece.toString());
		}

		// Quotes must appear in pairs
		if (inQuotes) {
			throw new IllegalArgumentException(
					"Command line contains mismatched quotes: " + commandLine);
		}

		return pieces;
	}

	/**
	 * The run is finished. Do we need to do anything?
	 */
	public void cleanup() {
		// Don't need to do anything.

		// If we've been starting and stopping Tomcat,
		// stop it one more time.
		if (parms.isCleanModel()) {
			try {
				stopTheWebapp();
			} catch (CommandRunnerException e) {
				throw new FatalException(e);
			}
		}

	}

}
