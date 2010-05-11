/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Run the Selenium test suites. Provide the properties file and perhaps an
 * "interactive" flag.
 */
public class SeleniumRunner {
	private final SeleniumRunnerParameters parms;
	private final Logger logger;
	private final UploadAreaCleaner uploadCleaner;
	private final ModelCleaner modelCleaner;
	private final SuiteRunner suiteRunner;

	public SeleniumRunner(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.logger = parms.getLogger();
		this.uploadCleaner = new UploadAreaCleaner(parms);
		this.modelCleaner = new ModelCleaner(parms);
		this.suiteRunner = new SuiteRunner(parms);
	}

	public void runSelectedSuites() {
		logger.runStarted();
		for (File suiteDir : parms.getSelectedSuites()) {
			logger.suiteStarted(suiteDir);
			try {
				if (parms.isCleanModel()) {
					modelCleaner.clean();
				}
				if (parms.isCleanUploads()) {
					uploadCleaner.clean();
				}
				suiteRunner.runSuite(suiteDir);
			} catch (IOException e) {
				logger.suiteFailed(suiteDir, e);
			}
			logger.suiteStopped(suiteDir);
		}
		logger.runStopped();
	}

	private static void selectAllSuites(SeleniumRunnerParameters parms) {
		List<File> suites = new ArrayList<File>();
		for (File parentDir : parms.getSuiteParentDirectories()) {
			suites.addAll(parms.findSuiteDirs(parentDir));
		}
		parms.setSelectedSuites(suites);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SeleniumRunnerParameters parms = null;
		boolean interactive = false;

		if ((args.length != 1) && (args.length != 2)) {
			usage("Wrong number of arguments.");
		}

		if (args.length == 2) {
			if (!"interactive".equalsIgnoreCase(args[1])) {
				usage("Invalid argument '" + args[1] + "'");
			}
			interactive = true;
		}

		try {
			parms = new SeleniumRunnerParameters(args[0]);
		} catch (IOException e) {
			usage("Can't read properties file: " + e);
		}

		if (interactive) {
			// TODO hook up the GUI.
			throw new RuntimeException("interactive mode not implemented.");
		} else {
			// Run all of the suites.
			// For each suite, clean the model and the upload area.
			selectAllSuites(parms);
			parms.setCleanModel(true);
			parms.setCleanUploads(true);
			SeleniumRunner runner = new SeleniumRunner(parms);
			runner.runSelectedSuites();
		}
	}

	private static void usage(String message) {
		System.out.println(message);
		System.out.println("Usage is: SeleniumRunner <parameters_file> "
				+ "[\"interactive\"]");
		System.exit(1);
	}
}
