/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the contents of the output area. Removes old files prior to a run.
 * Creates a unified summary of the test suite outputs.
 */
public class OutputManager {
	private final SeleniumRunnerParameters parms;
	private final Listener listener;

	public OutputManager(SeleniumRunnerParameters parms) {
		this.parms = parms;
		this.listener = parms.getListener();
	}

	/**
	 * Delete any output files from previous runs.
	 */
	public void cleanOutputDirectory() throws IOException {
		File outputDirectory = parms.getOutputDirectory();
		listener.cleanOutputStart(outputDirectory);

		try {
			for (File file : outputDirectory.listFiles()) {
				// Skip the log file, since we are already over-writing it.
				if (file.equals(parms.getLogFile())) {
					continue;
				}
				// Skip any hidden files (like .svn)
				if (file.getPath().startsWith(".")) {
					continue;
				}
				// Delete all of the others.
				if (file.isFile()) {
					FileHelper.deleteFile(file);
				} else {
					FileHelper.purgeDirectoryRecursively(file);
				}
			}
		} catch (IOException e) {
			listener.cleanOutputFailed(outputDirectory, e);
			throw e;
		} finally {
			listener.cleanOutputStop(outputDirectory);
		}
	}

	/**
	 * Parse each of the output files from the test suites, and create a unified
	 * output file.
	 */
	public Status summarizeOutput() {
		LogStats log = LogStats.parse(parms.getLogFile());

		List<SuiteStats> suites = new ArrayList<SuiteStats>();
		for (File outputFile : parms.getOutputDirectory().listFiles(
				new HtmlFileFilter())) {
			SuiteStats suite = SuiteStats.parse(parms, outputFile);
			if (suite != null) {
				suites.add(suite);
			}
		}

		OutputSummaryFormatter formatter = new OutputSummaryFormatter(parms);
		formatter.format(log, suites);
		return formatter.figureOverallStatus(log, suites);
	}

	private static class HtmlFileFilter implements FileFilter {
		public boolean accept(File path) {
			return path.getName().endsWith(".html")
					|| path.getName().endsWith(".htm");
		}

	}
}
