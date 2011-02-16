/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;

/**
 * Run a Selenium TestSuite in a sub-process.
 */
public class SuiteRunner {

	private final SeleniumRunnerParameters parms;
	private final CommandRunner runner;
	private final Listener listener;

	public SuiteRunner(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.runner = new CommandRunner(parms);
		this.listener = parms.getListener();
	}

	/**
	 * Run the suite.
	 */
	public void runSuite(File suiteDir) {
		listener.suiteTestingStarted(suiteDir);

		List<String> cmd = new ArrayList<String>();
		cmd.add("java");
		cmd.add("-jar");
		cmd.add(parms.getSeleniumJarPath().getPath());
		cmd.add("-singleWindow");
		cmd.add("-timeout");
		cmd.add(String.valueOf(parms.getSuiteTimeoutLimit()));
		cmd.add("-userExtensions");
		cmd.add(parms.getUserExtensionsFile().getPath());

		// TODO - figure out why the use of a template means running the test
		// twice in simultaneous tabs.
		// if (parms.hasFirefoxProfileDir()) {
		// cmd.add("-firefoxProfileTemplate");
		// cmd.add(parms.getFirefoxProfileDir().getPath());
		// }

		String suiteName = suiteDir.getName();
		File outputFile = new File(parms.getOutputDirectory(), suiteName
				+ ".html");
		File suiteFile = new File(suiteDir, "Suite.html");

		cmd.add("-htmlSuite");
		cmd.add("*firefox");
		cmd.add(parms.getWebsiteUrl());
		cmd.add(suiteFile.getPath());
		cmd.add(outputFile.getPath());

		try {
			runner.run(cmd);
		} catch (CommandRunnerException e) {
			throw new FatalException(e);
		}

		int returnCode = runner.getReturnCode();
		if (returnCode != 0) {
			listener.suiteFailed(suiteDir, returnCode);
		}

		listener.suiteTestingStopped(suiteDir);
	}

}
