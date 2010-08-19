/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.utilities.testrunner.LogStats;
import edu.cornell.mannlib.vitro.utilities.testrunner.Status;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.OutputDataListener;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.SuiteResults.TestResults;

/**
 * Collect all that we know about suites, tests, and their current status.
 */
public class DataModel {

	/* base data */
	private Collection<File> selectedSuites = Collections.emptyList();
	private Collection<SuiteResults> suiteResults = Collections.emptyList();
	private OutputDataListener.Info dataListenerInfo = OutputDataListener.Info.EMPTY_INFO;
	private LogStats logStats = LogStats.EMPTY_LOG_STATS; // TODO

	/* derived data */
	private Status runStatus = Status.PENDING;

	private final List<SuiteData> allSuiteData = new ArrayList<SuiteData>();
	private final List<SuiteData> pendingSuites = new ArrayList<SuiteData>();
	private final List<SuiteData> passingSuites = new ArrayList<SuiteData>();
	private final List<SuiteData> failingSuites = new ArrayList<SuiteData>();
	private final List<SuiteData> ignoredSuites = new ArrayList<SuiteData>();

	private final List<TestResults> allTests = new ArrayList<TestResults>();
	private final List<TestResults> pendingTests = new ArrayList<TestResults>();
	private final List<TestResults> passingTests = new ArrayList<TestResults>();
	private final List<TestResults> failingTests = new ArrayList<TestResults>();
	private final List<TestResults> ignoredTests = new ArrayList<TestResults>();

	// ----------------------------------------------------------------------
	// Constructor
	// ----------------------------------------------------------------------

	public DataModel() {
		calculate();
	}

	// ----------------------------------------------------------------------
	// Update the base data.
	// ----------------------------------------------------------------------

	public void setSelectedSuites(Collection<File> selectedSuites) {
		this.selectedSuites = new ArrayList<File>(selectedSuites);
		calculate();
	}

	public Collection<File> getSelectedSuites() {
		return new ArrayList<File>(selectedSuites);
	}

	public void setSuiteResults(Collection<SuiteResults> suiteResults) {
		this.suiteResults = new ArrayList<SuiteResults>(suiteResults);
		calculate();
	}

	public void captureDataListener(OutputDataListener dataListener) {
		this.dataListenerInfo = dataListener.getInfo();
		calculate();
	}

	public void setLogStats(LogStats logStats) { // TODO
		this.logStats = logStats;
		calculate();
	}

	// ----------------------------------------------------------------------
	// Keep the derived data current.
	// ----------------------------------------------------------------------

	/**
	 * Data in the model has been updated. Refresh all derived data.
	 */
	private void calculate() {
		// Clear all derived data.
		runStatus = Status.OK;

		allSuiteData.clear();
		ignoredSuites.clear();
		pendingSuites.clear();
		failingSuites.clear();
		passingSuites.clear();

		allTests.clear();
		ignoredTests.clear();
		pendingTests.clear();
		failingTests.clear();
		passingTests.clear();

		/*
		 * Suite data.
		 */
		Map<String, SuiteResults> resultsMap = new HashMap<String, SuiteResults>();
		for (SuiteResults result : suiteResults) {
			resultsMap.put(result.getName(), result);
		}

		for (String name : dataListenerInfo.getSuiteNames()) {
			if (dataListenerInfo.getIgnoredSuiteNames().contains(name)) {
				allSuiteData.add(new SuiteData(name, true, null));
			} else if (resultsMap.containsKey(name)) {
				allSuiteData.add(new SuiteData(name, false, resultsMap
						.get(name)));
			} else {
				allSuiteData.add(new SuiteData(name, false, null));
			}
		}

		/*
		 * Tallys of suites and tests.
		 */
		for (SuiteData sd : allSuiteData) {
			SuiteResults result = sd.getResults();
			if (result != null) {
				tallyTests(result);
			}

			if (sd.isIgnored()) {
				ignoredSuites.add(sd);
			} else if (result == null) {
				pendingSuites.add(sd);
			} else if (result.getStatus() == Status.ERROR) {
				failingSuites.add(sd);
			} else {
				passingSuites.add(sd);
			}
		}

		/*
		 * Overall status. Warnings in the log are scary, but ignored tests are
		 * OK.
		 */
		if (logStats.hasErrors() || !failingSuites.isEmpty()) {
			runStatus = Status.ERROR;
		} else {
			if (logStats.hasWarnings()) {
				runStatus = Status.WARN;
			} else {
				if (!pendingSuites.isEmpty()) {
					runStatus = Status.PENDING;
				} else {
					runStatus = Status.OK;
				}
			}
		}
	}

	/**
	 * Categorize all test results according to status.
	 */
	private void tallyTests(SuiteResults sResult) {
		for (TestResults tResult : sResult.getTests()) {
			allTests.add(tResult);
			switch (tResult.getStatus()) {
			case OK:
				passingTests.add(tResult);
				break;
			case PENDING:
				pendingTests.add(tResult);
				break;
			case WARN:
				ignoredTests.add(tResult);
				break;
			default: // Status.ERROR
				failingTests.add(tResult);
				break;
			}
		}
	}

	// ----------------------------------------------------------------------
	// Access the derived data.
	// ----------------------------------------------------------------------

	public Status getRunStatus() {
		return runStatus;
	}

	public long getStartTime() {
		return dataListenerInfo.getStartTime();
	}

	public long getEndTime() {
		return dataListenerInfo.getEndTime();
	}

	public long getElapsedTime() {
		return dataListenerInfo.getElapsedTime();
	}

	public boolean isAnyPasses() {
		return !(passingSuites.isEmpty() && passingTests.isEmpty());
	}

	public boolean isAnyFailures() {
		return !(failingSuites.isEmpty() && failingTests.isEmpty());
	}

	public boolean isAnyIgnores() {
		return !(ignoredSuites.isEmpty() && ignoredTests.isEmpty());
	}

	public boolean isAnyPending() {
		return !pendingSuites.isEmpty();
	}

	public int getTotalSuiteCount() {
		return allSuiteData.size();
	}

	public int getPassingSuiteCount() {
		return passingSuites.size();
	}

	public int getFailingSuiteCount() {
		return failingSuites.size();
	}

	public int getIgnoredSuiteCount() {
		return ignoredSuites.size();
	}

	public int getPendingSuitesCount() {
		return pendingSuites.size();
	}

	public Collection<SuiteResults> getSuiteResults() {
		return Collections.unmodifiableCollection(suiteResults);
	}

	public int getTotalTestCount() {
		return allTests.size();
	}

	public int getPassingTestCount() {
		return passingTests.size();
	}

	public int getFailingTestCount() {
		return failingTests.size();
	}

	public int getIgnoredTestCount() {
		return ignoredTests.size();
	}

	public Collection<TestResults> getAllTests() {
		return Collections.unmodifiableCollection(allTests);
	}

	public Collection<TestResults> getFailingTests() {
		return Collections.unmodifiableCollection(failingTests);
	}

	public Collection<TestResults> getIgnoredTests() {
		return Collections.unmodifiableCollection(ignoredTests);
	}

}
