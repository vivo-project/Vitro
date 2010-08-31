/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import static edu.cornell.mannlib.vitro.utilities.testrunner.SeleniumRunnerParameters.LOGFILE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.utilities.testrunner.datamodel.DataModel;
import edu.cornell.mannlib.vitro.utilities.testrunner.datamodel.SuiteContents;
import edu.cornell.mannlib.vitro.utilities.testrunner.listener.Listener;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.OutputManager;

/**
 * Run the Selenium test suites. Provide the properties file and perhaps an
 * "interactive" flag.
 */
public class SeleniumRunner {
	private final SeleniumRunnerParameters parms;
	private final Listener listener;
	private final UploadAreaCleaner uploadCleaner;
	private final TomcatController tomcatController;
	private final ModelCleaner modelCleaner;
	private final SuiteRunner suiteRunner;
	private final OutputManager outputManager;
	private final DataModel dataModel;

	public SeleniumRunner(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.listener = parms.getListener();
		this.uploadCleaner = new UploadAreaCleaner(parms);
		this.tomcatController = new TomcatController(parms);
		this.modelCleaner = new ModelCleaner(parms, this.tomcatController);
		this.suiteRunner = new SuiteRunner(parms);
		this.outputManager = new OutputManager(parms);

		this.dataModel = new DataModel();
		this.dataModel.setIgnoredTestList(parms.getIgnoredTests());
	}

	/**
	 * Set up the run, run the selected suites, summarize the output, and clean
	 * up afterwards.
	 * 
	 * @return <code>true</code> iff all tests passed.
	 */
	public boolean run() {
		boolean success;
		try {
			listener.runStarted();
			outputManager.cleanOutputDirectory();

			parseSuites();
			selectSuites();

			runSelectedSuites();
			tomcatController.cleanup();

			listener.runEndTime();
			outputManager.summarizeOutput(dataModel);
			success = Status.isSuccess(dataModel.getRunStatus());
		} catch (IOException e) {
			listener.runFailed(e);
			success = false;
			throw new FatalException(e);
		} catch (FatalException e) {
			listener.runFailed(e);
			success = false;
			throw e;
		} finally {
			listener.runStopped();
			outputManager.summarizeOutput(dataModel);
		}

		return success;
	}

	/**
	 * Scan the suite directories in the suite files.
	 */
	public void parseSuites() {
		List<SuiteContents> allContents = new ArrayList<SuiteContents>();
		for (File parentDir : parms.getSuiteParentDirectories()) {
			for (File suiteDir : parms.findSuiteDirs(parentDir)) {
				SuiteContents contents = SuiteContents.parse(suiteDir);
				if (contents != null) {
					allContents.add(contents);
				}
			}
		}
		dataModel.setSuiteContents(allContents);
	}

	/**
	 * Select all test suites which aren't explicitly ignored.
	 */
	public void selectSuites() {
		Listener listener = parms.getListener();
		IgnoredTests ignored = parms.getIgnoredTests();

		List<File> suites = new ArrayList<File>();

		for (File parentDir : parms.getSuiteParentDirectories()) {
			for (File suite : parms.findSuiteDirs(parentDir)) {
				String suiteName = suite.getName();
				if (ignored.isIgnored(suiteName)) {
					listener.suiteIgnored(suite);
				} else {
					listener.suiteAdded(suite);
					suites.add(suite);
				}
			}
		}

		dataModel.setSelectedSuites(suites);
	}

	public void runSelectedSuites() {
		for (File suiteDir : dataModel.getSelectedSuites()) {
			outputManager.summarizeOutput(dataModel);
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
		boolean success = false;

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
			try {
				parms = new SeleniumRunnerParameters(args[0]);
			} catch (IOException e) {
				usage("Can't read properties file: " + e.getMessage());
			} catch (IllegalArgumentException e) {
				throw new FatalException(e);
			}

			if (interactive) {
				// TODO hook up the GUI.
				throw new FatalException("interactive mode not implemented. "
						+ "use 'ant acceptance -Dacceptance.batch=true'");
			} else {
				File logFile = new File(parms.getOutputDirectory(),
						LOGFILE_NAME);
				System.out.println("Log file is '" + logFile.getPath() + "'");

				// Run all of the suites.
				// For each suite, clean the model and the upload area.
				parms.setCleanModel(true);
				parms.setCleanUploads(true);

				System.out.println(parms);

				success = new SeleniumRunner(parms).run();
			}
		} catch (FatalException e) {
			System.err.println("\n\n-----------------\n"
					+ "|  FATAL ERROR  |   " + e.getMessage()
					+ "\n-----------------\n\n");
			e.printStackTrace();
		}
		System.out.println("Exiting SeleniumRunner");
		System.exit(success ? 0 : -1);
	}

}
