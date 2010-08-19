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
	}

	/**
	 * Stop Tomcat and wait the prescribed number of seconds for it to clean up.
	 */
	public void stopTheWebapp() throws CommandRunnerException {
		String tomcatStopCommand = properties.getTomcatStopCommand();
		int tomcatStopDelay = properties.getTomcatStopDelay();

		CommandRunner runner = new CommandRunner(parms);

		listener.webappStopping(tomcatStopCommand);
		runner.run(parseCommandLine(tomcatStopCommand));

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.webappStopFailed(returnCode);
			// Throw no exception - this can happen if Tomcat isn't running.
		}

		listener.webappWaitingForStop(tomcatStopDelay);
		try {
			Thread.sleep(tomcatStopDelay * 1000L);
		} catch (InterruptedException e) {
			// Just continue.
		}

		listener.webappStopped();
	}

	/**
	 * Start Tomcat and wait for it to initialize.
	 */
	public void startTheWebapp() {
		String tomcatStartCommand = properties.getTomcatStartCommand();
		int tomcatStartDelay = properties.getTomcatStartDelay();

		CommandRunner runner = new CommandRunner(parms);

		listener.webappStarting(tomcatStartCommand);
		try {
			runner.runAsBackground(parseCommandLine(tomcatStartCommand));
		} catch (CommandRunnerException e) {
			throw new FatalException(e);
		}

		// Can't check the return code because the process shouldn't end.
		
		listener.webappWaitingForStart(tomcatStartDelay);
		try {
			Thread.sleep(tomcatStartDelay * 1000L);
		} catch (InterruptedException e) {
			// Just continue.
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
