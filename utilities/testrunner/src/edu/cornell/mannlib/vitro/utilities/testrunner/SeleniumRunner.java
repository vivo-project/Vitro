/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import static edu.cornell.mannlib.vitro.utilities.testrunner.SeleniumRunnerParameters.LOGFILE_NAME;

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
	private final Listener listener;
	private final UploadAreaCleaner uploadCleaner;
	private final ModelCleaner modelCleaner;
	private final SuiteRunner suiteRunner;
	private final OutputManager outputManager;

	public SeleniumRunner(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.listener = parms.getListener();
		this.uploadCleaner = new UploadAreaCleaner(parms);
		this.modelCleaner = new ModelCleaner(parms);
		this.suiteRunner = new SuiteRunner(parms);
		this.outputManager = new OutputManager(parms);
	}

	public void runSelectedSuites() {
		try {
			listener.runStarted();
			outputManager.cleanOutputDirectory();
			for (File suiteDir : parms.getSelectedSuites()) {
				listener.suiteStarted(suiteDir);
				try {
					if (parms.isCleanModel()) {
						modelCleaner.clean();
					}
					if (parms.isCleanUploads()) {
						uploadCleaner.clean();
					}
					suiteRunner.runSuite(suiteDir);
				} catch (IOException e) {
					listener.suiteFailed(suiteDir, e);
				} catch (CommandRunnerException e) {
					listener.suiteFailed(suiteDir, e);
				}
				listener.suiteStopped(suiteDir);
			}
			listener.runEndTime();
			outputManager.summarizeOutput();
		} catch (IOException e) {
			listener.runFailed(e);
			e.printStackTrace();
		} catch (FatalException e) {
			listener.runFailed(e);
			e.printStackTrace();
		}
		listener.runStopped();
	}

	private static void selectAllSuites(SeleniumRunnerParameters parms) {
		List<File> suites = new ArrayList<File>();
		for (File parentDir : parms.getSuiteParentDirectories()) {
			suites.addAll(parms.findSuiteDirs(parentDir));
		}
		parms.setSelectedSuites(suites);
	}

	private static void usage(String message) {
		System.out.println(message);
		System.out.println("Usage is: SeleniumRunner <parameters_file> "
				+ "[\"interactive\"]");
		System.exit(1);
	}

	public static void main(String[] args) {
		SeleniumRunnerParameters parms = null;
		boolean interactive = false;

		if ((args.length != 1) && (args.length != 2)) {
			usage("Wrong number of arguments.");
		}

		if (args.length == 2) {
			String option = args[1].trim();
			if (option.length() > 0) {
				if (!"interactive".equalsIgnoreCase(args[1])) {
					usage("Invalid argument '" + args[1] + "'");
				}
				interactive = true;
			}
		}

		try {
			parms = new SeleniumRunnerParameters(args[0]);
		} catch (IOException e) {
			usage("Can't read properties file: " + e.getMessage());
		}

		if (interactive) {
			// TODO hook up the GUI.
			throw new RuntimeException("interactive mode not implemented.");
		} else {
			File logFile = new File(parms.getOutputDirectory(), LOGFILE_NAME);
			System.out.println("Log file is '" + logFile.getPath() + "'");

			// Run all of the suites.
			// For each suite, clean the model and the upload area.
			selectAllSuites(parms);
			parms.setCleanModel(true);
			parms.setCleanUploads(true);

			System.out.println(parms);

			SeleniumRunner runner = new SeleniumRunner(parms);
			runner.runSelectedSuites();
		}
	}

}
